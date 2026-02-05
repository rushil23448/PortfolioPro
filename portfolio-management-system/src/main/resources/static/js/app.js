/**
 * PortfolioPro - Portfolio Management Dashboard
 * Enhanced with proper error handling, toast notifications, and all chart implementations
 */

const API_BASE = "";

// DOM Elements
const holderSelect = document.getElementById("holderSelect");
const holderSelectHolding = document.getElementById("holderSelectHolding");
const investedValue = document.getElementById("investedValue");
const currentValue = document.getElementById("currentValue");
const profitLoss = document.getElementById("profitLoss");
const returnPercent = document.getElementById("returnPercent");
const holdingsTable = document.getElementById("holdingsTable");
const profitCard = document.getElementById("profitCard");
const recommendationTable = document.getElementById("recommendationTable");
const toastContainer = document.getElementById("toast-container");

// Chart instances
let pieChart = null;
let barChart = null;
let lineChart = null;
let doughnutChart = null;
let sectorChart = null;
let performanceChart = null;

// Initialize dashboard
document.addEventListener("DOMContentLoaded", () => {
    loadHolders();
    loadRecommendations();
    setupEventListeners();
});

// Setup additional event listeners
function setupEventListeners() {
    // Holder select change for holdings section
    if (holderSelectHolding) {
        holderSelectHolding.addEventListener("change", () => {
            // Sync with main holder select
            if (holderSelect) {
                holderSelect.value = holderSelectHolding.value;
            }
        });
    }
}

// ==================== Toast Notifications ====================

function showToast(message, type = 'info', duration = 4000) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = getToastIcon(type);
    
    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span class="toast-message">${message}</span>
    `;
    
    toastContainer.appendChild(toast);
    
    // Auto remove
    setTimeout(() => {
        toast.style.animation = 'fadeOut 0.3s ease forwards';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

function getToastIcon(type) {
    const icons = {
        success: '✓',
        error: '✗',
        warning: '⚠',
        info: 'ℹ'
    };
    return icons[type] || icons.info;
}

// ==================== Error Handling ====================

async function handleApiError(error, fallbackMessage = 'An error occurred') {
    console.error('API Error:', error);
    
    if (error.response) {
        // Server responded with error
        const { status, data } = error.response;
        
        if (data && data.message) {
            showToast(data.message, 'error');
        } else if (status === 404) {
            showToast('Resource not found', 'error');
        } else if (status === 400) {
            showToast('Invalid request', 'error');
        } else if (status === 500) {
            showToast('Server error. Please try again later.', 'error');
        } else {
            showToast(fallbackMessage, 'error');
        }
    } else if (error.request) {
        // Request made but no response
        showToast('Unable to connect to server. Please check your connection.', 'error');
    } else {
        showToast(fallbackMessage, 'error');
    }
}

// ==================== Navigation ====================

function showSection(sectionName) {
    // Hide all sections
    const sections = document.querySelectorAll('.section');
    sections.forEach(section => {
        section.style.display = 'none';
        section.classList.remove('active');
    });
    
    // Show selected section
    const selectedSection = document.getElementById(`${sectionName}-section`);
    if (selectedSection) {
        selectedSection.style.display = 'block';
        selectedSection.classList.add('active');
    }
    
    // Load analytics if showing analytics section
    if (sectionName === 'analytics') {
        loadAnalytics();
    }
    
    // Update sidebar active state
    const sidebarItems = document.querySelectorAll('.sidebar li');
    sidebarItems.forEach(item => {
        item.classList.remove('active');
    });
    
    // Find and activate the clicked item
    const activeItem = Array.from(sidebarItems).find(item => 
        item.textContent.toLowerCase().includes(sectionName.toLowerCase()) ||
        (sectionName === 'holders' && item.textContent.includes('Holder')) ||
        (sectionName === 'holdings' && item.textContent.includes('Holding'))
    );
    if (activeItem) {
        activeItem.classList.add('active');
    }
}

// ==================== API Functions ====================

async function fetchWithTimeout(url, options = {}, timeout = 10000) {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), timeout);
    
    try {
        const response = await fetch(url, {
            ...options,
            signal: controller.signal
        });
        clearTimeout(id);
        return response;
    } catch (error) {
        clearTimeout(id);
        if (error.name === 'AbortError') {
            throw new Error('Request timed out');
        }
        throw error;
    }
}

async function safeFetch(url, options = {}) {
    try {
        const response = await fetchWithTimeout(url, options);
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw {
                response: {
                    status: response.status,
                    data: errorData
                }
            };
        }
        
        return await response.json();
    } catch (error) {
        throw error;
    }
}

// ==================== Analytics ====================

async function loadAnalytics() {
    const holderId = holderSelect?.value;
    if (!holderId) {
        updateAnalyticsDisplay({
            totalHoldings: 0,
            uniqueStocks: 0,
            averageReturn: 0,
            bestPerformer: '-',
            diversificationScore: 0,
            riskScore: 0
        });
        return;
    }
    
    try {
        const analytics = await safeFetch(`${API_BASE}/portfolio/analytics/${holderId}`);
        updateAnalyticsDisplay(analytics);
        
        // Update sector chart
        if (analytics.sectorAllocation) {
            const sectorData = Object.entries(analytics.sectorAllocation).map(([sector, value]) => ({
                sector,
                value
            }));
            loadSectorChart(sectorData);
        }
        
        // Update performance chart (simulated data)
        loadPerformanceChart(analytics);
        
    } catch (error) {
        handleApiError(error, 'Error loading analytics');
    }
}

function updateAnalyticsDisplay(analytics) {
    const elements = {
        totalHoldings: document.getElementById('totalHoldings'),
        uniqueStocks: document.getElementById('uniqueStocks'),
        avgReturn: document.getElementById('avgReturn'),
        bestPerformer: document.getElementById('bestPerformer'),
        diversificationScore: document.getElementById('diversificationScore'),
        riskScore: document.getElementById('riskScore')
    };
    
    if (elements.totalHoldings) elements.totalHoldings.textContent = analytics.totalHoldings || 0;
    if (elements.uniqueStocks) elements.uniqueStocks.textContent = analytics.uniqueStocks || 0;
    if (elements.avgReturn) elements.avgReturn.textContent = (analytics.averageReturn || 0).toFixed(2) + '%';
    if (elements.bestPerformer) elements.bestPerformer.textContent = analytics.bestPerformer || '-';
    if (elements.diversificationScore) elements.diversificationScore.textContent = analytics.diversificationScore || 0;
    if (elements.riskScore) elements.riskScore.textContent = analytics.riskScore || 0;
}

// ==================== Sector Chart ====================

function loadSectorChart(sectorData) {
    const ctx = document.getElementById("sectorChart");
    if (!ctx) return;
    
    // Destroy existing chart
    if (sectorChart) {
        sectorChart.destroy();
    }
    
    if (!sectorData || sectorData.length === 0) {
        return;
    }
    
    const colors = [
        '#3b82f6', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6',
        '#06b6d4', '#ec4899', '#14b8a6', '#f97316', '#6366f1'
    ];
    
    sectorChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: sectorData.map(d => d.sector),
            datasets: [{
                label: 'Value by Sector (₹)',
                data: sectorData.map(d => d.value),
                backgroundColor: colors.slice(0, sectorData.length),
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `₹${context.raw.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '₹' + value.toLocaleString();
                        }
                    }
                }
            }
        }
    });
}

