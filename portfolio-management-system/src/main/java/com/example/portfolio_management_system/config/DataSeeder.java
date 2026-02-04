package com.example.portfolio_management_system.config;

import com.example.portfolio_management_system.model.Holder;
import com.example.portfolio_management_system.model.Holding;
import com.example.portfolio_management_system.model.Stock;
import com.example.portfolio_management_system.repository.HolderRepository;
import com.example.portfolio_management_system.repository.HoldingRepository;
import com.example.portfolio_management_system.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final HolderRepository holderRepository;
    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;

    @Override
    public void run(String... args) {

        // ✅ Prevent duplicate insert
        if (holderRepository.count() > 0) {
            System.out.println("Database already seeded ✅");
            return;
        }

        System.out.println("🚀 Seeding Database with Holders + 100 Stocks + Holdings...");

        // ============================
        // ✅ 1. CREATE HOLDERS
        // ============================
        Holder rushil = holderRepository.save(
                Holder.builder().name("Rushil Shah").email("rushil@gmail.com").build()
        );

        Holder shambhavi = holderRepository.save(
                Holder.builder().name("Shambhavi").email("shambhavi@gmail.com").build()
        );

        Holder shruti = holderRepository.save(
                Holder.builder().name("Shruti").email("shruti@gmail.com").build()
        );

        Holder shivam = holderRepository.save(
                Holder.builder().name("Shivam").email("shivam@gmail.com").build()
        );

        // ============================
        // ✅ 2. INSERT TOP 100 STOCKS
        // ============================
        List<Stock> stocks = List.of(

                // IT Sector
                new Stock("TCS","Tata Consultancy Services","IT",3850.0,0.010,95),
                new Stock("INFY","Infosys","IT",1650.0,0.012,92),
                new Stock("WIPRO","Wipro","IT",520.0,0.015,85),
                new Stock("HCLTECH","HCL Technologies","IT",1250.0,0.013,88),
                new Stock("TECHM","Tech Mahindra","IT",1350.0,0.014,84),
                new Stock("LTIM","LTIMindtree","IT",5200.0,0.012,90),
                new Stock("MPHASIS","Mphasis","IT",2400.0,0.016,82),

                // Banking
                new Stock("HDFCBANK","HDFC Bank","Banking",1500.0,0.011,94),
                new Stock("ICICIBANK","ICICI Bank","Banking",1050.0,0.013,91),
                new Stock("SBIN","State Bank of India","Banking",720.0,0.020,83),
                new Stock("AXISBANK","Axis Bank","Banking",980.0,0.018,85),
                new Stock("KOTAKBANK","Kotak Bank","Banking",1750.0,0.012,88),
                new Stock("INDUSINDBK","IndusInd Bank","Banking",1450.0,0.017,80),
                new Stock("BANKBARODA","Bank of Baroda","Banking",240.0,0.022,75),
                new Stock("PNB","Punjab National Bank","Banking",110.0,0.025,70),
                new Stock("IDFCFIRSTB","IDFC First Bank","Banking",85.0,0.023,72),
                new Stock("CANBK","Canara Bank","Banking",420.0,0.021,74),

                // Finance
                new Stock("BAJFINANCE","Bajaj Finance","Finance",7200.0,0.016,90),
                new Stock("BAJAJFINSV","Bajaj Finserv","Finance",1600.0,0.015,87),
                new Stock("HDFCLIFE","HDFC Life","Finance",620.0,0.014,86),
                new Stock("SBILIFE","SBI Life","Finance",1350.0,0.013,85),
                new Stock("LICI","LIC India","Finance",900.0,0.012,84),
                new Stock("MUTHOOTFIN","Muthoot Finance","Finance",1350.0,0.018,80),
                new Stock("MANAPPURAM","Manappuram Finance","Finance",160.0,0.020,76),
                new Stock("CHOLAFIN","Cholamandalam Finance","Finance",1150.0,0.017,82),

                // FMCG
                new Stock("ITC","ITC Limited","FMCG",450.0,0.008,89),
                new Stock("HINDUNILVR","Hindustan Unilever","FMCG",2400.0,0.009,93),
                new Stock("NESTLEIND","Nestle India","FMCG",22000.0,0.007,95),
                new Stock("DABUR","Dabur India","FMCG",540.0,0.010,84),
                new Stock("BRITANNIA","Britannia Industries","FMCG",5100.0,0.010,88),
                new Stock("MARICO","Marico","FMCG",520.0,0.011,82),
                new Stock("COLPAL","Colgate Palmolive","FMCG",1800.0,0.009,87),
                new Stock("GODREJCP","Godrej Consumer","FMCG",1100.0,0.012,83),
                new Stock("TATACONSUM","Tata Consumer Products","FMCG",950.0,0.011,85),
                new Stock("MCDOWELL-N","United Spirits","FMCG",1050.0,0.013,81),

                // Energy
                new Stock("RELIANCE","Reliance Industries","Energy",2900.0,0.018,92),
                new Stock("ONGC","ONGC","Energy",180.0,0.025,78),
                new Stock("BPCL","Bharat Petroleum","Energy",460.0,0.022,76),
                new Stock("IOC","Indian Oil","Energy",125.0,0.020,74),
                new Stock("GAIL","GAIL India","Energy",160.0,0.021,75),

                // Pharma
                new Stock("SUNPHARMA","Sun Pharma","Pharma",1450.0,0.014,90),
                new Stock("CIPLA","Cipla","Pharma",1100.0,0.015,87),
                new Stock("DRREDDY","Dr Reddy Labs","Pharma",5800.0,0.013,89),
                new Stock("DIVISLAB","Divis Labs","Pharma",3900.0,0.016,85),
                new Stock("APOLLOHOSP","Apollo Hospitals","Healthcare",5200.0,0.014,91),
                new Stock("LUPIN","Lupin","Pharma",1200.0,0.017,82),
                new Stock("BIOCON","Biocon","Pharma",320.0,0.019,78),

                // Auto
                new Stock("TATAMOTORS","Tata Motors","Auto",850.0,0.025,80),
                new Stock("MARUTI","Maruti Suzuki","Auto",9500.0,0.018,90),
                new Stock("M&M","Mahindra & Mahindra","Auto",1650.0,0.020,84),
                new Stock("BAJAJ-AUTO","Bajaj Auto","Auto",7800.0,0.017,86),
                new Stock("HEROMOTOCO","Hero MotoCorp","Auto",4200.0,0.019,82),

                // Metals
                new Stock("TATASTEEL","Tata Steel","Metals",140.0,0.030,72),
                new Stock("JSWSTEEL","JSW Steel","Metals",820.0,0.028,75),
                new Stock("HINDALCO","Hindalco","Metals",520.0,0.027,74),
                new Stock("COALINDIA","Coal India","Mining",420.0,0.024,77),

                // Infra
                new Stock("LT","Larsen & Toubro","Infrastructure",3500.0,0.015,91),
                new Stock("ADANIPORTS","Adani Ports","Infrastructure",1250.0,0.022,79),

                // Power
                new Stock("NTPC","NTPC","Power",320.0,0.016,82),
                new Stock("POWERGRID","Power Grid","Power",280.0,0.014,83),

                // Telecom
                new Stock("BHARTIARTL","Bharti Airtel","Telecom",1120.0,0.015,88),

                // Consumer
                new Stock("TITAN","Titan Company","Consumer",3400.0,0.015,90),
                new Stock("ASIANPAINT","Asian Paints","Consumer",3100.0,0.014,89),
                new Stock("DMART","Avenue Supermarts","Retail",3800.0,0.020,85),

                // Cement
                new Stock("ULTRACEMCO","UltraTech Cement","Cement",10200.0,0.014,90),
                new Stock("GRASIM","Grasim Industries","Cement",2100.0,0.016,86),

                // Defense
                new Stock("BEL","Bharat Electronics","Defense",210.0,0.018,82),
                new Stock("HAL","Hindustan Aeronautics","Defense",3100.0,0.017,88),

                // Railways
                new Stock("IRCTC","IRCTC","Railways",850.0,0.020,82),

                // Aviation
                new Stock("INDIGO","IndiGo Aviation","Aviation",3200.0,0.021,84)
        );

        stockRepository.saveAll(stocks);

        // ============================
        // ✅ 3. ASSIGN DIFFERENT HOLDINGS
        // ============================

        // Rushil Portfolio (Tech)
        holdingRepository.save(new Holding(null, "TCS", 10, 3500.0, rushil));
        holdingRepository.save(new Holding(null, "INFY", 15, 1500.0, rushil));
        holdingRepository.save(new Holding(null, "HCLTECH", 8, 1200.0, rushil));

        // Shambhavi Portfolio (Banking + FMCG)
        holdingRepository.save(new Holding(null, "HDFCBANK", 12, 1400.0, shambhavi));
        holdingRepository.save(new Holding(null, "ITC", 50, 420.0, shambhavi));
        holdingRepository.save(new Holding(null, "NESTLEIND", 5, 22000.0, shambhavi));

        // Shruti Portfolio (Pharma + Auto)
        holdingRepository.save(new Holding(null, "SUNPHARMA", 10, 1300.0, shruti));
        holdingRepository.save(new Holding(null, "CIPLA", 12, 1100.0, shruti));
        holdingRepository.save(new Holding(null, "MARUTI", 3, 9500.0, shruti));

        // Shivam Portfolio (Energy + Infra)
        holdingRepository.save(new Holding(null, "RELIANCE", 6, 2700.0, shivam));
        holdingRepository.save(new Holding(null, "ONGC", 20, 180.0, shivam));
        holdingRepository.save(new Holding(null, "LT", 4, 3200.0, shivam));

        System.out.println("✅ Database Seeded Successfully!");
    }
}
