package com.example.financeapp.service.impl;

import com.example.financeapp.entity.Currency;
import com.example.financeapp.repository.CurrencyRepository;
import com.example.financeapp.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public void updateExchangeRates() {
        String url = "https://api.exchangerate.host/latest?base=USD&symbols=VND,EUR,JPY,GBP,KRW,THB";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = response.getBody();

        if (body == null || body.get("rates") == null) {
            throw new RuntimeException("Không thể lấy dữ liệu tỷ giá từ API");
        }

        @SuppressWarnings("unchecked")
        Map<String, Double> rates = (Map<String, Double>) body.get("rates");
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            String code = entry.getKey();
            BigDecimal rateToVnd = BigDecimal.valueOf(entry.getValue());

            Currency currency = currencyRepository.findByCurrencyCode(code)
                    .orElseGet(() -> {
                        Currency c = new Currency();
                        c.setCurrencyCode(code);
                        c.setCurrencyName(code);
                        return c;
                    });

            currency.setRateToVnd(rateToVnd);
            currency.setLastUpdated(now);
            currencyRepository.save(currency);
        }

        // USD là base → tự gán 1.0
        Currency usd = currencyRepository.findByCurrencyCode("USD")
                .orElseGet(Currency::new);
        usd.setCurrencyCode("USD");
        usd.setCurrencyName("US Dollar");
        usd.setRateToVnd(BigDecimal.ONE);
        usd.setLastUpdated(now);
        currencyRepository.save(usd);

        System.out.println("✅ Cập nhật tỷ giá thành công lúc " + now);
    }
}
