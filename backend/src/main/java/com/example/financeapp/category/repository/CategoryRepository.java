package com.example.financeapp.category.repository;

import com.example.financeapp.category.entity.Category;
import com.example.financeapp.transaction.entity.TransactionType;
import com.example.financeapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(User user);

    List<Category> findByUserAndTransactionType(User user, TransactionType type);

    // ✅ DÙNG field id trong User (User.id), KHÔNG phải userId
    List<Category> findByUser_Id(Long userId);

    List<Category> findByUserIsNullAndIsSystemTrue();

    boolean existsByCategoryNameAndTransactionTypeAndUser(
            String categoryName, TransactionType transactionType, User user
    );

    boolean existsByCategoryNameAndTransactionTypeAndUserIsNullAndIsSystemTrue(
            String categoryName, TransactionType transactionType
    );
}
