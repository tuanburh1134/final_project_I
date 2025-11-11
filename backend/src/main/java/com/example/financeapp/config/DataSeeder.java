package com.example.financeapp.config;

import com.example.financeapp.entity.*;
import com.example.financeapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp này sẽ tự động chạy khi ứng dụng khởi động.
 * Nó dùng để "gieo" dữ liệu mầm (seed data) cho database.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private CategoryRepository categoryRepository;

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";

    @Override
    public void run(String... args) throws Exception {
        seedCurrencies();
        seedTransactionTypesAndCategories();
    }

    private void seedCurrencies() {
        if (currencyRepository.count() == 0) {
            System.out.println(">>> [DataSeeder] Bảng 'currencies' trống. Bắt đầu thêm dữ liệu mẫu...");

            Currency vnd = new Currency();
            vnd.setCurrencyCode("VND");
            vnd.setCurrencyName("Vietnamese Dong");
            vnd.setSymbol("₫");

            Currency usd = new Currency();
            usd.setCurrencyCode("USD");
            usd.setCurrencyName("US Dollar");
            usd.setSymbol("$");

            currencyRepository.saveAll(List.of(vnd, usd));
            System.out.println(">>> [DataSeeder] Đã thêm thành công 2 loại tiền tệ (VND, USD).");
        if (currencyRepository.count() == 0) {
            System.out.println(">>> [DataSeeder] Bảng 'currencies' trống. Đang gọi API lấy tỷ giá...");

            try {
                RestTemplate restTemplate = new RestTemplate();
                String url = UriComponentsBuilder.fromHttpUrl(API_URL).toUriString();

                // Gọi API
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response == null || !response.containsKey("rates")) {
                    throw new RuntimeException("Không nhận được dữ liệu tỷ giá từ API!");
                }

                Map<String, Double> rates = (Map<String, Double>) response.get("rates");

                // Lấy tỷ giá của các đồng phổ biến (so với VND)
                double usdToVnd = rates.getOrDefault("VND", 25000.0);

                // Danh sách các đồng muốn lưu
                Map<String, String> currencyNames = new HashMap<>();
                currencyNames.put("USD", "US Dollar");
                currencyNames.put("VND", "Vietnamese Dong");
                currencyNames.put("EUR", "Euro");
                currencyNames.put("JPY", "Japanese Yen");
                currencyNames.put("GBP", "British Pound");
                currencyNames.put("AUD", "Australian Dollar");

                for (String code : currencyNames.keySet()) {
                    Currency currency = new Currency();
                    currency.setCurrencyCode(code);
                    currency.setCurrencyName(currencyNames.get(code));

                    if (code.equals("VND")) {
                        currency.setRateToVnd(BigDecimal.valueOf(1)); // VND là chuẩn
                        currency.setSymbol("₫");
                    } else {
                        Double rateToUsd = rates.get(code);
                        if (rateToUsd == null) continue; // bỏ qua nếu không có dữ liệu

                        // Quy đổi sang VND: 1 code = (USD → VND) / (USD → code)
                        double rateToVnd = usdToVnd / rateToUsd;
                        currency.setRateToVnd(BigDecimal.valueOf(rateToVnd));

                        // Gán biểu tượng đơn giản
                        switch (code) {
                            case "USD" -> currency.setSymbol("$");
                            case "EUR" -> currency.setSymbol("€");
                            case "JPY" -> currency.setSymbol("¥");
                            case "GBP" -> currency.setSymbol("£");
                            case "AUD" -> currency.setSymbol("A$");
                            default -> currency.setSymbol(code);
                        }
                    }

                    currencyRepository.save(currency);
                }

                System.out.println(">>> [DataSeeder] Đã thêm dữ liệu tiền tệ thật từ API thành công ✅");

            } catch (Exception e) {
                System.err.println(">>> [DataSeeder] Lỗi khi gọi API tỷ giá: " + e.getMessage());
                System.err.println(">>> Sử dụng dữ liệu mặc định (offline mode).");

                // fallback dữ liệu mặc định
                Currency vnd = new Currency();
                vnd.setCurrencyCode("VND");
                vnd.setCurrencyName("Vietnamese Dong");
                vnd.setRateToVnd(BigDecimal.valueOf(1));
                vnd.setSymbol("₫");

                Currency usd = new Currency();
                usd.setCurrencyCode("USD");
                usd.setCurrencyName("US Dollar");
                usd.setRateToVnd(BigDecimal.valueOf(25000));
                usd.setSymbol("$");

                Currency eur = new Currency();
                eur.setCurrencyCode("EUR");
                eur.setCurrencyName("Euro");
                eur.setRateToVnd(BigDecimal.valueOf(27000));
                eur.setSymbol("€");

                Currency jpy = new Currency();
                jpy.setCurrencyCode("JPY");
                jpy.setCurrencyName("Japanese Yen");
                jpy.setRateToVnd(BigDecimal.valueOf(180));
                jpy.setSymbol("¥");

                currencyRepository.saveAll(List.of(vnd, usd, eur, jpy));
            }

        } else {
            System.out.println(">>> [DataSeeder] Bảng 'currencies' đã có dữ liệu. Bỏ qua seeding.");
        }
    }

    private void seedTransactionTypesAndCategories() {
        if (transactionTypeRepository.count() == 0) {
            System.out.println(">>> [DataSeeder] Bảng 'transaction_types' trống. Bắt đầu thêm dữ liệu mẫu...");

            // 1. Tạo loại giao dịch
            TransactionType expense = new TransactionType();
            expense.setTypeName("Chi tiêu");
            transactionTypeRepository.save(expense);

            TransactionType income = new TransactionType();
            income.setTypeName("Thu nhập");
            transactionTypeRepository.save(income);

            // 2. Tạo danh mục cho Chi tiêu
            createCategory("Ăn uống", expense, "food");
            createCategory("Di chuyển", expense, "transport");
            createCategory("Mua sắm", expense, "shopping");
            createCategory("Giải trí", expense, "entertainment");
            createCategory("Hóa đơn", expense, "bills");
            createCategory("Sức khỏe", expense, "health");
            createCategory("Giáo dục", expense, "education");
            createCategory("Khác", expense, "other");

            // 3. Tạo danh mục cho Thu nhập
            createCategory("Lương", income, "salary");
            createCategory("Thưởng", income, "bonus");
            createCategory("Đầu tư", income, "investment");
            createCategory("Quà tặng", income, "gift");
            createCategory("Khác", income, "other");

            System.out.println(">>> [DataSeeder] Đã thêm TransactionType + Category mẫu thành công.");
        } else {
            System.out.println(">>> [DataSeeder] Bảng 'transaction_types' đã có dữ liệu. Bỏ qua seeding.");
        }
    }

    // Hàm hỗ trợ tạo danh mục
    private void createCategory(String name, TransactionType type, String icon) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setTransactionType(type);
        category.setIcon(icon);
        categoryRepository.save(category);
    }
}
}
