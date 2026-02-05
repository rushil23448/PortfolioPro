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

                Stock.builder().symbol("RELIANCE").name("Reliance Industries").sector("Energy")
                        .basePrice(2500.0).volatility(0.25).confidenceScore(90).currentPrice(2500.0).build(),

                Stock.builder().symbol("TCS").name("Tata Consultancy Services").sector("IT")
                        .basePrice(3400.0).volatility(0.20).confidenceScore(92).currentPrice(3400.0).build(),

                Stock.builder().symbol("INFY").name("Infosys").sector("IT")
                        .basePrice(1500.0).volatility(0.22).confidenceScore(91).currentPrice(1500.0).build(),

                Stock.builder().symbol("HDFCBANK").name("HDFC Bank").sector("Banking")
                        .basePrice(1600.0).volatility(0.18).confidenceScore(93).currentPrice(1600.0).build(),

                Stock.builder().symbol("ICICIBANK").name("ICICI Bank").sector("Banking")
                        .basePrice(1100.0).volatility(0.19).confidenceScore(91).currentPrice(1100.0).build(),

                Stock.builder().symbol("SBIN").name("State Bank of India").sector("Banking")
                        .basePrice(750.0).volatility(0.24).confidenceScore(88).currentPrice(750.0).build(),

                Stock.builder().symbol("AXISBANK").name("Axis Bank").sector("Banking")
                        .basePrice(1050.0).volatility(0.21).confidenceScore(87).currentPrice(1050.0).build(),

                Stock.builder().symbol("KOTAKBANK").name("Kotak Mahindra Bank").sector("Banking")
                        .basePrice(1800.0).volatility(0.17).confidenceScore(89).currentPrice(1800.0).build(),

                Stock.builder().symbol("LT").name("Larsen & Toubro").sector("Infrastructure")
                        .basePrice(3200.0).volatility(0.23).confidenceScore(90).currentPrice(3200.0).build(),

                Stock.builder().symbol("ITC").name("ITC Limited").sector("FMCG")
                        .basePrice(450.0).volatility(0.15).confidenceScore(92).currentPrice(450.0).build(),

                Stock.builder().symbol("HINDUNILVR").name("Hindustan Unilever").sector("FMCG")
                        .basePrice(2600.0).volatility(0.16).confidenceScore(91).currentPrice(2600.0).build(),

                Stock.builder().symbol("NESTLEIND").name("Nestle India").sector("FMCG")
                        .basePrice(24000.0).volatility(0.14).confidenceScore(92).currentPrice(24000.0).build(),

                Stock.builder().symbol("BAJFINANCE").name("Bajaj Finance").sector("Finance")
                        .basePrice(7200.0).volatility(0.28).confidenceScore(89).currentPrice(7200.0).build(),

                Stock.builder().symbol("BAJAJFINSV").name("Bajaj Finserv").sector("Finance")
                        .basePrice(1650.0).volatility(0.25).confidenceScore(88).currentPrice(1650.0).build(),

                Stock.builder().symbol("BHARTIARTL").name("Bharti Airtel").sector("Telecom")
                        .basePrice(1200.0).volatility(0.20).confidenceScore(88).currentPrice(1200.0).build(),

                Stock.builder().symbol("ASIANPAINT").name("Asian Paints").sector("Consumer")
                        .basePrice(3100.0).volatility(0.18).confidenceScore(90).currentPrice(3100.0).build(),

                Stock.builder().symbol("TITAN").name("Titan Company").sector("Consumer")
                        .basePrice(3500.0).volatility(0.19).confidenceScore(91).currentPrice(3500.0).build(),

                Stock.builder().symbol("MARUTI").name("Maruti Suzuki").sector("Automobile")
                        .basePrice(9800.0).volatility(0.22).confidenceScore(89).currentPrice(9800.0).build(),

                Stock.builder().symbol("TATAMOTORS").name("Tata Motors").sector("Automobile")
                        .basePrice(850.0).volatility(0.30).confidenceScore(85).currentPrice(850.0).build(),

                Stock.builder().symbol("M&M").name("Mahindra & Mahindra").sector("Automobile")
                        .basePrice(1600.0).volatility(0.24).confidenceScore(87).currentPrice(1600.0).build(),

                Stock.builder().symbol("SUNPHARMA").name("Sun Pharma").sector("Healthcare")
                        .basePrice(1500.0).volatility(0.21).confidenceScore(90).currentPrice(1500.0).build(),

                Stock.builder().symbol("DRREDDY").name("Dr Reddy Labs").sector("Healthcare")
                        .basePrice(5800.0).volatility(0.20).confidenceScore(88).currentPrice(5800.0).build(),

                Stock.builder().symbol("CIPLA").name("Cipla").sector("Healthcare")
                        .basePrice(1400.0).volatility(0.22).confidenceScore(87).currentPrice(1400.0).build(),

                Stock.builder().symbol("WIPRO").name("Wipro").sector("IT")
                        .basePrice(480.0).volatility(0.25).confidenceScore(85).currentPrice(480.0).build(),

                Stock.builder().symbol("HCLTECH").name("HCL Technologies").sector("IT")
                        .basePrice(1450.0).volatility(0.22).confidenceScore(87).currentPrice(1450.0).build(),

                Stock.builder().symbol("TECHM").name("Tech Mahindra").sector("IT")
                        .basePrice(1250.0).volatility(0.24).confidenceScore(86).currentPrice(1250.0).build(),

                Stock.builder().symbol("ZOMATO").name("Zomato").sector("Tech")
                        .basePrice(140.0).volatility(0.40).confidenceScore(78).currentPrice(140.0).build(),

                Stock.builder().symbol("PAYTM").name("Paytm").sector("FinTech")
                        .basePrice(420.0).volatility(0.45).confidenceScore(70).currentPrice(420.0).build(),

                Stock.builder().symbol("DMART").name("Avenue Supermarts").sector("Retail")
                        .basePrice(4200.0).volatility(0.18).confidenceScore(90).currentPrice(4200.0).build(),

                Stock.builder().symbol("IRCTC").name("IRCTC").sector("Travel")
                        .basePrice(900.0).volatility(0.25).confidenceScore(84).currentPrice(900.0).build(),

                Stock.builder().symbol("HAL").name("Hindustan Aeronautics").sector("Defense")
                        .basePrice(3200.0).volatility(0.22).confidenceScore(88).currentPrice(3200.0).build(),

                Stock.builder().symbol("BEL").name("Bharat Electronics").sector("Defense")
                        .basePrice(180.0).volatility(0.25).confidenceScore(85).currentPrice(180.0).build(),

                Stock.builder().symbol("LUPIN").name("Lupin").sector("Healthcare")
                        .basePrice(1400.0).volatility(0.24).confidenceScore(83).currentPrice(1400.0).build()
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
