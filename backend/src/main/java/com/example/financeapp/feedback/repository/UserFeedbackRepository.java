package com.example.financeapp.feedback.repository;

import com.example.financeapp.feedback.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {

    List<UserFeedback> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}

