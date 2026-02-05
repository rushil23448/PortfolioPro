/**
 * PortfolioPro - Vanilla JS Frontend with Chart.js
 * Handles API communication, routing, and modern chart rendering.
 */

// --- Configuration ---
const API_BASE = "http://localhost:8081/api";
const REFRESH_RATE = 5000;

// --- Chart Instances (Global) ---
// We keep track of these to update data without re-drawing the whole chart
let lineChartInstance = null;
let pieChartInstance = null;
let barChartInstance = null;
let gaugeChartInstance = null;

// --- State Management ---
let state = {
    holders: [],
    currentHolderId: null,
    holdings: [],
    analytics: {},
    recommendations: [],
    history: []
};

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    fetchHolders();
    initTicker();

    // Auto-refresh loop
    setInterval(() => {
        if (state.currentHolderId) {
            refreshData();
        }
    }, REFRESH_RATE);

    document.getElementById('refresh-btn').addEventListener('click', refreshData);
});

// --- Navigation Logic ---
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const sections = document.querySelectorAll('.view-section');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');

            const targetId = `view-${item.dataset.tab}`;
            sections.forEach(sec => sec.classList.remove('active'));
            const targetSection = document.getElementById(targetId);
            if (targetSection) targetSection.classList.add('active');

            if (item.dataset.tab === 'dumb-money') renderAISignals();
            if (item.dataset.tab === 'portfolio') renderPortfolio();
            if (item.dataset.tab === 'manage') {
                fetchStocks();
                syncManageDropdown();
            }
        });
    });
}

// --- Ticker Logic ---
async function initTicker() {
    try {
        const res = await fetch(`${API_BASE}/stocks`);
        const stocks = await res.json();
        const track = document.getElementById('ticker-track');
        if (!track) return;
        track.innerHTML = '';

        const tickerData = [...stocks, ...stocks];

        tickerData.forEach(s => {
            const change = s.currentPrice - s.basePrice;
            const pct = (change / s.basePrice) * 100;
            const isUp = change >= 0;

            const html = `
                <div class="ticker-item">
                    <span class="ticker-symbol">${s.symbol}</span>
                    <span class="ticker-price">${formatCurrency(s.currentPrice)}</span>
                    <span class="ticker-change ${isUp ? 'ticker-up' : 'ticker-down'}">
                        ${isUp ? '▲' : '▼'} ${Math.abs(pct).toFixed(2)}%
                    </span>
                </div>
            `;
            track.innerHTML += html;
        });
    } catch (e) { console.error("Ticker Error:", e); }
}

// --- API Calls ---
async function fetchHolders() {
    try {
        const res = await fetch(`${API_BASE}/holders`);
        state.holders = await res.json();
        const select = document.getElementById('holder-select');
        select.innerHTML = '<option value="" disabled selected>Select Investor</option>';
        state.holders.forEach(h => {
            const opt = document.createElement('option');
            opt.value = h.id;
            opt.text = h.name;
            select.appendChild(opt);
        });

        select.addEventListener('change', (e) => {
            state.currentHolderId = e.target.value;
            // Reset history and charts when user changes to prevent old data mixing
            state.history = [];
            if(lineChartInstance) { lineChartInstance.destroy(); lineChartInstance = null; }
            refreshData();
        });
    } catch (error) { console.error("Error fetching holders:", error); }
}

async function refreshData() {
    if (!state.currentHolderId) return;

    try {
        const hid = state.currentHolderId;
        const [portfolioRes, analyticsRes, divRes] = await Promise.all([
            fetch(`${API_BASE}/portfolio/${hid}`),
            fetch(`${API_BASE}/${hid}/analytics`),
            fetch(`${API_BASE}/${hid}/diversification`)
        ]);

        state.holdings = await portfolioRes.json();
        state.analytics = await analyticsRes.json();
        state.recommendations = await divRes.json();

        // Update History
        state.history.push({
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
            value: state.analytics.currentValue
        });
        if (state.history.length > 20) state.history.shift();

        updateDashboard();

    } catch (error) { console.error("Sync Error:", error); }
}