// ==================== Performance Chart ====================

function loadPerformanceChart(analytics) {
    const ctx = document.getElementById("performanceChart");
    if (!ctx) return;
    
    if (performanceChart) {
        performanceChart.destroy();
    }
    
    // Generate simulated performance data based on analytics
    const labels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];
    const invested = analytics.totalInvested || 100000;
    const current = analytics.currentValue || invested * 1.1;
    
    const investedData = labels.map((_, i) => invested * (1 + (i * 0.02)));
    const currentData = labels.map((_, i) => current * (1 + (Math.random() - 0.5) * 0.1));
    
    performanceChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Invested Value',
                    data: investedData,
                    borderColor: '#64748b',
                    backgroundColor: 'rgba(100, 116, 139, 0.1)',
                    fill: true,
                    tension: 0.4
                },
                {
                    label: 'Current Value',
                    data: currentData,
                    borderColor: '#22c55e',
                    backgroundColor: 'rgba(34, 197, 94, 0.1)',
                    fill: true,
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.dataset.label}: ₹${context.raw.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    ticks: {
                        callback: function(value) {
                            return '₹' + value.toLocaleString();
                        }
                    }
                }
            }
        }
    });
}

// ==================== Holders ====================

async function loadHolders() {
    try {
        const holders = await safeFetch(`${API_BASE}/holders`);
        
        if (holderSelect) {
            holderSelect.innerHTML = "";
        }
        if (holderSelectHolding) {
            holderSelectHolding.innerHTML = "";
        }
        
        // Add default option
        if (holderSelect) {
            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = "Select a holder...";
            holderSelect.appendChild(defaultOption);
        }
        if (holderSelectHolding) {
            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = "Select a holder...";
            holderSelectHolding.appendChild(defaultOption);
        }
        
        if (holders.length === 0) {
            if (holderSelect) {
                const option = document.createElement("option");
                option.value = "";
                option.textContent = "No holders - Add one below";
                holderSelect.appendChild(option);
            }
            return;
        }
        
        holders.forEach(holder => {
            if (holderSelect) {
                const option = document.createElement("option");
                option.value = holder.id;
                option.textContent = holder.name;
                holderSelect.appendChild(option);
            }
            if (holderSelectHolding) {
                const option = document.createElement("option");
                option.value = holder.id;
                option.textContent = holder.name;
                holderSelectHolding.appendChild(option);
            }
        });
        
        // Load portfolio for first holder
        if (holders.length > 0 && holderSelect) {
            holderSelect.value = holders[0].id;
            loadPortfolio(holders[0].id);
        }
        
    } catch (error) {
        handleApiError(error, 'Error loading holders');
    }
}

