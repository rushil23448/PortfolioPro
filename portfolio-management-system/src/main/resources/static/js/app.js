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

            // Trigger specific renders if needed (like re-animating charts)
            if (item.dataset.tab === 'dumb-money') renderAISignals(); 
        });
    });
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
    // 1. KPI Cards
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

    // 2. Table
    renderTable();

    // 3. Charts
    drawPieChart();
    drawLineChart();
    drawBarChart();
    drawGaugeChart();

    // 4. Diversification
    renderDiversification();

    // 5. AI Signals
    renderAISignals();
}

function renderTable() {
    const tbody = document.querySelector('#dashboard-table tbody');
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

function renderDiversification() {
    const container = document.getElementById('diversification-container');
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

// --- Dumb Money AI Engine (Client Side Logic) ---
/*
 * AI Logic Explanation:
 * We categorize stocks into 'Smart Money', 'Dumb Money', or 'Neutral' based on
 * a combination of Volatility and Confidence Score provided by the backend.
 * * Logic:
 * 1. Smart Money: Low Volatility (< 0.3) AND High Confidence (> 80). Indicates stable growth.
 * 2. Dumb Money (Risky): High Volatility (> 0.4) AND Low Confidence (< 60). Indicates retail hype bubble.
 * 3. Neutral: Anything in between.
 */
function renderAISignals() {
    const container = document.getElementById('ai-signals-grid');
    container.innerHTML = '';

    state.holdings.forEach(h => {
        const s = h.stock;
        let signal = "NEUTRAL";
        let typeClass = "neutral";
        let reason = "Market Perform";
        let emoji = "😐";

        // AI Decision Logic
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


// --- Canvas Charting Helpers (No Libraries) ---

function drawPieChart() {
    const canvas = document.getElementById('pieChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const allocation = state.analytics.sectorAllocation;
    
    // Reset canvas
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

function drawLineChart() {
    const canvas = document.getElementById('lineChart');
    if (!canvas || state.history.length < 2) return;
    const ctx = canvas.getContext('2d');
    
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = 300;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const padding = 40;
    const chartW = canvas.width - padding * 2;
    const chartH = canvas.height - padding * 2;

    // Find min/max for scaling
    const values = state.history.map(d => d.value);
    const minVal = Math.min(...values) * 0.95;
    const maxVal = Math.max(...values) * 1.05;

    ctx.beginPath();
    ctx.strokeStyle = '#2563eb';
    ctx.lineWidth = 2;

    state.history.forEach((point, index) => {
        const x = padding + (index / (state.history.length - 1)) * chartW;
        const y = padding + chartH - ((point.value - minVal) / (maxVal - minVal)) * chartH;
        
        if (index === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
    });
    ctx.stroke();
}

function drawBarChart() {
    // Canvas logic for confidence distribution (Bar Chart)
    const canvas = document.getElementById('barChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = 250;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Bin the data
    const bins = { '0-50': 0, '51-75': 0, '76-100': 0 };
    state.holdings.forEach(h => {
        const c = h.stock.confidenceScore;
        if (c <= 50) bins['0-50']++;
        else if (c <= 75) bins['51-75']++;
        else bins['76-100']++;
    });

    const keys = Object.keys(bins);
    const barWidth = 60;
    const gap = 40;
    const startX = (canvas.width - (keys.length * (barWidth + gap))) / 2;

    keys.forEach((key, i) => {
        const val = bins[key];
        const h = val * 20; // Scale factor
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