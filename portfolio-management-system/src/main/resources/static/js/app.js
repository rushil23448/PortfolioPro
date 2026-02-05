/**
 * PortfolioPro - Vanilla JS Frontend
 * Handles API communication, routing, and canvas rendering.
 */

// --- Configuration ---
const API_BASE = "http://localhost:8081/api";
const REFRESH_RATE = 5000; // 5 seconds

// --- State Management ---
let state = {
    holders: [],
    currentHolderId: null,
    holdings: [],
    analytics: {},
    recommendations: [],
    history: [] // Simulating history for line chart
};

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    fetchHolders();
    initTicker(); // ✅ NEW: Start the Stock Ticker

    // Auto-refresh loop
    setInterval(() => {
        if (state.currentHolderId) {
            refreshData();
        }
    }, REFRESH_RATE);

    // Manual refresh
    document.getElementById('refresh-btn').addEventListener('click', refreshData);
});

// --- Navigation Logic ---
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const sections = document.querySelectorAll('.view-section');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            // Update UI State
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');

            // Switch View
            const targetId = `view-${item.dataset.tab}`;
            sections.forEach(sec => sec.classList.remove('active'));
            const targetSection = document.getElementById(targetId);
            if (targetSection) targetSection.classList.add('active');

            // Trigger specific renders if needed
            if (item.dataset.tab === 'dumb-money') renderAISignals();
            if (item.dataset.tab === 'portfolio') renderPortfolio();

            // ✅ Trigger stock loading for Manage Tab
            if (item.dataset.tab === 'manage') {
                fetchStocks();
                syncManageDropdown();
            }
        });
    });
}

// --- Ticker Logic (NEW) ---
async function initTicker() {
    try {
        const res = await fetch(`${API_BASE}/stocks`);
        const stocks = await res.json();

        const track = document.getElementById('ticker-track');
        if (!track) return; // Guard clause

        track.innerHTML = ''; // Clear loading message

        // Duplicate list for smooth infinite scroll
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

    } catch (e) {
        console.error("Ticker Error:", e);
    }
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

        // Event Listener for selection
        select.addEventListener('change', (e) => {
            state.currentHolderId = e.target.value;
            state.history = []; // Reset history for new user
            refreshData();
        });

    } catch (error) {
        console.error("Error fetching holders:", error);
    }
}

async function refreshData() {
    if (!state.currentHolderId) return;

    try {
        const hid = state.currentHolderId;

        // Parallel fetching
        const [portfolioRes, analyticsRes, divRes] = await Promise.all([
            fetch(`${API_BASE}/portfolio/${hid}`),
            fetch(`${API_BASE}/${hid}/analytics`),
            fetch(`${API_BASE}/${hid}/diversification`)
        ]);

        state.holdings = await portfolioRes.json();
        state.analytics = await analyticsRes.json();
        state.recommendations = await divRes.json();

        // Update Global State History for Line Chart
        state.history.push({
            time: new Date().toLocaleTimeString(),
            value: state.analytics.currentValue
        });
        if (state.history.length > 20) state.history.shift(); // Keep last 20 points

        // Update UI
        updateDashboard();

    } catch (error) {
        console.error("Sync Error:", error);
    }
}

// --- DOM Updating ---

