package com.example.financeapp.feedback.repository;

import com.example.financeapp.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}

