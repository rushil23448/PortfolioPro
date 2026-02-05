
const API_BASE = "";

// DOM Elements
const holderSelect = document.getElementById("holderSelect");
const investedValue = document.getElementById("investedValue");
const currentValue = document.getElementById("currentValue");
const profitLoss = document.getElementById("profitLoss");
const holdingsTable = document.getElementById("holdingsTable");
const profitCard = document.getElementById("profitCard");
const recommendationTable = document.getElementById("recommendationTable");

let pieChart = null;

// Initialize dashboard
document.addEventListener("DOMContentLoaded", () => {
    loadHolders();
    loadRecommendations();
});

// Show Section (Navigation)
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

// Load Analytics Data
async function loadAnalytics() {
    const holderId = holderSelect.value;
    if (!holderId) {
        document.getElementById('totalHoldings').textContent = '0';
        document.getElementById('uniqueStocks').textContent = '0';
        document.getElementById('avgReturn').textContent = '0%';
        document.getElementById('bestPerformer').textContent = '-';
        return;
    }
    
    try {
        const res = await fetch(`${API_BASE}/portfolio/analytics/${holderId}`);
        const analytics = await res.json();
        
        document.getElementById('totalHoldings').textContent = analytics.totalHoldings || 0;
        document.getElementById('uniqueStocks').textContent = analytics.uniqueStocks || 0;
        document.getElementById('avgReturn').textContent = (analytics.averageReturn || 0).toFixed(2) + '%';
        document.getElementById('bestPerformer').textContent = analytics.bestPerformer || '-';
        
        // Convert sectorAllocation map to array for chart
        const sectorData = analytics.sectorAllocation ? 
            Object.entries(analytics.sectorAllocation).map(([sector, value]) => ({ sector, value })) : [];
        
        if (sectorData.length > 0) {
            loadSectorChart(sectorData);
        } else {
            loadSectorChart([]);
        }
    } catch (error) {
        console.error("Error loading analytics:", error);
    }
}

// Load Sector Chart for Analytics
function loadSectorChart(sectorData) {
    const ctx = document.getElementById("sectorChart");
    if (!ctx) return;
    
    // Remove existing chart if any
    const existingChart = Chart.getChart(ctx);
    if (existingChart) {
        existingChart.destroy();
    }
    
    if (sectorData.length === 0) {
        return;
    }
    
    const colors = [
        '#3b82f6', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6',
        '#06b6d4', '#ec4899', '#14b8a6', '#f97316', '#6366f1'
    ];
    
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: sectorData.map(d => d.sector),
            datasets: [{
                label: 'Value by Sector',
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
                            return '₹' + value;
                        }
                    }
                }
            }
        }
    });
}

// Load Holders into dropdown
async function loadHolders() {
    try {
        const res = await fetch(`${API_BASE}/holders`);
        const holders = await res.json();
        
        holderSelect.innerHTML = "";
        
        if (holders.length === 0) {
            // Add default placeholder option
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "No holders - Add one below";
            holderSelect.appendChild(option);
            return;
        }
        
        holders.forEach(holder => {
            const option = document.createElement("option");
            option.value = holder.id;
            option.textContent = holder.name;
            holderSelect.appendChild(option);
        });
        
        // Load portfolio for first holder
        if (holders.length > 0) {
            loadPortfolio(holders[0].id);
        }
    } catch (error) {
        console.error("Error loading holders:", error);
    }
}

// Load Recommendations (Top 5 Stocks by Confidence)
async function loadRecommendations() {
    try {
        const res = await fetch(`${API_BASE}/stocks`);
        const stocks = await res.json();
        
        // Sort by confidence score and get top 5
        const top5 = stocks
            .sort((a, b) => b.confidenceScore - a.confidenceScore)
            .slice(0, 5);
        
        recommendationTable.innerHTML = "";
        
        if (top5.length === 0) {
            recommendationTable.innerHTML = 
                `<tr><td colspan="6">No recommendations available</td></tr>`;
            return;
        }
        
        top5.forEach(stock => {
            let confidenceColor = 
                stock.confidenceScore >= 90 ? "#22c55e" : 
                stock.confidenceScore >= 75 ? "#f59e0b" : "#ef4444";
            
            recommendationTable.innerHTML += `
                <tr>
                    <td><strong>${stock.symbol}</strong></td>
                    <td>${stock.name}</td>
                    <td>${stock.sector}</td>
                    <td>₹${stock.basePrice.toFixed(2)}</td>
                    <td>${(stock.volatility * 100).toFixed(2)}%</td>
                    <td><span style="color:${confidenceColor}; font-weight:bold">${stock.confidenceScore}%</span></td>
                </tr>
            `;
        });
    } catch (error) {
        console.error("Error loading recommendations:", error);
        recommendationTable.innerHTML = 
            `<tr><td colspan="6">Error loading recommendations</td></tr>`;
    }
}