function updateDashboard() {
    const a = state.analytics;
    document.getElementById('kpi-invested').innerText = formatCurrency(a.totalInvested);
    document.getElementById('kpi-current').innerText = formatCurrency(a.currentValue);

    const pnlEl = document.getElementById('kpi-pnl');
    pnlEl.innerText = formatCurrency(a.profitLoss);
    pnlEl.className = a.profitLoss >= 0 ? 'pnl-pos' : 'pnl-neg';

    const pnlPct = (a.profitLoss / a.totalInvested) * 100;
    document.getElementById('kpi-pnl-percent').innerText = `${pnlPct.toFixed(2)}%`;

    document.getElementById('kpi-risk').innerText = `${a.riskScore}/100`;
    document.getElementById('kpi-risk-bar').style.width = `${a.riskScore}%`;

    renderTable();
    drawPieChart();
    drawLineChart(); // This now calls the updated function with scales
    drawBarChart();
    drawGaugeChart();
    renderDiversification();
    renderAISignals();
    renderPortfolio();
}

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
        const plPercent = (pl / investedVal) * 100;

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
                <td>
                    <button class="icon-btn" style="font-size:0.8rem">👁️</button>
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
                <div class="msg">
                    <strong>${rec.suggestedSector}</strong>: ${rec.message}
                </div>
                <div class="tag">${rec.severity} Priority</div>
            </div>
        `;
        container.innerHTML += html;
    });
}

function renderAISignals() {
    const container = document.getElementById('ai-signals-grid');
    if (!container) return;
    container.innerHTML = '';

    state.holdings.forEach(h => {
        const s = h.stock;
        let signal = "NEUTRAL";
        let typeClass = "neutral";
        let reason = "Market Perform";
        let emoji = "😐";

        if (s.volatility < 0.3 && s.confidenceScore > 80) {
            signal = "SMART MONEY";
            typeClass = "smart";
            reason = "High confidence, stable volatility.";
            emoji = "🧠";
        } else if (s.volatility > 0.4 || s.confidenceScore < 50) {
            signal = "DUMB MONEY";
            typeClass = "dumb";
            reason = "High volatility, low confidence. Hype risk.";
            emoji = "🤡";
        }

        const card = `
            <div class="signal-card ${typeClass}">
                <div class="signal-header">
                    <span>${s.symbol}</span>
                    <span>${emoji}</span>
                </div>
                <div class="signal-body">
                    <h2>${signal}</h2>
                    <div class="signal-details">${reason}</div>
                    <small>Conf: ${s.confidenceScore} | Vol: ${s.volatility}</small>
                </div>
            </div>
        `;
        container.innerHTML += card;
    });
}

// --- Management Logic (Corrected) ---

async function fetchStocks() {
    try {
        const res = await fetch(`${API_BASE}/stocks`);
        const stocks = await res.json();
        const select = document.getElementById('manage-stock-select');
        select.innerHTML = '<option value="" disabled selected>Select Stock</option>';

        stocks.forEach(s => {
            const opt = document.createElement('option');
            // ✅ CORRECTED: Use 's.symbol' (String) instead of 's.id'
            opt.value = s.symbol;
            opt.text = `${s.symbol} - ${s.name}`;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error("Error loading stocks", e);
    }
}

function syncManageDropdown() {
    const mainSelect = document.getElementById('holder-select');
    const manageSelect = document.getElementById('manage-holder-select');
    if(mainSelect && manageSelect) {
        manageSelect.innerHTML = mainSelect.innerHTML;
        manageSelect.value = state.currentHolderId || "";
    }
}

async function createHolder() {
    const nameInput = document.getElementById('new-holder-name');
    const name = nameInput.value.trim();
    if (!name) return alert("Please enter a name");

    try {
        const res = await fetch(`${API_BASE}/holders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: name })
        });

        if (res.ok) {
            alert("Investor created successfully!");
            nameInput.value = "";
            fetchHolders(); // Refresh global list
        } else {
            alert("Failed to create investor");
        }
    } catch (e) {
        console.error(e);
        alert("Error creating investor");
    }
}