// ==================== Recommendations ====================

async function loadRecommendations() {
    try {
        const stocks = await safeFetch(`${API_BASE}/stocks`);
        
        // Sort by confidence score and get top 5
        const top5 = stocks
            .sort((a, b) => b.confidenceScore - a.confidenceScore)
            .slice(0, 5);
        
        if (recommendationTable) {
            recommendationTable.innerHTML = "";
            
            if (top5.length === 0) {
                recommendationTable.innerHTML = 
                    `<tr><td colspan="6">No recommendations available</td></tr>`;
                return;
            }
            
            top5.forEach(stock => {
                const confidenceClass = stock.confidenceScore >= 90 ? 'confidence-high' :
                    stock.confidenceScore >= 75 ? 'confidence-medium' : 'confidence-low';
                
                const action = stock.confidenceScore >= 85 ? 'BUY' : 
                    stock.confidenceScore >= 70 ? 'HOLD' : 'WATCH';
                
                const actionClass = action === 'BUY' ? 'buy' : action === 'SELL' ? 'sell' : '';
                
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td><strong>${escapeHtml(stock.symbol)}</strong></td>
                    <td>${escapeHtml(stock.name || 'N/A')}</td>
                    <td>${escapeHtml(stock.sector || 'N/A')}</td>
                    <td>₹${stock.basePrice?.toFixed(2) || '0.00'}</td>
                    <td><span class="${confidenceClass}">${stock.confidenceScore}%</span></td>
                    <td><button class="action-btn ${actionClass}">${action}</button></td>
                `;
                recommendationTable.appendChild(row);
            });
        }
        
    } catch (error) {
        handleApiError(error, 'Error loading recommendations');
        if (recommendationTable) {
            recommendationTable.innerHTML = 
                `<tr><td colspan="6">Error loading recommendations</td></tr>`;
        }
    }
}

// ==================== Portfolio ====================

async function loadPortfolio(holderId) {
    if (!holderId) {
        resetPortfolioDisplay();
        return;
    }
    
    try {
        // Load portfolio summary
        const summary = await safeFetch(`${API_BASE}/portfolio/summary/${holderId}`);
        
        updatePortfolioSummary(summary);
        
        // Load holdings
        const holdings = await safeFetch(`${API_BASE}/holdings/${holderId}`);
        updateHoldingsTable(holdings);
        
        // Update charts
        updatePortfolioCharts(holdings);
        
    } catch (error) {
        handleApiError(error, 'Error loading portfolio');
    }
}

function resetPortfolioDisplay() {
    if (investedValue) investedValue.textContent = "₹0";
    if (currentValue) currentValue.textContent = "₹0";
    if (profitLoss) profitLoss.textContent = "₹0";
    if (returnPercent) returnPercent.textContent = "0%";
    if (holdingsTable) holdingsTable.innerHTML = "";
    
    if (pieChart) {
        pieChart.destroy();
        pieChart = null;
    }
    if (barChart) {
        barChart.destroy();
        barChart = null;
    }
    if (lineChart) {
        lineChart.destroy();
        lineChart = null;
    }
    if (doughnutChart) {
        doughnutChart.destroy();
        doughnutChart = null;
    }
}

function updatePortfolioSummary(summary) {
    if (investedValue) {
        investedValue.textContent = "₹" + (summary.totalInvested || 0).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
    }
    if (currentValue) {
        currentValue.textContent = "₹" + (summary.currentValue || 0).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
    }
    if (profitLoss) {
        const pl = summary.profitLoss || 0;
        profitLoss.textContent = "₹" + pl.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
        profitLoss.className = pl >= 0 ? 'profit' : 'loss';
    }
    if (returnPercent) {
        const ret = summary.averageReturn || 0;
        returnPercent.textContent = (ret >= 0 ? '+' : '') + ret.toFixed(2) + '%';
        returnPercent.style.color = ret >= 0 ? '#22c55e' : '#ef4444';
    }
    if (profitCard) {
        profitCard.style.borderLeft = (summary.profitLoss || 0) >= 0 ? '6px solid #22c55e' : '6px solid #ef4444';
    }
}

function updateHoldingsTable(holdings) {
    if (!holdingsTable) return;
    
    holdingsTable.innerHTML = "";
    
    if (!holdings || holdings.length === 0) {
        holdingsTable.innerHTML = 
            `<tr><td colspan="6">No holdings yet - Add your first holding!</td></tr>`;
        return;
    }
    
    holdings.forEach(h => {
        const stock = h.stock || {};
        const symbol = h.stockSymbol || stock.symbol || 'N/A';
        const quantity = h.quantity || 0;
        const avgPrice = h.avgPrice || 0;
        const currentPrice = stock.currentPrice || avgPrice;
        const value = quantity * currentPrice;
        const pl = (currentPrice - avgPrice) * quantity;
        const plClass = pl >= 0 ? 'profit' : 'loss';
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${escapeHtml(symbol)}</strong></td>
            <td>${quantity}</td>
            <td>₹${avgPrice.toFixed(2)}</td>
            <td>₹${currentPrice.toFixed(2)}</td>
            <td>₹${value.toFixed(2)}</td>
            <td class="${plClass}">${pl >= 0 ? '+' : ''}₹${pl.toFixed(2)}</td>
        `;
        holdingsTable.appendChild(row);
    });
}

function updatePortfolioCharts(holdings) {
    if (!holdings || holdings.length === 0) {
        return;
    }
    
    const labels = [];
    const investedValues = [];
    const currentValues = [];
    const quantities = [];
    
    holdings.forEach(h => {
        const stock = h.stock || {};
        const symbol = h.stockSymbol || stock.symbol || 'Unknown';
        const quantity = h.quantity || 0;
        const avgPrice = h.avgPrice || 0;
        const currentPrice = stock.currentPrice || avgPrice;
        
        labels.push(symbol);
        quantities.push(quantity * currentPrice);
        investedValues.push(quantity * avgPrice);
        currentValues.push(quantity * currentPrice);
    });
    
    // Load Pie Chart
    loadPieChart(labels, quantities);
    
    // Load Bar Chart
    loadBarChart(labels, quantities);
    
    // Load Line Chart
    loadLineChart(labels, investedValues, currentValues);
    
    // Load Doughnut Chart
    loadDoughnutChart(labels, quantities);
}

// ==================== Pie Chart ====================

function loadPieChart(labels, data) {
    const ctx = document.getElementById("pieChart");
    if (!ctx) return;
    
    if (pieChart) {
        pieChart.destroy();
    }
    
    if (labels.length === 0) {
        return;
    }
    
    const colors = [
        '#3b82f6', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6',
        '#06b6d4', '#ec4899', '#14b8a6', '#f97316', '#6366f1'
    ];
    
    pieChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors.slice(0, labels.length),
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        padding: 15,
                        usePointStyle: true,
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = context.raw;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return `${context.label}: ₹${value.toFixed(2)} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// ==================== Bar Chart ====================

function loadBarChart(labels, data) {
    const ctx = document.getElementById("barChart");
    if (!ctx) return;
    
    if (barChart) {
        barChart.destroy();
    }
    
    if (labels.length === 0) {
        return;
    }
    
    const colors = [
        '#3b82f6', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6',
        '#06b6d4', '#ec4899', '#14b8a6', '#f97316', '#6366f1'
    ];
    
    barChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Portfolio Value (₹)',
                data: data,
                backgroundColor: colors.slice(0, labels.length),
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `₹${context.raw.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '₹' + value.toLocaleString();
                        }
                    }
                }
            }
        }
    });
}

