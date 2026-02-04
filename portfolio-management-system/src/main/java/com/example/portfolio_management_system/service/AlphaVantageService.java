package com.example.portfolio_management_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AlphaVantageService {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Double getLivePrice(String symbol) {

        try {
            String url =
                    "https://www.alphavantage.co/query?function=GLOBAL_QUOTE"
                            + "&symbol=" + symbol
                            + "&apikey=" + apiKey;

            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode quote = root.path("Global Quote");

            String priceStr = quote.path("05. price").asText();

            return Double.parseDouble(priceStr);

        } catch (Exception e) {
            System.out.println("Error fetching price: " + e.getMessage());
            return 0.0;
        }
    }
}