async function addTransaction() {
    const holderId = document.getElementById('manage-holder-select').value;
    const stockSymbol = document.getElementById('manage-stock-select').value;
    const qty = document.getElementById('trans-qty').value;
    const price = document.getElementById('trans-price').value;

    if (!holderId || !stockSymbol || !qty || !price) {
        return alert("Please fill all fields");
    }

    const payload = {
        holderId: parseInt(holderId),
        stockSymbol: stockSymbol, // ✅ CORRECTED: Sending symbol as string
        quantity: parseInt(qty),
        price: parseFloat(price)
    };

    try {
        const res = await fetch(`${API_BASE}/holdings/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert("Transaction Added!");
            // Refresh logic if we modified current user
            if (state.currentHolderId == holderId) {
                refreshData();
            }
        } else {
            const err = await res.text();
            console.error("Failed:", err);
            alert("Failed to add transaction. Check console.");
        }
    } catch (e) {
        console.error(e);
        alert("Error adding transaction");
    }
}

// --- Canvas Charting Helpers (Updated with Scales) ---

function drawPieChart() {
    const canvas = document.getElementById('pieChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const allocation = state.analytics.sectorAllocation;
    if (!allocation) return;

    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = 300;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const colors = ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#64748b'];
    let startAngle = 0;
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = 100;

    const total = Object.values(allocation).reduce((a, b) => a + b, 0);
    let i = 0;

    for (const [sector, value] of Object.entries(allocation)) {
        const sliceAngle = (value / total) * 2 * Math.PI;

        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
        ctx.fillStyle = colors[i % colors.length];
        ctx.fill();

        // Legend text
        const midAngle = startAngle + sliceAngle / 2;
        const textX = centerX + (radius + 20) * Math.cos(midAngle);
        const textY = centerY + (radius + 20) * Math.sin(midAngle);

        ctx.fillStyle = '#333';
        ctx.font = '10px Inter';
        ctx.fillText(sector, textX - 10, textY);

        startAngle += sliceAngle;
        i++;
    }
}

// ✅ REPLACED with Advanced Chart (Axes + Grid + Gradient)
function drawLineChart() {
    const canvas = document.getElementById('lineChart');
    if (!canvas || state.history.length < 2) return;
    const ctx = canvas.getContext('2d');

    // 1. Setup Dimensions & Margins
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = 300;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const margin = { top: 20, right: 30, bottom: 40, left: 50 };
    const chartW = canvas.width - margin.left - margin.right;
    const chartH = canvas.height - margin.top - margin.bottom;

    // 2. Calculate Min/Max with buffer
    const values = state.history.map(d => d.value);
    let minVal = Math.min(...values);
    let maxVal = Math.max(...values);

    // Add 5% buffer so line doesn't touch edges
    let range = maxVal - minVal;
    if (range === 0) range = 100;
    minVal -= range * 0.05;
    maxVal += range * 0.05;
    range = maxVal - minVal;

    // Helpers
    const getX = (i) => margin.left + (i / (state.history.length - 1)) * chartW;
    const getY = (val) => margin.top + chartH - ((val - minVal) / range) * chartH;

    // 3. Draw Grid & Y-Axis Labels
    ctx.font = '11px Inter';
    ctx.fillStyle = '#64748b';
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = 1;

    const ySteps = 5;
    for (let i = 0; i <= ySteps; i++) {
        const val = minVal + (range * (i / ySteps));
        const y = getY(val);

        ctx.beginPath();
        ctx.moveTo(margin.left, y);
        ctx.lineTo(margin.left + chartW, y);
        ctx.stroke();

        ctx.textAlign = 'right';
        ctx.fillText(formatCurrencyShort(val), margin.left - 10, y + 4);
    }

    // 4. Draw X-Axis Labels (Time)
    const xLabelCount = 6;
    const stepSize = Math.max(1, Math.floor((state.history.length - 1) / (xLabelCount - 1)));

    for (let i = 0; i < state.history.length; i += stepSize) {
        const pt = state.history[i];
        const x = getX(i);
        ctx.textAlign = 'center';
        ctx.fillText(pt.time, x, margin.top + chartH + 20);
    }

    // 5. Draw Data Line
    ctx.beginPath();
    ctx.strokeStyle = '#2563eb';
    ctx.lineWidth = 2.5;
    ctx.lineJoin = 'round';

    state.history.forEach((point, index) => {
        const x = getX(index);
        const y = getY(point.value);
        if (index === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
    });
    ctx.stroke();

    // 6. Gradient Fill
    const gradient = ctx.createLinearGradient(0, margin.top, 0, canvas.height);
    gradient.addColorStop(0, 'rgba(37, 99, 235, 0.15)');
    gradient.addColorStop(1, 'rgba(37, 99, 235, 0.0)');

    ctx.lineTo(getX(state.history.length - 1), margin.top + chartH);
    ctx.lineTo(margin.left, margin.top + chartH);
    ctx.closePath();
    ctx.fillStyle = gradient;
    ctx.fill();
}

function drawBarChart() {
    const canvas = document.getElementById('barChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = 250;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const bins = { '0-50': 0, '51-75': 0, '76-100': 0 };
    if (state.holdings) {
        state.holdings.forEach(h => {
            const c = h.stock.confidenceScore;
            if (c <= 50) bins['0-50']++;
            else if (c <= 75) bins['51-75']++;
            else bins['76-100']++;
        });
    }

    const keys = Object.keys(bins);
    const barWidth = 60;
    const gap = 40;
    const startX = (canvas.width - (keys.length * (barWidth + gap))) / 2;

    keys.forEach((key, i) => {
        const val = bins[key];
        const h = val * 20;
        const x = startX + i * (barWidth + gap);
        const y = canvas.height - h - 30;

        ctx.fillStyle = i === 2 ? '#10b981' : (i === 1 ? '#f59e0b' : '#ef4444');
        ctx.fillRect(x, y, barWidth, h);

        ctx.fillStyle = '#333';
        ctx.fillText(key, x + 10, canvas.height - 10);
        ctx.fillText(val, x + 25, y - 5);
    });
}

function drawGaugeChart() {
    const canvas = document.getElementById('gaugeChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = 250;

    const x = canvas.width / 2;
    const y = canvas.height - 50;
    const r = 80;
    const score = state.analytics.riskScore || 0;

    // Background Arc
    ctx.beginPath();
    ctx.arc(x, y, r, Math.PI, 2 * Math.PI);
    ctx.lineWidth = 20;
    ctx.strokeStyle = '#e2e8f0';
    ctx.stroke();

    // Value Arc
    const angle = Math.PI + (score / 100) * Math.PI;
    ctx.beginPath();
    ctx.arc(x, y, r, Math.PI, angle);
    ctx.strokeStyle = score > 70 ? '#ef4444' : (score > 40 ? '#f59e0b' : '#10b981');
    ctx.stroke();

    // Text
    ctx.fillStyle = '#333';
    ctx.font = 'bold 20px Inter';
    ctx.fillText(`${score}`, x - 10, y - 20);
    ctx.font = '12px Inter';
    ctx.fillText("Risk Level", x - 30, y + 20);
}

// --- Utils ---
function formatCurrency(num) {
    if (num === undefined || num === null) return '₹0.00';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(num);
}

// ✅ NEW HELPER for Chart Labels
function formatCurrencyShort(num) {
    if (num >= 10000000) return '₹' + (num / 10000000).toFixed(2) + 'Cr';
    if (num >= 100000) return '₹' + (num / 100000).toFixed(2) + 'L';
    if (num >= 1000) return '₹' + (num / 1000).toFixed(1) + 'k';
    return '₹' + Math.round(num).toLocaleString();
}