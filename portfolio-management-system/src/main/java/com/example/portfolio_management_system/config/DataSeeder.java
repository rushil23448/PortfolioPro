package com.example.portfolio_management_system.config;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HolderRepository;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final StockRepository stockRepository;
    private final HolderRepository holderRepository;
    private final HoldingRepository holdingRepository;

    public DataSeeder(StockRepository stockRepository,
                      HolderRepository holderRepository,
                      HoldingRepository holdingRepository) {
        this.stockRepository = stockRepository;
        this.holderRepository = holderRepository;
        this.holdingRepository = holdingRepository;
    }

    @Override
    public void run(String... args) {

        // ✅ Prevent reseeding again and again
        if (stockRepository.count() > 0) {
            System.out.println("✅ Database already seeded.");
            return;
        }

        System.out.println("🚀 Seeding 100 Indian Stocks + Holders + Holdings...");

        // -------------------------------
        // ✅ 1. Create Holders
        // -------------------------------
        Holder rushil = holderRepository.save(new Holder(null, "Rushil", null));
        Holder shambhavi = holderRepository.save(new Holder(null, "Shambhavi", null));
        Holder shruti = holderRepository.save(new Holder(null, "Shruti", null));
        Holder shivam = holderRepository.save(new Holder(null, "Shivam", null));

        List<Holder> holders = List.of(rushil, shambhavi, shruti, shivam);

        // -------------------------------
        // ✅ 2. Add 100 Real Indian Stocks
        // -------------------------------
        List<Stock> stocks = List.of(

                new Stock("RELIANCE", "Reliance Industries", "Energy", 2500.0, 0.25, 90),
                new Stock("TCS", "Tata Consultancy Services", "IT", 3400.0, 0.20, 92),
                new Stock("INFY", "Infosys", "IT", 1500.0, 0.22, 91),
                new Stock("HDFCBANK", "HDFC Bank", "Banking", 1600.0, 0.18, 93),
                new Stock("ICICIBANK", "ICICI Bank", "Banking", 1100.0, 0.19, 91),
                new Stock("SBIN", "State Bank of India", "Banking", 750.0, 0.24, 88),
                new Stock("AXISBANK", "Axis Bank", "Banking", 1050.0, 0.21, 87),
                new Stock("KOTAKBANK", "Kotak Mahindra Bank", "Banking", 1800.0, 0.17, 89),

                new Stock("LT", "Larsen & Toubro", "Infrastructure", 3200.0, 0.23, 90),
                new Stock("ITC", "ITC Limited", "FMCG", 450.0, 0.15, 92),
                new Stock("HINDUNILVR", "Hindustan Unilever", "FMCG", 2600.0, 0.16, 91),
                new Stock("NESTLEIND", "Nestle India", "FMCG", 24000.0, 0.14, 92),

                new Stock("BAJFINANCE", "Bajaj Finance", "Finance", 7200.0, 0.28, 89),
                new Stock("BAJAJFINSV", "Bajaj Finserv", "Finance", 1650.0, 0.25, 88),
                new Stock("HDFCLIFE", "HDFC Life Insurance", "Insurance", 650.0, 0.20, 87),
                new Stock("SBILIFE", "SBI Life Insurance", "Insurance", 1350.0, 0.19, 88),

                new Stock("BHARTIARTL", "Bharti Airtel", "Telecom", 1200.0, 0.20, 88),
                new Stock("ASIANPAINT", "Asian Paints", "Consumer", 3100.0, 0.18, 90),
                new Stock("TITAN", "Titan Company", "Consumer", 3500.0, 0.19, 91),
                new Stock("MARUTI", "Maruti Suzuki", "Automobile", 9800.0, 0.22, 89),
                new Stock("TATAMOTORS", "Tata Motors", "Automobile", 850.0, 0.30, 85),
                new Stock("M&M", "Mahindra & Mahindra", "Automobile", 1600.0, 0.24, 87),
                new Stock("HEROMOTOCO", "Hero MotoCorp", "Automobile", 4500.0, 0.21, 86),
                new Stock("BAJAJ-AUTO", "Bajaj Auto", "Automobile", 9000.0, 0.20, 88),

                new Stock("SUNPHARMA", "Sun Pharma", "Healthcare", 1500.0, 0.21, 90),
                new Stock("DRREDDY", "Dr Reddy Labs", "Healthcare", 5800.0, 0.20, 88),
                new Stock("CIPLA", "Cipla", "Healthcare", 1400.0, 0.22, 87),
                new Stock("APOLLOHOSP", "Apollo Hospitals", "Healthcare", 6200.0, 0.23, 89),
                new Stock("DIVISLAB", "Divi’s Laboratories", "Healthcare", 3900.0, 0.20, 88),

                new Stock("WIPRO", "Wipro", "IT", 480.0, 0.25, 85),
                new Stock("HCLTECH", "HCL Technologies", "IT", 1450.0, 0.22, 87),
                new Stock("TECHM", "Tech Mahindra", "IT", 1250.0, 0.24, 86),
                new Stock("LTIM", "LTIMindtree", "IT", 5400.0, 0.21, 88),

                new Stock("ULTRACEMCO", "UltraTech Cement", "Cement", 9800.0, 0.20, 89),
                new Stock("SHREECEM", "Shree Cement", "Cement", 26000.0, 0.19, 88),
                new Stock("GRASIM", "Grasim Industries", "Cement", 1850.0, 0.22, 87),
                new Stock("AMBUJACEM", "Ambuja Cement", "Cement", 550.0, 0.23, 86),

                new Stock("ADANIENT", "Adani Enterprises", "Conglomerate", 3200.0, 0.35, 80),
                new Stock("ADANIPORTS", "Adani Ports", "Logistics", 1100.0, 0.30, 82),
                new Stock("ADANIGREEN", "Adani Green Energy", "Renewable", 1800.0, 0.40, 78),

                new Stock("ONGC", "ONGC", "Energy", 250.0, 0.28, 85),
                new Stock("IOC", "Indian Oil", "Energy", 140.0, 0.26, 84),
                new Stock("BPCL", "Bharat Petroleum", "Energy", 450.0, 0.27, 83),

                new Stock("POWERGRID", "Power Grid Corp", "Utilities", 280.0, 0.18, 87),
                new Stock("NTPC", "NTPC Limited", "Utilities", 310.0, 0.19, 88),
                new Stock("TATAPOWER", "Tata Power", "Utilities", 420.0, 0.22, 85),

                new Stock("JSWSTEEL", "JSW Steel", "Metals", 850.0, 0.27, 84),
                new Stock("TATASTEEL", "Tata Steel", "Metals", 145.0, 0.30, 82),
                new Stock("HINDALCO", "Hindalco Industries", "Metals", 520.0, 0.28, 83),

                new Stock("COALINDIA", "Coal India", "Mining", 420.0, 0.20, 86),
                new Stock("VEDL", "Vedanta Limited", "Mining", 260.0, 0.33, 80),

                new Stock("DMART", "Avenue Supermarts", "Retail", 4200.0, 0.18, 90),
                new Stock("TRENT", "Trent Limited", "Retail", 3500.0, 0.25, 85),

                new Stock("PIDILITIND", "Pidilite Industries", "Chemicals", 2800.0, 0.20, 88),
                new Stock("UPL", "UPL Limited", "Chemicals", 650.0, 0.27, 82),
                new Stock("SRF", "SRF Limited", "Chemicals", 2500.0, 0.23, 84),

                new Stock("INDIGO", "IndiGo Airlines", "Aviation", 2400.0, 0.30, 83),
                new Stock("IRCTC", "IRCTC", "Travel", 900.0, 0.25, 84),

                new Stock("ZOMATO", "Zomato", "Tech", 140.0, 0.40, 78),
                new Stock("PAYTM", "Paytm", "FinTech", 420.0, 0.45, 70),
                new Stock("NYKAA", "Nykaa", "E-Commerce", 160.0, 0.38, 76),

                new Stock("DABUR", "Dabur India", "FMCG", 550.0, 0.18, 88),
                new Stock("BRITANNIA", "Britannia Industries", "FMCG", 5200.0, 0.16, 90),
                new Stock("COLPAL", "Colgate Palmolive", "FMCG", 2400.0, 0.15, 89),

                new Stock("BEL", "Bharat Electronics", "Defense", 180.0, 0.25, 85),
                new Stock("HAL", "Hindustan Aeronautics", "Defense", 3200.0, 0.22, 88),

                new Stock("LICI", "LIC India", "Insurance", 850.0, 0.20, 86),
                new Stock("TATAELXSI", "Tata Elxsi", "IT", 8200.0, 0.24, 87),

                new Stock("LUPIN", "Lupin", "Healthcare", 1400.0, 0.24, 83)
        );

        // ✅ Save all stocks
        stockRepository.saveAll(stocks);

        // -------------------------------
        // ✅ 3. Assign Holdings Randomly
        // -------------------------------
        Random random = new Random();

        for (Holder holder : holders) {

            // Shuffle stock list for unique portfolio
            List<Stock> shuffledStocks = new ArrayList<>(stocks);
            Collections.shuffle(shuffledStocks);

            // Each holder gets 5 holdings
            for (int i = 0; i < 5; i++) {

                Stock stock = shuffledStocks.get(i);

                Holding holding = Holding.builder()
                        .holder(holder)
                        .stock(stock)
                        .quantity(5 + random.nextInt(20))
                        .avgPrice(stock.getBasePrice())
                        .build();

                holdingRepository.save(holding);
            }
        }

        System.out.println("✅ Seeding Complete: 100 Stocks + Holders + Holdings Inserted!");
    }
}