// --- DOM Updating ---
function updateDashboard() {
    const a = state.analytics;
    document.getElementById('kpi-invested').innerText = formatCurrency(a.totalInvested);
    document.getElementById('kpi-current').innerText = formatCurrency(a.currentValue);

    const pnlEl = document.getElementById('kpi-pnl');
    pnlEl.innerText = formatCurrency(a.profitLoss);
    pnlEl.className = a.profitLoss >= 0 ? 'pnl-pos' : 'pnl-neg';

    const pnlPct = a.totalInvested > 0 ? (a.profitLoss / a.totalInvested) * 100 : 0;
    document.getElementById('kpi-pnl-percent').innerText = `${pnlPct.toFixed(2)}%`;

    document.getElementById('kpi-risk').innerText = `${a.riskScore}/100`;
    document.getElementById('kpi-risk-bar').style.width = `${a.riskScore}%`;

    renderTable();
    updateCharts(); // ✅ Call Chart.js update logic
    renderDiversification();
    renderAISignals();
    renderPortfolio();
}

// --- Chart.js Logic (Replaces Manual Canvas Drawing) ---

function updateCharts() {
    updateLineChart();
    updatePieChart();
    updateBarChart();
    updateGaugeChart();
}

function updateLineChart() {
    const ctx = document.getElementById('lineChart').getContext('2d');
    const labels = state.history.map(d => d.time);
    const data = state.history.map(d => d.value);

    // Create Gradient
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(37, 99, 235, 0.5)'); // Blue
    gradient.addColorStop(1, 'rgba(37, 99, 235, 0.0)');

    if (lineChartInstance) {
        lineChartInstance.data.labels = labels;
        lineChartInstance.data.datasets[0].data = data;
        lineChartInstance.update('none'); // 'none' mode for performance
    } else {
        lineChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Portfolio Value',
                    data: data,
                    borderColor: '#2563eb',
                    backgroundColor: gradient,
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4, // Smooth curve
                    pointRadius: 0,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { display: false }, ticks: { maxTicksLimit: 6 } },
                    y: { grid: { color: '#f1f5f9' }, beginAtZero: false }
                },
                interaction: { intersect: false, mode: 'index' }
            }
        });
    }
}

function updatePieChart() {
    const ctx = document.getElementById('pieChart').getContext('2d');
    const allocation = state.analytics.sectorAllocation || {};
    const labels = Object.keys(allocation);
    const data = Object.values(allocation);

    if (pieChartInstance) {
        pieChartInstance.data.labels = labels;
        pieChartInstance.data.datasets[0].data = data;
        pieChartInstance.update();
    } else {
        pieChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#64748b'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'right', labels: { boxWidth: 12 } } }
            }
        });
    }
}

function updateBarChart() {
    const ctx = document.getElementById('barChart').getContext('2d');

    const bins = { '0-50 (Low)': 0, '51-75 (Med)': 0, '76-100 (High)': 0 };
    if (state.holdings) {
        state.holdings.forEach(h => {
            const c = h.stock.confidenceScore;
            if (c <= 50) bins['0-50 (Low)']++;
            else if (c <= 75) bins['51-75 (Med)']++;
            else bins['76-100 (High)']++;
        });
    }

    if (barChartInstance) {
        barChartInstance.data.datasets[0].data = Object.values(bins);
        barChartInstance.update();
    } else {
        barChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Object.keys(bins),
                datasets: [{
                    label: 'Stocks Count',
                    data: Object.values(bins),
                    backgroundColor: ['#ef4444', '#f59e0b', '#10b981'],
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, ticks: { stepSize: 1 } },
                    x: { grid: { display: false } }
                }
            }
        });
    }
}

function updateGaugeChart() {
    const ctx = document.getElementById('gaugeChart').getContext('2d');
    const score = state.analytics.riskScore || 0;

    // Determine color based on score
    let color = '#10b981'; // Green
    if (score > 40) color = '#f59e0b'; // Orange
    if (score > 70) color = '#ef4444'; // Red

    if (gaugeChartInstance) {
        gaugeChartInstance.data.datasets[0].data = [score, 100 - score];
        gaugeChartInstance.data.datasets[0].backgroundColor[0] = color;
        gaugeChartInstance.update();
    } else {
        gaugeChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Risk', 'Safety'],
                datasets: [{
                    data: [score, 100 - score],
                    backgroundColor: [color, '#e2e8f0'],
                    borderWidth: 0,
                    cutout: '75%'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                rotation: -90,      // Start at top-left
                circumference: 180, // Draw half circle
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            }
        });
    }
}

