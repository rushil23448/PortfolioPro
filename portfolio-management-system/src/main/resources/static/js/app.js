const holderSelect = document.getElementById("holderSelect");
const investedValue = document.getElementById("investedValue");
const currentValue = document.getElementById("currentValue");
const profitLoss = document.getElementById("profitLoss");
const holdingsTable = document.getElementById("holdingsTable");
const profitCard = document.getElementById("profitCard");
const recommendationTable = document.getElementById("recommendationTable");

let pieChart;

// Load Holders
async function loadHolders() {
    holderSelect.innerHTML = "";
    const res = await fetch("/holders");
    const holders = await res.json();

    holders.forEach(holder => {
        const option = document.createElement("option");
        option.value = holder.id;
        option.textContent = holder.name;
        holderSelect.appendChild(option);
    });

    if (holders.length > 0) {
        loadPortfolio(holders[0].id);
    }
}

async function loadRecommendations() {

    recommendationTable.innerHTML = "";

    try {
        const res = await fetch("/stocks/recommendations", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!res.ok) {
            console.error("Failed to fetch recommendations");
            return;
        }

        const data = await res.json();

        // Ensure only TOP 5
        const top5 = data
            .sort((a, b) => b.confidenceScore - a.confidenceScore)
            .slice(0, 5);

        if (top5.length === 0) {
            recommendationTable.innerHTML =
                `<tr><td colspan="6">No recommendations available</td></tr>`;
            return;
        }

        top5.forEach(stock => {

            let confidenceColor =
                stock.confidenceScore >= 90 ? "green" :
                stock.confidenceScore >= 75 ? "orange" : "red";

            recommendationTable.innerHTML += `
                <tr>
                    <td>${stock.symbol}</td>
                    <td>${stock.name}</td>
                    <td>${stock.sector}</td>
                    <td>₹${stock.basePrice.toFixed(2)}</td>
                    <td>${(stock.volatility * 100).toFixed(2)}%</td>
                    <td style="color:${confidenceColor}; font-weight:bold">
                        ${stock.confidenceScore}%
                    </td>
                </tr>
            `;
        });

    } catch (err) {
        console.error("Recommendation error:", err);
    }
}


// Portfolio Summary + Holdings
async function loadPortfolio(holderId) {

    const summaryRes = await fetch(`/portfolio/summary/${holderId}`);
    const summary = await summaryRes.json();

    investedValue.textContent = "₹" + summary.totalInvested.toFixed(2);
    currentValue.textContent = "₹" + summary.currentValue.toFixed(2);
    profitLoss.textContent = "₹" + summary.profitLoss.toFixed(2);

    // Profit Loss Color
    if (summary.profitLoss >= 0) {
        profitCard.style.borderLeft = "6px solid green";
    } else {
        profitCard.style.borderLeft = "6px solid red";
    }

    // Holdings
    const holdingsRes = await fetch(`/holdings/${holderId}`);
    const holdings = await holdingsRes.json();

    holdingsTable.innerHTML = "";
    let labels = [];
    let quantities = [];

    holdings.forEach(h => {
        holdingsTable.innerHTML += `
            <tr>
                <td>${h.stockSymbol}</td>
                <td>${h.quantity}</td>
                <td>₹${h.avgPrice}</td>
            </tr>
        `;

        labels.push(h.stockSymbol);
        quantities.push(h.quantity);
    });

    loadChart(labels, quantities);
}

// Chart
function loadChart(labels, data) {

    const ctx = document.getElementById("pieChart").getContext("2d");

    if (pieChart) pieChart.destroy();

    pieChart = new Chart(ctx, {
        type: "pie",
        data: {
            labels: labels,
            datasets: [{ data: data }]
        }
    });
}

// Add Holder
async function addHolder() {
    const name = document.getElementById("holderName").value;
    const email = document.getElementById("holderEmail").value;

    await fetch("/holders/add", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ name, email })
    });

    alert("Holder Added Successfully ✅");
    loadHolders();
}

// Add Holding
async function addHolding() {

    const holderId = holderSelect.value;
    const stockSymbol = document.getElementById("stockSymbol").value;
    const quantity = document.getElementById("quantity").value;
    const avgPrice = document.getElementById("avgPrice").value;

    await fetch(`/holdings/add/${holderId}`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ stockSymbol, quantity, avgPrice })
    });

    alert("Holding Added Successfully ✅");
    loadPortfolio(holderId);
}

// Dropdown Change
holderSelect.addEventListener("change", () => {
    loadPortfolio(holderSelect.value);
});

// Start
loadHolders();
loadRecommendations();
