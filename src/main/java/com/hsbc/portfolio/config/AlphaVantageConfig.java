package com.hsbc.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class AlphaVantageConfig {

    @Value("${app.alpha-vantage.api-key:}")
    private String apiKey;

    @Value("${app.alpha-vantage.base-url:https://www.alphavantage.co/query}")
    private String baseUrl;

    /**
     * RestTemplate for Alpha Vantage API (no special headers needed)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * RestTemplate for Yahoo Finance API (requires specific headers)
     */
    @Bean
    public RestTemplate yahooFinanceRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add interceptor to add required headers for Yahoo Finance
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new YahooFinanceHeaderInterceptor());
        restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Interceptor to add required headers for Yahoo Finance API requests
     */
    private static class YahooFinanceHeaderInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public org.springframework.http.client.ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request, byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

            HttpHeaders headers = request.getHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.add("Accept", "application/json, text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8");
            headers.add("Accept-Language", "en-US,en;q=0.5");
            headers.add("Connection", "keep-alive");
            
            return execution.execute(request, body);
        }
    }
}

