package com.example.financeapp.ai.service;

import com.example.financeapp.ai.dto.ChatMessage;
import com.example.financeapp.ai.dto.ChatRequest;
import com.example.financeapp.ai.dto.ChatResponse;
import com.example.financeapp.ai.dto.UsageInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private static final String ENDPOINT_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String SYSTEM_CONTEXT = "You are MyWallet AI copilot for a personal finance app. Goals: unblock users fast, guide them step-by-step, and propose the exact next UI actions. Respond in the user's language (Vietnamese or English) and keep answers ultra-concise (<=5 bullet points or <=80 words). Core modules (match app): Auth, Wallets, Transactions (income/expense/transfer), Categories, Budgets, Reports, Funds, Feedback, Notifications, Reminders/Scheduled transactions, Reviews, Admin tools. Capabilities: (1) create/advise on transactions, (2) create categories, (3) set budgets, (4) explain reports, (5) wallet help, (6) onboarding and troubleshooting, (7) security hygiene (logout all devices, change password/2FA). When asked to 'create' things, list minimal inputs AND the UI path/button exactly as in app: Transaction: type (thu/chi/chuyển), amount, wallet, category, date/time, note -> Giao dịch > Thêm giao dịch; Category: name, type (thu/chi), description (optional) -> Danh mục > Thêm danh mục; Budget: category, limit amount, period -> Ngân sách > Tạo ngân sách; Report: date range, filters (wallet, category) -> Báo cáo > chọn khoảng thời gian/bộ lọc. Use provided user context (wallet balances + currency, recent tx, budgets with usage/alerts, top spend categories 30d, MoM income/expense change, low-balance wallets, largest expense + avg expense 30d) to tailor advice and surface alerts (e.g., budget near/over limit, wallet balance low -> suggest nạp tiền/chuyển từ ví khác, unusual large spend -> suggest kiểm tra giao dịch/báo cáo). Suggest likely category or wallet based on keywords if clear; if unsure, ask one short clarifying question. Never invent features that do not exist in the app.";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiContextService aiContextService;

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String model;

    public GeminiService(AiContextService aiContextService) {
        this.aiContextService = aiContextService;
    }

    public ChatResponse generate(ChatRequest request, Long userId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI API key is not configured. Set ai.gemini.api-key or environment variable GEMINI_API_KEY.");
        }

        String userContext = aiContextService.buildUserContext(userId);

        List<Map<String, Object>> contents = buildContents(request.getMessages(), userContext);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.55);
        generationConfig.put("topP", 0.9);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 512);

        List<Map<String, String>> safetySettings = List.of(
                Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                Map.of("category", "HARM_CATEGORY_DANGEROUS", "threshold", "BLOCK_MEDIUM_AND_ABOVE")
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", contents);
        payload.put("generationConfig", generationConfig);
        payload.put("safetySettings", safetySettings);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = String.format(ENDPOINT_TEMPLATE, model) + "?key=" + apiKey;

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                String.class
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể gọi AI: " + ex.getMessage());
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("AI service returned status " + response.getStatusCode());
        }

        return parseResponse(response.getBody());
    }

    private List<Map<String, Object>> buildContents(List<ChatMessage> messages, String userContext) {
        List<Map<String, Object>> contents = new ArrayList<>();

        Map<String, Object> system = new HashMap<>();
        system.put("role", "user");
        system.put("parts", List.of(Map.of("text", SYSTEM_CONTEXT)));
        contents.add(system);

        if (userContext != null && !userContext.isBlank()) {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "user");
            ctx.put("parts", List.of(Map.of("text", "User context: " + userContext)));
            contents.add(ctx);
        }

        for (ChatMessage msg : messages) {
            String role = "user".equalsIgnoreCase(msg.getSender()) ? "user" : "model";
            Map<String, Object> item = new HashMap<>();
            item.put("role", role);
            item.put("parts", List.of(Map.of("text", msg.getText())));
            contents.add(item);
        }
        return contents;
    }

    private ChatResponse parseResponse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            if (root.has("error")) {
                String message = root.path("error").path("message").asText("AI service error");
                throw new IllegalStateException(message);
            }

            JsonNode parts = root.at("/candidates/0/content/parts");
            if (parts.isMissingNode() || !parts.isArray()) {
                throw new IllegalStateException("AI did not return any content.");
            }

                String resultText = parts.size() == 0 ? "" :
                    java.util.stream.StreamSupport.stream(parts.spliterator(), false)
                        .map(node -> node.path("text").asText(""))
                        .collect(Collectors.joining(""))
                        .trim();

            UsageInfo usage = new UsageInfo(
                    root.at("/usageMetadata/promptTokenCount").isMissingNode() ? null : root.at("/usageMetadata/promptTokenCount").asInt(),
                    root.at("/usageMetadata/candidatesTokenCount").isMissingNode() ? null : root.at("/usageMetadata/candidatesTokenCount").asInt(),
                    root.at("/usageMetadata/totalTokenCount").isMissingNode() ? null : root.at("/usageMetadata/totalTokenCount").asInt()
            );

            if (resultText.isEmpty()) {
                throw new IllegalStateException("AI did not return a reply.");
            }

            return new ChatResponse(resultText, usage);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse AI response: " + ex.getMessage());
        }
    }
}
