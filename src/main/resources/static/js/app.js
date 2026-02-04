const API = '/api';

let allStocks = [];
let allRecommendations = [];
let heatMapData = [];
let diversificationData = null;
let marketMoversData = null;
let selectedStockSymbol = null;
let watchlist = JSON.parse(localStorage.getItem('watchlist') || '[]');
let priceAlerts = JSON.parse(localStorage.getItem('priceAlerts') || '[]');

// Navigation
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        const target = link.getAttribute('href').slice(1);
        document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        document.getElementById(target).classList.add('active');
        link.classList.add('active');
    });
});

// Fetch helpers
async function get(url) {
    const res = await fetch(API + url);
    if (!res.ok) {
        const errorText = await res.text();
        console.error(`API Error (${res.status}): ${url}`, errorText);
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
    }
    const contentType = res.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
        console.error(`Expected JSON but got: ${contentType}`, url);
        throw new Error(`Expected JSON response but got ${contentType || 'unknown'}`);
    }
    try {
        return await res.json();
    } catch (e) {
        console.error('JSON parsing error:', e, url);
        throw new Error(`Failed to parse JSON: ${e.message}`);
    }
}

// ==================== Utility Functions ====================

function escape(str) {
    if (str == null) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function formatVolume(volume) {
    if (volume == null) return '—';
    if (volume >= 1000000) return (volume / 1000000).toFixed(1) + 'M';
    if (volume >= 1000) return (volume / 1000).toFixed(1) + 'K';
    return volume.toString();
}

// ==================== Dashboard ====================

async function loadDashboard() {
    try {
        const [stocks, recs, movers, div] = await Promise.all([
            get('/stocks'),
            get('/recommendations?limit=50'),
            get('/market/movers'),
            get('/portfolio/diversification')
        ]);
        
        allStocks = stocks;
        allRecommendations = recs;
        marketMoversData = movers;
        diversificationData = div;

        document.getElementById('totalStocks').textContent = stocks.length;
        document.getElementById('buyCount').textContent = recs.filter(r => r.action === 'BUY').length;
        document.getElementById('sellCount').textContent = recs.filter(r => r.action === 'SELL').length;
        document.getElementById('divScore').textContent = div.diversificationScore != null
            ? Math.round(div.diversificationScore) + '%'
            : '—';

        renderMarketMovers(movers);
        renderCharts(recs, stocks);
    } catch (e) {
        console.error('Dashboard load error:', e);
    }
}

function renderMarketMovers(movers) {
    const gainersList = document.getElementById('topGainersList');
    if (gainersList && movers?.topGainers?.length > 0) {
        gainersList.innerHTML = movers.topGainers.slice(0, 5).map(s => `
            <li class="movers-item">
                <span class="movers-symbol">${escape(s.symbol)}</span>
                <span class="movers-change positive">+${(s.changePercent || 0).toFixed(2)}%</span>
            </li>
        `).join('');
    } else if (gainersList) {
        gainersList.innerHTML = '<li class="movers-placeholder">No data</li>';
    }

    const losersList = document.getElementById('topLosersList');
    if (losersList && movers?.topLosers?.length > 0) {
        losersList.innerHTML = movers.topLosers.slice(0, 5).map(s => `
            <li class="movers-item">
                <span class="movers-symbol">${escape(s.symbol)}</span>
                <span class="movers-change negative">${(s.changePercent || 0).toFixed(2)}%</span>
            </li>
        `).join('');
    } else if (losersList) {
        losersList.innerHTML = '<li class="movers-placeholder">No data</li>';
    }

    const activeList = document.getElementById('mostActiveList');
    if (activeList && movers?.mostActive?.length > 0) {
        activeList.innerHTML = movers.mostActive.slice(0, 5).map(s => `
            <li class="movers-item">
                <span class="movers-symbol">${escape(s.symbol)}</span>
                <span class="movers-change">Vol: ${formatVolume(s.volume)}</span>
            </li>
        `).join('');
    } else if (activeList) {
        activeList.innerHTML = '<li class="movers-placeholder">No data</li>';
    }
}

function renderCharts(recs, stocks) {
    const buyCount = recs.filter(r => r.action === 'BUY').length;
    const sellCount = recs.filter(r => r.action === 'SELL').length;
    const holdCount = recs.filter(r => r.action === 'HOLD').length;

    const ctxRec = document.getElementById('chartRecommendations');
    if (ctxRec && window.Chart) {
        new Chart(ctxRec, {
            type: 'doughnut',
            data: {
                labels: ['Buy', 'Sell', 'Hold'],
                datasets: [{
                    data: [buyCount, sellCount, holdCount],
                    backgroundColor: ['#059669', '#dc2626', '#6b7280']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    const nseCount = stocks.filter(s => s.exchange === 'NSE').length;
    const bseCount = stocks.filter(s => s.exchange === 'BSE').length;
    const ctxEx = document.getElementById('chartExchange');
    if (ctxEx && window.Chart) {
        new Chart(ctxEx, {
            type: 'pie',
            data: {
                labels: ['NSE', 'BSE'],
                datasets: [{
                    data: [nseCount, bseCount],
                    backgroundColor: ['#00d09c', '#00b386']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }
}

// ==================== Portfolio Performance ====================

async function loadPortfolio() {
    try {
        const [performance, history] = await Promise.all([
            get('/portfolio/performance'),
            get('/portfolio/performance/history?days=30')
        ]);
        renderPortfolioPerformance(performance, history);
    } catch (e) {
        console.error('Portfolio load error:', e);
    }
}

function renderPortfolioPerformance(performance, history) {
    const valueEl = document.getElementById('portfolioValue');
    const gainEl = document.getElementById('portfolioGain');
    const dayChangeEl = document.getElementById('portfolioDayChange');
    
    if (valueEl) {
        valueEl.textContent = '₹' + (performance.totalValue || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 });
    }
    
    if (gainEl && performance.totalGain !== undefined) {
        const gain = performance.totalGain;
        const gainPercent = performance.totalGainPercent || 0;
        const sign = gain >= 0 ? '+' : '';
        gainEl.textContent = `${sign}₹${Math.abs(gain).toFixed(2)} (${sign}${gainPercent.toFixed(2)}%)`;
        gainEl.className = 'card-value ' + (gain >= 0 ? 'green' : 'red');
    }
    
    if (dayChangeEl && performance.dayChange !== undefined) {
        const change = performance.dayChange || 0;
        const changePercent = performance.dayChangePercent || 0;
        const sign = change >= 0 ? '+' : '';
        dayChangeEl.textContent = `${sign}₹${Math.abs(change).toFixed(2)} (${sign}${changePercent.toFixed(2)}%)`;
        dayChangeEl.className = 'card-value ' + (change >= 0 ? 'green' : 'red');
    }

    const ctxHistory = document.getElementById('chartPortfolioHistory');
    if (ctxHistory && window.Chart && history?.length > 0) {
        new Chart(ctxHistory, {
            type: 'line',
            data: {
                labels: history.map(h => h.date),
                datasets: [{
                    label: 'Portfolio Value',
                    data: history.map(h => h.value),
                    borderColor: '#00d09c',
                    backgroundColor: 'rgba(0, 208, 156, 0.1)',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { display: false } },
                scales: {
                    y: {
                        beginAtZero: false,
                        ticks: { callback: value => '₹' + value.toLocaleString('en-IN') }
                    }
                }
            }
        });
    }

    const holdingsBody = document.getElementById('holdingsTableBody');
    if (holdingsBody && performance.holdings?.length > 0) {
        holdingsBody.innerHTML = performance.holdings.map(h => {
            const gainClass = h.gain >= 0 ? 'positive' : 'negative';
            const dayClass = h.dayChange >= 0 ? 'positive' : 'negative';
            return `<tr>
                <td><strong>${escape(h.symbol)}</strong></td>
                <td>${escape(h.name || '—')}</td>
                <td>${escape(h.exchange || '—')}</td>
                <td>₹${(h.currentValue || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                <td>₹${(h.costBasis || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                <td class="${gainClass}">${h.gain >= 0 ? '+' : ''}₹${Math.abs(h.gain || 0).toFixed(2)}</td>
                <td class="${gainClass}">${h.gainPercent >= 0 ? '+' : ''}${(h.gainPercent || 0).toFixed(2)}%</td>
                <td class="${dayClass}">${h.dayChangePercent >= 0 ? '+' : ''}${(h.dayChangePercent || 0).toFixed(2)}%</td>
                <td>${(h.weight || 0).toFixed(1)}%</td>
            </tr>`;
        }).join('');
    } else if (holdingsBody) {
        holdingsBody.innerHTML = '<tr><td colspan="9" class="empty-message">No holdings in portfolio</td></tr>';
    }
}

// ==================== Market Data ====================

let currentMarketTab = 'gainers';

async function loadMarketData() {
    try {
        const [overview, gainers, losers, active] = await Promise.all([
            get('/market/overview'),
            get('/market/top-gainers?limit=10'),
            get('/market/top-losers?limit=10'),
            get('/market/most-active?limit=10')
        ]);
        renderMarketOverview(overview);
        renderMarketTable(gainers, losers, active);
    } catch (e) {
        console.error('Market load error:', e);
    }
}

function renderMarketOverview(overview) {
    if (document.getElementById('marketTotalStocks')) {
        document.getElementById('marketTotalStocks').textContent = overview.totalStocks || '—';
    }
    if (document.getElementById('marketAdvancing')) {
        document.getElementById('marketAdvancing').textContent = overview.advancingStocks || '—';
    }
    if (document.getElementById('marketDeclining')) {
        document.getElementById('marketDeclining').textContent = overview.decliningStocks || '—';
    }
    if (document.getElementById('marketSentiment')) {
        const sentiment = overview.marketSentiment || 50;
        const trend = overview.overallTrend || 'NEUTRAL';
        document.getElementById('marketSentiment').textContent = `${trend} (${sentiment.toFixed(0)})`;
        document.getElementById('marketSentiment').className = sentiment > 55 ? 'green' : sentiment < 45 ? 'red' : '';
    }
}

function renderMarketTable(gainers, losers, active) {
    let data = gainers;
    if (currentMarketTab === 'losers') data = losers;
    if (currentMarketTab === 'active') data = active;

    const tbody = document.getElementById('marketTableBody');
    if (tbody && data) {
        tbody.innerHTML = data.map(s => {
            const chgClass = (s.changePercent || 0) >= 0 ? 'positive' : 'negative';
            return `<tr>
                <td><strong>${escape(s.symbol)}</strong></td>
                <td>${escape(s.name || '—')}</td>
                <td>${escape(s.exchange || '—')}</td>
                <td>₹${(s.currentPrice || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                <td class="${chgClass}">${(s.changePercent || 0) >= 0 ? '+' : ''}${(s.changePercent || 0).toFixed(2)}%</td>
                <td>${formatVolume(s.volume)}</td>
                <td>${s.peRatio?.toFixed(1) || '—'}</td>
            </tr>`;
        }).join('');
    }
}

document.querySelectorAll('.tab-btn[data-market]').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn[data-market]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentMarketTab = btn.dataset.market;
        loadMarketData();
    });
});

// ==================== Sector Performance ====================

async function loadSectorPerformance() {
    try {
        const data = await get('/sectors');
        renderSectorPerformance(data);
    } catch (e) {
        console.error('Sector load error:', e);
    }
}

function renderSectorPerformance(data) {
    if (data.marketSentiment) {
        const trendEl = document.getElementById('sectorMarketTrend');
        const sentEl = document.getElementById('sectorOverallSentiment');
        if (trendEl) trendEl.textContent = data.marketSentiment.overallTrend || '—';
        if (sentEl) {
            const score = data.marketSentiment.overallScore || 50;
            sentEl.textContent = score.toFixed(0);
            sentEl.className = score > 55 ? 'green' : score < 45 ? 'red' : '';
        }
    }

    const ctxPerf = document.getElementById('chartSectorPerformance');
    if (ctxPerf && window.Chart && data.sectors?.length > 0) {
        const sectors = data.sectors.slice(0, 8);
        new Chart(ctxPerf, {
            type: 'bar',
            data: {
                labels: sectors.map(s => s.displayName || s.name),
                datasets: [{
                    label: 'Day Change %',
                    data: sectors.map(s => s.dayChangePercent || 0),
                    backgroundColor: sectors.map(s => s.dayChangePercent >= 0 ? '#059669' : '#dc2626')
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { display: false } }
            }
        });
    }

    const ctxAlloc = document.getElementById('chartSectorAllocation');
    if (ctxAlloc && window.Chart && diversificationData?.sectorAllocation) {
        const sectors = diversificationData.sectorAllocation;
        new Chart(ctxAlloc, {
            type: 'doughnut',
            data: {
                labels: Object.keys(sectors),
                datasets: [{
                    data: Object.values(sectors),
                    backgroundColor: ['#00d09c', '#00b386', '#059669', '#0d9488', '#14b8a6', '#2dd4bf', '#5eead4', '#99f6e4']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { position: 'right' } }
            }
        });
    }

    const sectorDetails = document.getElementById('sectorDetails');
    if (sectorDetails && data.sectors?.length > 0) {
        sectorDetails.innerHTML = data.sectors.map(s => {
            const changeClass = (s.dayChangePercent || 0) >= 0 ? 'positive' : 'negative';
            const sentimentClass = s.sentiment?.toUpperCase().includes('BULLISH') ? 'green' : s.sentiment?.toUpperCase().includes('BEARISH') ? 'red' : '';
            return `<div class="sector-item">
                <div class="sector-header">
                    <span class="sector-name">${escape(s.displayName || s.name)}</span>
                    <span class="sector-change ${changeClass}">${(s.dayChangePercent || 0) >= 0 ? '+' : ''}${(s.dayChangePercent || 0).toFixed(2)}%</span>
                </div>
                <div class="sector-details">
                    <span>Week: ${(s.weekChange || 0) >= 0 ? '+' : ''}${(s.weekChange || 0).toFixed(1)}%</span>
                    <span>Month: ${(s.monthChange || 0) >= 0 ? '+' : ''}${(s.monthChange || 0).toFixed(1)}%</span>
                    <span class="${sentimentClass}">${s.sentiment || '—'}</span>
                </div>`;
        }).join('');
    }
}

// ==================== AI Insights ====================

async function loadAIInsights() {
    try {
        const insights = await get('/ai-insights');
        renderAIInsights(insights);
    } catch (e) {
        console.error('AI insights load error:', e);
    }
}

function renderAIInsights(insights) {
    if (!insights) return;

    document.getElementById('aiInsightTitle').textContent = insights.title || 'Market Intelligence Report';
    document.getElementById('aiSummaryText').textContent = insights.summary || 'No data available';
    document.getElementById('aiDetailedText').textContent = insights.detailedAnalysis || '';

    if (insights.outlook) {
        document.getElementById('outlookOverall').textContent = insights.outlook.overall || '—';
        document.getElementById('outlookShort').textContent = insights.outlook.shortTerm || '—';
        document.getElementById('outlookMedium').textContent = insights.outlook.mediumTerm || '—';
        document.getElementById('outlookLong').textContent = insights.outlook.longTerm || '—';
        const confidence = insights.outlook.confidence || 0;
        document.getElementById('outlookConfidence').style.width = confidence + '%';
        document.getElementById('outlookConfidenceText').textContent = confidence.toFixed(0) + '%';
    }

    if (insights.trendingStocks?.length > 0) {
        const t = insights.trendingStocks[0];
        const trendingDiv = document.getElementById('trendingStock');
        trendingDiv.innerHTML = `
            <div class="trending-symbol">${escape(t.symbol || '—')}</div>
            <div class="trending-name">${escape(t.name || '—')}</div>
            <div class="trending-reason">${escape(t.reason || '—')}</div>
            <div class="trending-score">AI Score: ${(t.sentimentScore || 0).toFixed(0)}</div>
        `;
    }

    if (insights.riskAlerts?.length > 0) {
        const r = insights.riskAlerts[0];
        document.querySelector('.risk-type').textContent = r.type || '—';
        document.querySelector('.risk-desc').textContent = r.description || '—';
        const riskLevel = r.riskLevel || 0;
        document.getElementById('riskFill').style.width = riskLevel + '%';
        document.getElementById('riskLevelText').textContent = riskLevel.toFixed(0) + '%';
    }

    if (insights.opportunities?.length > 0) {
        const o = insights.opportunities[0];
        document.querySelector('.opp-type').textContent = o.type || '—';
        document.querySelector('.opp-desc').textContent = o.description || '—';
        document.querySelector('.opp-symbols').textContent = (o.suggestedSymbols || []).join(', ') || '—';
        document.querySelector('.opp-return').textContent = 'Potential: ' + (o.potentialReturn || 0).toFixed(1) + '%';
    }

    const factorsList = document.getElementById('keyFactorsList');
    if (insights.outlook?.keyFactors?.length > 0) {
        factorsList.innerHTML = insights.outlook.keyFactors.map(f => `<li>${escape(f)}</li>`).join('');
    } else {
        factorsList.innerHTML = '<li>No key factors available</li>';
    }
}

// ==================== Heat Map ====================

async function loadHeatMap() {
    try {
        const data = await get('/dumb-money/heat-map');
        heatMapData = data;
        updateHeatMapSummary(data);
        renderHeatMapGrid(data);
        renderHeatMapTable(data);
    } catch (e) {
        console.error('Heat map load error:', e);
    }
}

function updateHeatMapSummary(data) {
    const counts = { OVERHEATED: 0, WARM: 0, NEUTRAL: 0, COOL: 0 };
    let totalScore = 0;
    data.forEach(d => {
        const level = d.heatLevel || 'NEUTRAL';
        if (counts[level] !== undefined) counts[level]++;
        totalScore += (d.heatScore || 0);
    });
    
    document.getElementById('overheatedCount').textContent = counts.OVERHEATED;
    document.getElementById('warmCount').textContent = counts.WARM;
    document.getElementById('neutralCount').textContent = counts.NEUTRAL;
    document.getElementById('coolCount').textContent = counts.COOL;
    document.getElementById('avgHeatScore').textContent = data.length > 0 ? (totalScore / data.length).toFixed(1) : '0';
}

function renderHeatMapGrid(data) {
    const container = document.getElementById('dumbMoneyHeatMap');
    const filter = document.getElementById('heatLevelFilter')?.value || '';
    let filteredData = filter ? data.filter(d => d.heatLevel === filter) : data;
    
    container.innerHTML = filteredData.map(d => {
        const level = (d.heatLevel || 'NEUTRAL').toLowerCase().replace(' ', '');
        const cls = level === 'overheated' ? 'overheated' : level === 'warm' ? 'warm' : level === 'neutral' ? 'neutral' : 'cool';
        const trendIcon = d.trend?.includes('UP') ? '↑' : d.trend?.includes('DOWN') ? '↓' : '→';
        const changeStr = d.changePercent != null ? (d.changePercent >= 0 ? '+' : '') + d.changePercent.toFixed(2) + '%' : '—';
        
        return `<div class="heat-cell ${cls}" title="${d.symbol}: Heat ${(d.heatScore || 0).toFixed(0)}">
            <span class="symbol">${escape(d.symbol)}</span>
            <span class="heat">${(d.heatScore || 0).toFixed(0)}</span>
            <span class="trend">${trendIcon} ${changeStr}</span>
        </div>`;
    }).join('');
}

function renderHeatMapTable(data) {
    const tbody = document.getElementById('heatMapTableBody');
    const filter = document.getElementById('heatLevelFilter')?.value || '';
    let filteredData = filter ? data.filter(d => d.heatLevel === filter) : data;
    
    tbody.innerHTML = filteredData.map(d => {
        const level = (d.heatLevel || 'NEUTRAL').toLowerCase();
        const levelClass = level === 'overheated' ? 'red' : level === 'warm' ? 'orange' : level === 'neutral' ? '' : 'green';
        const changeClass = (d.changePercent || 0) >= 0 ? 'positive' : 'negative';
        const trendClass = d.trend?.includes('UP') ? 'positive' : d.trend?.includes('DOWN') ? 'negative' : '';
        
        return `<tr>
            <td><strong>${escape(d.symbol)}</strong></td>
            <td>₹${(d.currentPrice || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
            <td class="${changeClass}">${(d.changePercent || 0) >= 0 ? '+' : ''}${(d.changePercent || 0).toFixed(2)}%</td>
            <td>${formatVolume(d.volume)}</td>
            <td><strong>${(d.heatScore || 0).toFixed(1)}</strong></td>
            <td class="${levelClass}">${d.heatLevel || '—'}</td>
            <td>${(d.aiSentimentScore || 0).toFixed(0)}</td>
            <td class="${trendClass}">${(d.trend || '—').replace('_', ' ')}</td>
            <td><small title="${escape(d.aiReasoning || '')}">${escape((d.aiReasoning || '').substring(0, 60))}...</small></td>
        </tr>`;
    }).join('');
}

document.getElementById('heatLevelFilter')?.addEventListener('change', () => {
    renderHeatMapGrid(heatMapData);
    renderHeatMapTable(heatMapData);
});

async function loadHeatMapRealtime() {
    const btn = document.getElementById('refreshHeatBtn');
    const msgEl = document.getElementById('heatRefreshMsg');
    if (!btn || !msgEl) return;
    
    btn.disabled = true;
    btn.textContent = '🔄 Fetching...';
    
    try {
        const data = await get('/dumb-money/heat-map/realtime');
        heatMapData = data;
        updateHeatMapSummary(data);
        renderHeatMapGrid(data);
        renderHeatMapTable(data);
        msgEl.textContent = `Updated: ${data.length} stocks`;
        msgEl.className = 'refresh-msg success';
    } catch (e) {
        msgEl.textContent = 'Refresh failed: ' + e.message;
        msgEl.className = 'refresh-msg error';
    } finally {
        btn.disabled = false;
        btn.textContent = '🔄 Refresh Real-Time Data';
    }
}

document.getElementById('refreshHeatBtn')?.addEventListener('click', loadHeatMapRealtime);

// ==================== Watchlist ====================

function loadWatchlist() {
    renderWatchlist();
    renderPriceAlerts();
}

function renderWatchlist() {
    const tbody = document.getElementById('watchlistTableBody');
    const countEl = document.getElementById('watchlistCount');
    
    if (!tbody) return;
    
    countEl.textContent = watchlist.length + ' stocks';
    
    if (watchlist.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-message">Your watchlist is empty. Add stocks to track them here.</td></tr>';
        return;
    }
    
    tbody.innerHTML = watchlist.map(symbol => {
        const stock = allStocks.find(s => s.symbol === symbol) || {};
        const heat = heatMapData.find(h => h.symbol === symbol) || {};
        const chgClass = (stock.changePercent || 0) >= 0 ? 'positive' : 'negative';
        
        return `<tr>
            <td><strong>${escape(symbol)}</strong></td>
            <td>${escape(stock.name || '—')}</td>
            <td>${escape(stock.exchange || '—')}</td>
            <td>₹${(stock.currentPrice || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
            <td class="${chgClass}">${(stock.changePercent || 0) >= 0 ? '+' : ''}${(stock.changePercent || 0).toFixed(2)}%</td>
            <td>${(heat.heatScore || 0).toFixed(0)}</td>
            <td><button class="btn-remove-watchlist" data-symbol="${escape(symbol)}">✕</button></td>
        </tr>`;
    }).join('');
    
    tbody.querySelectorAll('.btn-remove-watchlist').forEach(btn => {
        btn.addEventListener('click', (e) => {
            removeFromWatchlist(e.target.dataset.symbol);
        });
    });
}

function renderPriceAlerts() {
    const alertsList = document.getElementById('alertsList');
    if (!alertsList) return;
    
    if (priceAlerts.length === 0) {
        alertsList.innerHTML = '<div class="empty-message">No active alerts.</div>';
        return;
    }
    
    alertsList.innerHTML = priceAlerts.map((alert, index) => `
        <div class="alert-item">
            <div>
                <span class="alert-symbol">${escape(alert.symbol)}</span>
                <span class="alert-condition">${alert.condition} ₹${alert.price.toFixed(2)}</span>
            </div>
            <button class="alert-delete" data-index="${index}">✕</button>
        </div>
    `).join('');
    
    alertsList.querySelectorAll('.alert-delete').forEach(btn => {
        btn.addEventListener('click', (e) => {
            removeAlert(parseInt(e.target.dataset.index));
        });
    });
}

function addToWatchlist(symbol) {
    if (!watchlist.includes(symbol)) {
        watchlist.push(symbol);
        localStorage.setItem('watchlist', JSON.stringify(watchlist));
        renderWatchlist();
    }
}

function removeFromWatchlist(symbol) {
    watchlist = watchlist.filter(s => s !== symbol);
    localStorage.setItem('watchlist', JSON.stringify(watchlist));
    renderWatchlist();
}

function addAlert(symbol, condition, price) {
    priceAlerts.push({ symbol, condition, price, createdAt: new Date().toISOString() });
    localStorage.setItem('priceAlerts', JSON.stringify(priceAlerts));
    renderPriceAlerts();
}

function removeAlert(index) {
    priceAlerts.splice(index, 1);
    localStorage.setItem('priceAlerts', JSON.stringify(priceAlerts));
    renderPriceAlerts();
}

document.getElementById('addToWatchlistBtn')?.addEventListener('click', () => {
    const symbol = prompt('Enter stock symbol to add:');
    if (symbol) addToWatchlist(symbol.toUpperCase().trim());
});

// ==================== Stock Search ====================

function initSearch() {
    const searchInput = document.getElementById('stockSearch');
    const searchResults = document.getElementById('searchResults');
    
    if (!searchInput || !searchResults) return;
    
    searchInput.addEventListener('input', async (e) => {
        const query = e.target.value.toUpperCase().trim();
        
        if (query.length < 2) {
            searchResults.classList.remove('active');
            return;
        }
        
        const results = allStocks.filter(s => 
            s.symbol?.includes(query) || s.name?.toUpperCase().includes(query)
        ).slice(0, 5);
        
        if (results.length > 0) {
            searchResults.innerHTML = results.map(s => `
                <div class="search-result-item" data-symbol="${escape(s.symbol)}">
                    <span class="search-result-symbol">${escape(s.symbol)}</span>
                    <span>${escape(s.name || '')}</span>
                </div>
            `).join('');
            searchResults.classList.add('active');
            
            searchResults.querySelectorAll('.search-result-item').forEach(item => {
                item.addEventListener('click', () => {
                    const symbol = item.dataset.symbol;
                    searchInput.value = symbol;
                    searchResults.classList.remove('active');
                    addToWatchlist(symbol);
                });
            });
        } else {
            searchResults.innerHTML = '<div class="search-result-item">No results</div>';
            searchResults.classList.add('active');
        }
    });
    
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.search-container')) {
            searchResults.classList.remove('active');
        }
    });
}

// ==================== Stocks Table ====================

async function loadStocks(exchange) {
    try {
        const url = exchange ? `/stocks?exchange=${encodeURIComponent(exchange)}` : '/stocks';
        const stocks = await get(url);
        allStocks = stocks;
        renderStocksTable(stocks);
    } catch (e) {
        console.error('Stocks load error:', e);
    }
}

function renderStocksTable(stocks) {
    const tbody = document.getElementById('stocksTableBody');
    tbody.innerHTML = stocks.map(s => {
        const chg = s.changePercent != null ? s.changePercent : 0;
        const chgClass = chg >= 0 ? 'positive' : 'negative';
        const heat = heatMapData.find(h => h.symbol === s.symbol);
        const heatScore = heat?.heatScore || 0;
        const heatClass = heatScore >= 75 ? 'red' : heatScore >= 55 ? 'orange' : heatScore >= 35 ? '' : 'green';
        
        return `<tr>
            <td><strong>${escape(s.symbol)}</strong></td>
            <td>${escape(s.name || '—')}</td>
            <td>${escape(s.sector || '—')}</td>
            <td>${escape(s.exchange || '—')}</td>
            <td>₹${(s.currentPrice || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
            <td class="${chgClass}">${chg >= 0 ? '+' : ''}${chg.toFixed(2)}%</td>
            <td>${s.peRatio != null ? s.peRatio.toFixed(1) : '—'}</td>
            <td class="${heatClass}">${heatScore.toFixed(0)}</td>
        </tr>`;
    }).join('');
}

document.getElementById('exchangeFilter')?.addEventListener('change', (e) => {
    loadStocks(e.target.value || null);
});

// ==================== Recommendations ====================

async function loadRecommendations(filter) {
    try {
        let url = '/recommendations?limit=30';
        if (filter === 'buy') url = '/recommendations/buy?limit=15';
        if (filter === 'sell') url = '/recommendations/sell?limit=15';
        const recs = await get(url);
        allRecommendations = recs;
        renderRecommendations(recs);
    } catch (e) {
        console.error('Recommendations load error:', e);
    }
}

function renderRecommendations(recs) {
    const grid = document.getElementById('recommendationsGrid');
    if (!grid) return;
    
    grid.innerHTML = recs.map(r => {
        const action = (r.action || 'HOLD').toLowerCase();
        return `<div class="rec-card ${action}">
            <span class="symbol">${escape(r.stock?.symbol || '—')}</span>
            <span class="name">${escape(r.stock?.name || '—')}</span>
            <span class="reason">${escape(r.reason || '—')}</span>
            <span class="action">${escape(r.action || '—')}</span>
            <span class="score">Score: ${(r.score || 0).toFixed(0)}</span>
        </div>`;
    }).join('');
}

// ==================== Diversification ====================

async function loadDiversification() {
    try {
        const data = await get('/portfolio/diversification');
        diversificationData = data;
        renderDiversification(data);
    } catch (e) {
        console.error('Diversification load error:', e);
    }
}

function renderDiversification(data) {
    // Render suggestions
    const suggestionsEl = document.getElementById('diversificationSuggestions');
    if (suggestionsEl && data.suggestions) {
        suggestionsEl.innerHTML = data.suggestions.map(s => `<li>${escape(s)}</li>`).join('');
    }
    
    // Render sector chart
    const ctxSector = document.getElementById('chartSector');
    if (ctxSector && window.Chart && data.sectorAllocation) {
        const sectors = data.sectorAllocation;
        new Chart(ctxSector, {
            type: 'doughnut',
            data: {
                labels: Object.keys(sectors),
                datasets: [{
                    data: Object.values(sectors),
                    backgroundColor: ['#00d09c', '#00b386', '#059669', '#0d9488', '#14b8a6', '#2dd4bf', '#5eead4', '#99f6e4']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { position: 'right' } }
            }
        });
    }
    
    // Render exchange chart
    const ctxExchange = document.getElementById('chartExchangeDiv');
    if (ctxExchange && window.Chart && data.exchangeAllocation) {
        const exchanges = data.exchangeAllocation;
        new Chart(ctxExchange, {
            type: 'pie',
            data: {
                labels: Object.keys(exchanges),
                datasets: [{
                    data: Object.values(exchanges),
                    backgroundColor: ['#00d09c', '#00b386']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { position: 'right' } }
            }
        });
    }
}

// ==================== Initialization ====================

document.addEventListener('DOMContentLoaded', () => {
    console.log('Portfolio Manager initializing...');
    
    // Load initial data for each section
    loadDashboard();
    loadPortfolio();
    loadMarketData();
    loadSectorPerformance();
    loadHeatMap();
    loadAIInsights();
    loadDiversification();
    loadStocks();
    loadRecommendations();
    loadWatchlist();
    initSearch();
    
    console.log('Portfolio Manager initialized');
});