// Load Portfolio Summary and Holdings
async function loadPortfolio(holderId) {
    if (!holderId) {
        investedValue.textContent = "₹0";
        currentValue.textContent = "₹0";
        profitLoss.textContent = "₹0";
        holdingsTable.innerHTML = "";
        if (pieChart) {
            pieChart.destroy();
            pieChart = null;
        }
        return;
    }
    
    try {
        // Load portfolio summary
        const summaryRes = await fetch(`${API_BASE}/portfolio/summary/${holderId}`);
        const summary = await summaryRes.json();
        
        investedValue.textContent = "₹" + (summary.totalInvested || 0).toFixed(2);
        currentValue.textContent = "₹" + (summary.currentValue || 0).toFixed(2);
        profitLoss.textContent = "₹" + (summary.profitLoss || 0).toFixed(2);
        
        // Color based on profit/loss
        const profitLossValue = summary.profitLoss || 0;
        if (profitLossValue >= 0) {
            profitCard.style.borderLeft = "6px solid #22c55e";
            profitCard.querySelector("p").style.color = "#22c55e";
        } else {
            profitCard.style.borderLeft = "6px solid #ef4444";
            profitCard.querySelector("p").style.color = "#ef4444";
        }
        
        // Load holdings
        const holdingsRes = await fetch(`${API_BASE}/holdings/${holderId}`);
        const holdings = await holdingsRes.json();
        
        holdingsTable.innerHTML = "";
        let labels = [];
        let quantities = [];
        
        if (holdings.length === 0) {
            holdingsTable.innerHTML = 
                `<tr><td colspan="3">No holdings yet - Add your first holding!</td></tr>`;
        } else {
            holdings.forEach(h => {
                holdingsTable.innerHTML += `
                    <tr>
                        <td><strong>${h.stockSymbol}</strong></td>
                        <td>${h.quantity}</td>
                        <td>₹${(h.avgPrice || 0).toFixed(2)}</td>
                    </tr>
                `;
                labels.push(h.stockSymbol);
                quantities.push(h.quantity * h.avgPrice);
            });
        }
        
        loadChart(labels, quantities);
        
    } catch (error) {
        console.error("Error loading portfolio:", error);
    }
}

// Load Pie Chart for Diversification
function loadChart(labels, data) {
    const ctx = document.getElementById("pieChart").getContext("2d");
    
    if (pieChart) {
        pieChart.destroy();
    }
    
    if (labels.length === 0) {
        return;
    }
    
    // Generate colors for chart
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
                        usePointStyle: true
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

// Add New Holder
async function addHolder() {
    const name = document.getElementById("holderName").value.trim();
    const email = document.getElementById("holderEmail").value.trim();
    
    if (!name) {
        alert("Please enter a holder name!");
        return;
    }
    
    try {
        const res = await fetch(`${API_BASE}/holders/add`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email })
        });
        
        if (res.ok) {
            alert("Holder added successfully!");
            document.getElementById("holderName").value = "";
            document.getElementById("holderEmail").value = "";
            loadHolders();
        } else {
            alert("Failed to add holder!");
        }
    } catch (error) {
        console.error("Error adding holder:", error);
        alert("Error adding holder. Please try again.");
    }
}

// Add New Holding
async function addHolding() {
    const holderId = holderSelect.value;
    const stockSymbol = document.getElementById("stockSymbol").value.trim().toUpperCase();
    const quantity = parseInt(document.getElementById("quantity").value);
    const avgPrice = parseFloat(document.getElementById("avgPrice").value);
    
    if (!holderId) {
        alert("Please add a holder first!");
        return;
    }
    
    if (!stockSymbol || !quantity || !avgPrice) {
        alert("Please fill in all fields!");
        return;
    }
    
    try {
        const res = await fetch(`${API_BASE}/holdings/add/${holderId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ stockSymbol, quantity, avgPrice })
        });
        
        if (res.ok) {
            alert("Holding added successfully!");
            document.getElementById("stockSymbol").value = "";
            document.getElementById("quantity").value = "";
            document.getElementById("avgPrice").value = "";
            loadPortfolio(holderId);
        } else {
            const error = await res.text();
            alert("Failed to add holding: " + error);
        }
    } catch (error) {
        console.error("Error adding holding:", error);
        alert("Error adding holding. Please try again.");
    }
}

// Handle holder selection change
holderSelect.addEventListener("change", () => {
    loadPortfolio(holderSelect.value);
});