// --- Other Renders (Table, Modals, etc.) ---

function renderTable() {
    const tbody = document.querySelector('#dashboard-table tbody');
    if (!tbody) return;
    tbody.innerHTML = '';

    state.holdings.forEach(h => {
        const s = h.stock;
        const pl = (s.currentPrice - h.avgPrice) * h.quantity;
        const row = `
            <tr>
                <td><strong>${s.symbol}</strong></td>
                <td>${s.sector}</td>
                <td>${h.quantity}</td>
                <td>${formatCurrency(h.avgPrice)}</td>
                <td>${formatCurrency(s.currentPrice)}</td>
                <td class="${pl >= 0 ? 'pnl-pos' : 'pnl-neg'}">${formatCurrency(pl)}</td>
                <td>${(s.volatility * 100).toFixed(1)}%</td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

function renderPortfolio() {
    const tbody = document.querySelector('#portfolio-table tbody');
    if (!tbody) return;
    tbody.innerHTML = '';
    if (!state.holdings || state.holdings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;">No holdings found. Select an investor.</td></tr>';
        return;
    }
    state.holdings.forEach(h => {
        const s = h.stock;
        const currentVal = s.currentPrice * h.quantity;
        const investedVal = h.avgPrice * h.quantity;
        const pl = currentVal - investedVal;
        const plPercent = investedVal > 0 ? (pl / investedVal) * 100 : 0;
        const plClass = pl >= 0 ? 'pnl-pos' : 'pnl-neg';
        const plSign = pl >= 0 ? '+' : '';
        const name = s.name || s.symbol;

        const row = `
            <tr>
                <td><strong>${s.symbol}</strong></td>
                <td>${name}</td>
                <td><span class="tag-sector">${s.sector}</span></td>
                <td>${h.quantity}</td>
                <td>${formatCurrency(h.avgPrice)}</td>
                <td>${formatCurrency(s.currentPrice)}</td>
                <td><strong>${formatCurrency(currentVal)}</strong></td>
                <td class="${plClass}">
                    ${plSign}${formatCurrency(pl)} <br>
                    <small>(${plSign}${plPercent.toFixed(2)}%)</small>
                </td>
                <td style="text-align: center;">
                    <button class="icon-btn" onclick="viewStockDetails('${s.symbol}')" title="View Details" style="font-size: 1.1rem; justify-content: center; width: 35px; height: 35px; margin: 0 auto;">👁️</button>
                </td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

function renderDiversification() {
    const container = document.getElementById('diversification-container');
    if (!container) return;
    container.innerHTML = '';
    state.recommendations.forEach(rec => {
        const severityClass = `rec-${rec.severity.toLowerCase()}`;
        const html = `
            <div class="recommendation-item ${severityClass}">
                <div class="msg"><strong>${rec.suggestedSector}</strong>: ${rec.message}</div>
                <div class="tag">${rec.severity} Priority</div>
            </div>`;
        container.innerHTML += html;
    });
}

function renderAISignals() {
    const container = document.getElementById('ai-signals-grid');
    if (!container) return;
    container.innerHTML = '';
    state.holdings.forEach(h => {
        const s = h.stock;
        let signal = "NEUTRAL", typeClass = "neutral", reason = "Market Perform", emoji = "😐";
        if (s.volatility < 0.3 && s.confidenceScore > 80) { signal = "SMART MONEY"; typeClass = "smart"; reason = "High confidence, stable volatility."; emoji = "🧠"; }
        else if (s.volatility > 0.4 || s.confidenceScore < 50) { signal = "DUMB MONEY"; typeClass = "dumb"; reason = "High volatility, low confidence."; emoji = "🤡"; }

        container.innerHTML += `
            <div class="signal-card ${typeClass}">
                <div class="signal-header"><span>${s.symbol}</span><span>${emoji}</span></div>
                <div class="signal-body"><h2>${signal}</h2><div class="signal-details">${reason}</div><small>Conf: ${s.confidenceScore} | Vol: ${s.volatility}</small></div>
            </div>`;
    });
}

// --- Modals & Utils ---
function viewStockDetails(symbol) {
    const holding = state.holdings.find(h => h.stock.symbol === symbol);
    if (!holding) return;
    const s = holding.stock;
    const pl = (s.currentPrice - holding.avgPrice) * holding.quantity;
    const plClass = pl >= 0 ? 'pnl-pos' : 'pnl-neg';
    let confClass = s.confidenceScore > 75 ? 'badge-success' : (s.confidenceScore < 50 ? 'badge-danger' : 'badge-neutral');

    document.getElementById('modal-title').innerHTML = `<span style="background:var(--primary); color:white; padding:4px 8px; border-radius:6px; font-size:0.9rem;">${s.symbol}</span> ${s.name}`;
    document.getElementById('modal-body').innerHTML = `
        <div class="modal-grid">
            <div class="modal-item"><span class="modal-label">Current Price</span><span class="modal-value">${formatCurrency(s.currentPrice)}</span></div>
            <div class="modal-item"><span class="modal-label">Avg Buy Price</span><span class="modal-value">${formatCurrency(holding.avgPrice)}</span></div>
            <div class="modal-item"><span class="modal-label">Quantity Held</span><span class="modal-value">${holding.quantity} Units</span></div>
            <div class="modal-item"><span class="modal-label">Net P/L</span><span class="modal-value ${plClass}">${formatCurrency(pl)}</span></div>
            <div class="modal-item"><span class="modal-label">Sector</span><span class="modal-value">${s.sector}</span></div>
            <div class="modal-item"><span class="modal-label">Volatility</span><span class="modal-value">${(s.volatility * 100).toFixed(1)}%</span></div>
        </div>
        <div style="margin-top: 1.5rem; border-top: 1px solid #e2e8f0; padding-top: 1rem;">
             <span class="modal-label">AI Confidence Score</span>
             <div style="display:flex; align-items:center; gap:10px; margin-top:5px;">
                <span class="modal-badge ${confClass}">${s.confidenceScore}/100</span>
                <div style="flex:1; height:6px; background:#e2e8f0; border-radius:3px;"><div style="width:${s.confidenceScore}%; height:100%; background:currentColor; border-radius:3px; opacity:0.7;"></div></div>
             </div>
        </div>`;

    const modal = document.getElementById('stock-modal');
    modal.style.display = 'flex';
    modal.onclick = (e) => { if(e.target === modal) closeModal(); }
}

function closeModal() { document.getElementById('stock-modal').style.display = 'none'; }
function formatCurrency(num) { if (num == null) return '₹0.00'; return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(num); }

// --- Management Logic ---
async function fetchStocks() {
    try {
        const res = await fetch(`${API_BASE}/stocks`);
        const stocks = await res.json();
        const select = document.getElementById('manage-stock-select');
        select.innerHTML = '<option value="" disabled selected>Select Stock</option>';
        stocks.forEach(s => {
            const opt = document.createElement('option');
            opt.value = s.symbol; opt.text = `${s.symbol} - ${s.name}`;
            select.appendChild(opt);
        });
    } catch (e) { console.error("Error loading stocks", e); }
}

function syncManageDropdown() {
    const mainSelect = document.getElementById('holder-select');
    const manageSelect = document.getElementById('manage-holder-select');
    if(mainSelect && manageSelect) { manageSelect.innerHTML = mainSelect.innerHTML; manageSelect.value = state.currentHolderId || ""; }
}

async function createHolder() {
    const name = document.getElementById('new-holder-name').value.trim();
    if (!name) return alert("Enter Name");
    try {
        const res = await fetch(`${API_BASE}/holders`, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ name }) });
        if(res.ok) { alert("Created!"); document.getElementById('new-holder-name').value = ""; fetchHolders(); }
    } catch(e) { console.error(e); }
}

async function addTransaction() {
    const holderId = document.getElementById('manage-holder-select').value;
    const symbol = document.getElementById('manage-stock-select').value;
    const qty = document.getElementById('trans-qty').value;
    const price = document.getElementById('trans-price').value;

    if (!holderId || !symbol || !qty || !price) return alert("Fill all fields");

    try {
        const res = await fetch(`${API_BASE}/holdings/add`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ holderId: parseInt(holderId), stockSymbol: symbol, quantity: parseInt(qty), price: parseFloat(price) })
        });
        if(res.ok) { alert("Added!"); if(state.currentHolderId == holderId) refreshData(); }
        else alert("Failed");
    } catch(e) { console.error(e); }
}