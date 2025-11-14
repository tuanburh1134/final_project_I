package com.example.financeapp.config;

import com.example.financeapp.entity.*;
import com.example.financeapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedCurrencies();
        seedTransactionTypesAndDefaultCategories();
    }

    // --- Tiền tệ ---
    private void seedCurrencies() {
        if (currencyRepository.count() == 0) {
            System.out.println(">>> [DataSeeder] Bảng 'currencies' trống. Đang thêm...");

            Currency vnd = new Currency();
            vnd.setCurrencyCode("VND");
            vnd.setCurrencyName("Vietnamese Dong");
            vnd.setSymbol("₫");

            Currency usd = new Currency();
            usd.setCurrencyCode("USD");
            usd.setCurrencyName("US Dollar");
            usd.setSymbol("$");

            currencyRepository.saveAll(List.of(vnd, usd));
            System.out.println(">>> [DataSeeder] Đã thêm 2 loại tiền tệ.");
        }
    }

    // --- Loại giao dịch + Danh mục mặc định ---
    private void seedTransactionTypesAndDefaultCategories() {
        if (transactionTypeRepository.count() == 0) {
            System.out.println(">>> [DataSeeder] Bảng 'transaction_types' trống. Đang thêm...");

            TransactionType expense = new TransactionType();
            expense.setTypeName("Chi tiêu");
            transactionTypeRepository.save(expense);

            TransactionType income = new TransactionType();
            income.setTypeName("Thu nhập");
            transactionTypeRepository.save(income);

            // DANH MỤC MẶC ĐỊNH (hệ thống, user = null)
            createDefault("Ăn uống", expense, "utensils");
            createDefault("Di chuyển", expense, "car");
            createDefault("Mua sắm", expense, "shopping-bag");
            createDefault("Giải trí", expense, "gamepad");
            createDefault("Hóa đơn", expense, "file-invoice-dollar");
            createDefault("Sức khỏe", expense, "heartbeat");
            createDefault("Giáo dục", expense, "graduation-cap");
            createDefault("Khác", expense, "ellipsis-h");

            createDefault("Lương", income, "money-bill-wave");
            createDefault("Thưởng", income, "gift");
            createDefault("Đầu tư", income, "chart-line");
            createDefault("Quà tặng", income, "hand-holding-heart");
            createDefault("Khác", income, "ellipsis-h");

            System.out.println(">>> [DataSeeder] Đã thêm TransactionType + 13 danh mục mặc định.");
        }
    }

    // Tạo danh mục hệ thống (user = null)
    private void createDefault(String name, TransactionType type, String icon) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setTransactionType(type);
        category.setIcon(icon);
        category.setUser(null); // Hệ thống
        categoryRepository.save(category);
    }
}