// ==================== Line Chart ====================

function loadLineChart(labels, investedData, currentData) {
    const ctx = document.getElementById("lineChart");
    if (!ctx) return;
    
    if (lineChart) {
        lineChart.destroy();
    }
    
    if (labels.length === 0) {
        return;
    }
    
    lineChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Invested',
                    data: investedData,
                    borderColor: '#64748b',
                    backgroundColor: 'rgba(100, 116, 139, 0.1)',
                    fill: true,
                    tension: 0.4
                },
                {
                    label: 'Current',
                    data: currentData,
                    borderColor: '#22c55e',
                    backgroundColor: 'rgba(34, 197, 94, 0.1)',
                    fill: true,
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.dataset.label}: ₹${context.raw.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    ticks: {
                        callback: function(value) {
                            return '₹' + value.toLocaleString();
                        }
                    }
                }
            }
        }
    });
}

// ==================== Doughnut Chart ====================

function loadDoughnutChart(labels, data) {
    const ctx = document.getElementById("doughnutChart");
    if (!ctx) return;
    
    if (doughnutChart) {
        doughnutChart.destroy();
    }
    
    if (labels.length === 0) {
        return;
    }
    
    const colors = [
        '#3b82f6', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6',
        '#06b6d4', '#ec4899', '#14b8a6', '#f97316', '#6366f1'
    ];
    
    doughnutChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors.slice(0, labels.length),
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            cutout: '60%',
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        padding: 15,
                        usePointStyle: true,
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = context.raw;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return `${context.label}: ₹${value.toFixed(2)} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// ==================== Holder Management ====================

async function addHolder() {
    const name = document.getElementById("holderName")?.value.trim();
    const email = document.getElementById("holderEmail")?.value.trim();
    
    if (!name) {
        showToast('Please enter a holder name!', 'warning');
        return;
    }
    
    try {
        const res = await fetch(`${API_BASE}/holders/add`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email: email || null })
        });
        
        if (res.ok) {
            showToast('Holder added successfully!', 'success');
            
            // Clear form
            const nameInput = document.getElementById("holderName");
            const emailInput = document.getElementById("holderEmail");
            if (nameInput) nameInput.value = "";
            if (emailInput) emailInput.value = "";
            
            // Reload holders
            await loadHolders();
            
            // Switch to dashboard
            showSection('dashboard');
        } else {
            const error = await res.json().catch(() => ({}));
            showToast(error.message || 'Failed to add holder!', 'error');
        }
    } catch (error) {
        handleApiError(error, 'Error adding holder');
    }
}

// ==================== Holding Management ====================

async function addHolding() {
    const holderId = holderSelect?.value || holderSelectHolding?.value;
    const stockSymbol = document.getElementById("stockSymbol")?.value.trim().toUpperCase();
    const quantityInput = document.getElementById("quantity");
    const avgPriceInput = document.getElementById("avgPrice");
    const quantity = quantityInput ? parseInt(quantityInput.value) : null;
    const avgPrice = avgPriceInput ? parseFloat(avgPriceInput.value) : null;
    
    if (!holderId) {
        showToast('Please add a holder first!', 'warning');
        return;
    }
    
    if (!stockSymbol || !quantity || !avgPrice) {
        showToast('Please fill in all fields!', 'warning');
        return;
    }
    
    if (quantity < 1) {
        showToast('Quantity must be at least 1!', 'warning');
        return;
    }
    
    if (avgPrice <= 0) {
        showToast('Average price must be positive!', 'warning');
        return;
    }
    
    try {
        const res = await fetch(`${API_BASE}/holdings/add/${holderId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ stockSymbol, quantity, avgPrice })
        });
        
        if (res.ok) {
            showToast('Holding added successfully!', 'success');
            
            // Clear form
            const symbolInput = document.getElementById("stockSymbol");
            if (symbolInput) symbolInput.value = "";
            if (quantityInput) quantityInput.value = "";
            if (avgPriceInput) avgPriceInput.value = "";
            
            // Reload portfolio
            await loadPortfolio(holderId);
            
            // Switch to dashboard
            showSection('dashboard');
        } else {
            const error = await res.json().catch(() => ({}));
            showToast(error.message || 'Failed to add holding!', 'error');
        }
    } catch (error) {
        handleApiError(error, 'Error adding holding');
    }
}

// ==================== Event Handlers ====================

function handleHolderChange() {
    const holderId = holderSelect?.value;
    loadPortfolio(holderId);
    
    // Sync with holding select
    if (holderSelectHolding) {
        holderSelectHolding.value = holderId;
    }
}

async function refreshData() {
    const holderId = holderSelect?.value;
    
    showToast('Refreshing data...', 'info', 2000);
    
    await loadHolders();
    await loadRecommendations();
    
    if (holderId) {
        await loadPortfolio(holderId);
    }
    
    showToast('Data refreshed!', 'success');
}

// ==================== Utility Functions ====================

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ==================== Initialize Charts ====================

// Make showSection globally accessible
window.showSection = showSection;
window.addHolder = addHolder;
window.addHolding = addHolding;
window.handleHolderChange = handleHolderChange;
window.refreshData = refreshData;

