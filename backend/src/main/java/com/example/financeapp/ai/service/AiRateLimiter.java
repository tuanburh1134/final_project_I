package com.example.financeapp.ai.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiRateLimiter {
    private static final int LIMIT = 30; // requests
    private static final long WINDOW_MS = 5 * 60 * 1000L; // 5 minutes

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) {
                q.pollFirst();
            }
            if (q.size() >= LIMIT) {
                return false;
            }
            q.addLast(now);
            return true;
        }
    }
}
