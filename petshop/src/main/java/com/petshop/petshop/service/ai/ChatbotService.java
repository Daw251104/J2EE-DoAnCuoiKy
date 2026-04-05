package com.petshop.petshop.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.petshop.dto.ChatbotAction;
import com.petshop.petshop.dto.ChatbotActionRequest;
import com.petshop.petshop.dto.ChatbotResponse;
import com.petshop.petshop.dto.GioHangRequest;
import com.petshop.petshop.model.SanPham;
import com.petshop.petshop.repository.SanPhamRepository;
import com.petshop.petshop.service.service.GioHangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final String ACTION_ADD_TO_CART = "ADD_TO_CART";
    private static final String ACTION_OPEN_URL = "OPEN_URL";
    private static final int MAX_SUGGESTIONS = 3;
    private static final int MAX_QUANTITY = 20;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(\\d{1,3})\\b");

    private static final Set<String> STOP_WORDS = Set.of(
            "toi", "muon", "mua", "them", "vao", "gio", "hang", "san", "pham", "nhe",
            "voi", "giup", "minh", "can", "tim", "dat", "cho", "la", "di", "duoc"
    );

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private GioHangService gioHangService;

    @Value("${chatbot.openai.api-key:}")
    private String apiKey;

    @Value("${chatbot.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${chatbot.openai.fallback-models:gpt-4.1-mini}")
    private String fallbackModels;

    @Value("${chatbot.openai.api-url:https://api.openai.com/v1/responses}")
    private String apiUrl;

    @Value("${chatbot.system-prompt:You are a helpful assistant for a Vietnamese pet shop website. Keep responses concise and friendly in Vietnamese.}")
    private String systemPrompt;

    @Value("${chatbot.max-output-tokens:300}")
    private Integer maxOutputTokens;

    public ChatbotService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public ChatbotResponse ask(String userMessage, String username) {
        String message = userMessage == null ? "" : userMessage.trim();
        if (message.isEmpty()) {
            return new ChatbotResponse("Ban hay nhap cau hoi de minh ho tro nhe.");
        }

        String normalized = normalize(message);
        if (isPurchaseIntent(normalized)) {
            return buildPurchaseSuggestion(normalized, username);
        }

        if (apiKey == null || apiKey.isBlank() || "your_openai_api_key".equalsIgnoreCase(apiKey.trim())) {
            return new ChatbotResponse("Chatbot chua duoc cau hinh OPENAI_API_KEY. Hay set bien moi truong roi thu lai.");
        }

        String aiReply = askOpenAi(message);
        return new ChatbotResponse(aiReply, List.of());
    }

    public ChatbotResponse executeAction(ChatbotActionRequest request, String username) {
        if (request == null || request.getType() == null || request.getType().isBlank()) {
            return new ChatbotResponse("Minh chua nhan duoc hanh dong hop le.");
        }

        String actionType = request.getType().trim().toUpperCase(Locale.ROOT);
        if (ACTION_ADD_TO_CART.equals(actionType)) {
            return handleAddToCartAction(request, username);
        }

        if (ACTION_OPEN_URL.equals(actionType)) {
            String safeUrl = sanitizeInternalUrl(request.getUrl());
            if (safeUrl == null) {
                return new ChatbotResponse("Lien ket nay khong hop le.");
            }
            return new ChatbotResponse("Da san sang mo trang.", List.of(
                    new ChatbotAction(ACTION_OPEN_URL, "Mo trang", null, null, safeUrl)
            ));
        }

        return new ChatbotResponse("Hanh dong " + actionType + " hien chua duoc ho tro.");
    }

    private ChatbotResponse buildPurchaseSuggestion(String normalizedMessage, String username) {
        int quantity = extractQuantity(normalizedMessage);
        List<SanPham> products = suggestProducts(normalizedMessage);

        if (products.isEmpty()) {
            return new ChatbotResponse(
                    "Minh chua tim thay san pham phu hop. Ban thu noi ro ten san pham hoac loai thu cung nhe.",
                    List.of(new ChatbotAction(ACTION_OPEN_URL, "Xem tat ca san pham", null, null, "/sanpham"))
            );
        }

        StringBuilder replyBuilder = new StringBuilder();
        replyBuilder.append("Minh tim thay ").append(products.size()).append(" san pham phu hop:\n");
        for (SanPham sp : products) {
            replyBuilder.append("- ")
                    .append(sp.getTenSP())
                    .append(" (")
                    .append(formatPrice(sp.getGiaBan()))
                    .append(")\n");
        }

        List<ChatbotAction> actions = new ArrayList<>();
        if (username == null || username.isBlank()) {
            replyBuilder.append("Ban can dang nhap de minh them vao gio hang cho ban.");
            actions.add(new ChatbotAction(ACTION_OPEN_URL, "Dang nhap", null, null, "/login"));
            actions.add(new ChatbotAction(ACTION_OPEN_URL, "Xem san pham", null, null, "/sanpham"));
            return new ChatbotResponse(replyBuilder.toString().trim(), actions);
        }

        replyBuilder.append("Ban bam nut duoi day de xac nhan them vao gio hang.");
        for (SanPham sp : products) {
            actions.add(new ChatbotAction(
                    ACTION_ADD_TO_CART,
                    "Them " + shortName(sp.getTenSP(), 24) + " x" + quantity,
                    sp.getMaSP(),
                    quantity,
                    null
            ));
        }
        actions.add(new ChatbotAction(ACTION_OPEN_URL, "Xem gio hang", null, null, "/gio-hang"));
        return new ChatbotResponse(replyBuilder.toString().trim(), actions);
    }

    private ChatbotResponse handleAddToCartAction(ChatbotActionRequest request, String username) {
        if (username == null || username.isBlank()) {
            return new ChatbotResponse(
                    "Ban can dang nhap tai khoan khach hang de them vao gio.",
                    List.of(new ChatbotAction(ACTION_OPEN_URL, "Dang nhap ngay", null, null, "/login"))
            );
        }

        if (request.getProductId() == null) {
            return new ChatbotResponse("Minh khong nhan duoc ma san pham de them vao gio.");
        }

        int quantity = normalizeQuantity(request.getQuantity());
        GioHangRequest gioHangRequest = new GioHangRequest();
        gioHangRequest.setMaSP(request.getProductId());
        gioHangRequest.setSoLuong(quantity);

        try {
            gioHangService.themVaoGioHang(username, gioHangRequest);
            String productName = sanPhamRepository.findById(request.getProductId())
                    .map(SanPham::getTenSP)
                    .orElse("san pham da chon");

            return new ChatbotResponse(
                    "Da them " + quantity + " x " + productName + " vao gio hang cho ban.",
                    List.of(
                            new ChatbotAction(ACTION_OPEN_URL, "Xem gio hang", null, null, "/gio-hang"),
                            new ChatbotAction(ACTION_OPEN_URL, "Thanh toan ngay", null, null, "/thanh-toan")
                    )
            );
        } catch (Exception ex) {
            log.warn("Cannot add product to cart via chatbot. username={} productId={} quantity={}",
                    username, request.getProductId(), quantity, ex);
            return new ChatbotResponse("Minh chua them duoc vao gio hang. Ban thu lai sau nhe.");
        }
    }

    private List<SanPham> suggestProducts(String normalizedMessage) {
        List<SanPham> allProducts = sanPhamRepository.findAll();
        List<SanPham> activeProducts = allProducts.stream()
                .filter(this::isActiveProduct)
                .toList();

        if (activeProducts.isEmpty()) {
            return List.of();
        }

        Set<String> keywords = extractKeywords(normalizedMessage);
        List<ProductScore> scores = new ArrayList<>();
        for (SanPham product : activeProducts) {
            int score = computeProductScore(product, normalizedMessage, keywords);
            if (score > 0) {
                scores.add(new ProductScore(product, score));
            }
        }

        if (scores.isEmpty()) {
            return activeProducts.stream()
                    .limit(MAX_SUGGESTIONS)
                    .toList();
        }

        scores.sort((a, b) -> Integer.compare(b.score, a.score));
        List<SanPham> result = new ArrayList<>();
        Set<Integer> seenProductIds = new HashSet<>();
        for (ProductScore score : scores) {
            Integer id = score.product.getMaSP();
            if (id == null || seenProductIds.contains(id)) {
                continue;
            }
            seenProductIds.add(id);
            result.add(score.product);
            if (result.size() >= MAX_SUGGESTIONS) {
                break;
            }
        }
        return result;
    }

    private int computeProductScore(SanPham product, String normalizedMessage, Set<String> keywords) {
        String name = normalize(product.getTenSP());
        String desc = normalize(product.getMoTa());
        String petType = product.getLoaiThuCung() != null
                ? normalize(product.getLoaiThuCung().getTenLoaiTC())
                : "";
        String category = product.getLoaiSanPham() != null
                ? normalize(product.getLoaiSanPham().getTenLoai())
                : "";

        int score = 0;
        for (String keyword : keywords) {
            if (name.contains(keyword)) {
                score += 5;
            } else if (petType.contains(keyword) || category.contains(keyword)) {
                score += 3;
            } else if (desc.contains(keyword)) {
                score += 1;
            }
        }

        if (normalizedMessage.contains("meo") && petType.contains("meo")) {
            score += 3;
        }
        if (normalizedMessage.contains("cho canh") && petType.contains("cho")) {
            score += 2;
        }
        if (normalizedMessage.contains("phu kien") && category.contains("phu kien")) {
            score += 2;
        }

        return score;
    }

    private boolean isActiveProduct(SanPham product) {
        return product != null
                && product.getMaSP() != null
                && (product.getTinhTrang() == null || product.getTinhTrang() == 1);
    }

    private boolean isPurchaseIntent(String normalizedMessage) {
        return normalizedMessage.contains("mua")
                || normalizedMessage.contains("them vao gio")
                || normalizedMessage.contains("them gio")
                || normalizedMessage.contains("dat hang");
    }

    private int extractQuantity(String normalizedMessage) {
        Matcher matcher = NUMBER_PATTERN.matcher(normalizedMessage);
        if (!matcher.find()) {
            return 1;
        }

        try {
            int value = Integer.parseInt(matcher.group(1));
            return normalizeQuantity(value);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private int normalizeQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return 1;
        }
        return Math.min(quantity, MAX_QUANTITY);
    }

    private Set<String> extractKeywords(String normalizedMessage) {
        Set<String> keywords = new HashSet<>();
        String[] words = normalizedMessage.split("[^a-z0-9]+");
        for (String word : words) {
            if (word == null) {
                continue;
            }
            String token = word.trim();
            if (token.length() < 2 || STOP_WORDS.contains(token)) {
                continue;
            }
            keywords.add(token);
        }
        return keywords;
    }

    private String shortName(String name, int maxLength) {
        if (name == null || name.isBlank()) {
            return "san pham";
        }
        String cleaned = name.trim();
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }
        return cleaned.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }

    private String formatPrice(BigDecimal value) {
        if (value == null) {
            return "Lien he";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(value) + " VND";
    }

    private String sanitizeInternalUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        return trimmed.startsWith("/") ? trimmed : null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replace('đ', 'd');
    }

    private String askOpenAi(String message) {
        List<String> candidateModels = buildCandidateModels();
        int lastStatus = 0;
        String lastDetail = "Unknown error";

        for (String currentModel : candidateModels) {
            try {
                HttpResponse<String> response = sendRequest(message, currentModel);
                if (response.statusCode() >= 400) {
                    String detail = extractErrorMessage(response.body());
                    lastStatus = response.statusCode();
                    lastDetail = detail;
                    log.warn("OpenAI API error. model={} status={} detail={}", currentModel, lastStatus, detail);

                    if (isModelError(lastStatus, detail)) {
                        continue;
                    }

                    return mapErrorToUserMessage(lastStatus, lastDetail);
                }

                String answer = parseReply(response.body());
                if (!answer.isBlank()) {
                    return answer;
                }

                log.warn("OpenAI response has no text content. model={} body={}",
                        currentModel, compactForLog(response.body()));
            } catch (Exception ex) {
                log.error("Chatbot call failed. model={}", currentModel, ex);
                lastStatus = 500;
                lastDetail = ex.getMessage() == null ? "Unknown error" : ex.getMessage();
            }
        }

        if (isModelError(lastStatus, lastDetail)) {
            return "Model OpenAI cau hinh chua dung hoac khong duoc cap quyen. Ban thu doi OPENAI_MODEL sang gpt-4o-mini.";
        }
        if (lastStatus > 0) {
            return mapErrorToUserMessage(lastStatus, lastDetail);
        }
        return "Minh chua lay duoc cau tra loi tu AI. Ban thu lai hoac doi model OPENAI_MODEL nhe.";
    }

    private HttpResponse<String> sendRequest(String userMessage, String currentModel) throws Exception {
        String requestBody = buildRequestBody(userMessage, currentModel);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<String> buildCandidateModels() {
        List<String> candidates = new ArrayList<>();
        addModelIfValid(candidates, model);

        if (fallbackModels != null && !fallbackModels.isBlank()) {
            Arrays.stream(fallbackModels.split(","))
                    .map(String::trim)
                    .forEach(fallback -> addModelIfValid(candidates, fallback));
        }

        if (candidates.isEmpty()) {
            candidates.add("gpt-4o-mini");
        }

        return candidates;
    }

    private void addModelIfValid(List<String> candidates, String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return;
        }
        String trimmed = modelName.trim();
        if (!candidates.contains(trimmed)) {
            candidates.add(trimmed);
        }
    }

    private boolean isModelError(int statusCode, String detail) {
        if (statusCode != 400) {
            return false;
        }
        String message = detail == null ? "" : detail.toLowerCase(Locale.ROOT);
        return message.contains("model")
                || message.contains("not found")
                || message.contains("does not exist")
                || message.contains("not supported");
    }

    private String buildRequestBody(String userMessage, String currentModel) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", currentModel);
        payload.put("instructions", systemPrompt);
        payload.put("input", userMessage);
        payload.put("max_output_tokens", maxOutputTokens);

        return objectMapper.writeValueAsString(payload);
    }

    private String parseReply(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);

        String outputText = root.path("output_text").asText("");
        if (!outputText.isBlank()) {
            return outputText.trim();
        }

        List<String> parts = new ArrayList<>();

        for (JsonNode outputNode : root.path("output")) {
            for (JsonNode contentNode : outputNode.path("content")) {
                collectText(parts, contentNode);
            }
        }

        for (JsonNode contentNode : root.path("content")) {
            collectText(parts, contentNode);
        }

        for (JsonNode choiceNode : root.path("choices")) {
            JsonNode messageNode = choiceNode.path("message");
            JsonNode contentNode = messageNode.path("content");
            if (contentNode.isTextual()) {
                String text = contentNode.asText("");
                if (!text.isBlank()) {
                    parts.add(text.trim());
                }
            } else if (contentNode.isArray()) {
                for (JsonNode item : contentNode) {
                    collectText(parts, item);
                }
            }
        }

        return String.join("\n", parts).trim();
    }

    private void collectText(List<String> parts, JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }

        String type = node.path("type").asText("");
        if (!type.isBlank() && !"output_text".equals(type) && !"text".equals(type)) {
            return;
        }

        JsonNode textNode = node.path("text");
        if (textNode.isTextual()) {
            String text = textNode.asText("");
            if (!text.isBlank()) {
                parts.add(text.trim());
            }
            return;
        }

        String value = textNode.path("value").asText("");
        if (!value.isBlank()) {
            parts.add(value.trim());
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText("");
            if (!message.isBlank()) {
                return message.trim();
            }
        } catch (Exception ex) {
            log.debug("Cannot parse OpenAI error response body", ex);
        }

        if (responseBody == null || responseBody.isBlank()) {
            return "Unknown error";
        }

        String compact = responseBody.replaceAll("\\s+", " ").trim();
        return compact.length() > 180 ? compact.substring(0, 180) + "..." : compact;
    }

    private String mapErrorToUserMessage(int statusCode, String detail) {
        if (statusCode == 401 || statusCode == 403) {
            return "OpenAI API key khong hop le hoac khong du quyen. Ban kiem tra OPENAI_API_KEY.";
        }

        if (statusCode == 429) {
            return "Tai khoan OpenAI dang vuot han muc (rate limit/quota). Ban thu lai sau.";
        }

        if (statusCode == 400) {
            if (detail != null && detail.toLowerCase(Locale.ROOT).contains("model")) {
                return "Model OpenAI cau hinh chua dung hoac khong duoc cap quyen. Ban thu doi OPENAI_MODEL.";
            }
            return "Yeu cau den OpenAI chua hop le. Chi tiet: " + detail;
        }

        if (statusCode >= 500) {
            return "May chu AI dang tam thoi qua tai. Ban thu lai sau it phut nhe.";
        }

        return "Chatbot tam thoi gap loi ket noi AI. Chi tiet: " + detail;
    }

    private String compactForLog(String raw) {
        if (raw == null || raw.isBlank()) {
            return "(empty)";
        }
        String compact = raw.replaceAll("\\s+", " ").trim();
        return compact.length() > 500 ? compact.substring(0, 500) + "..." : compact;
    }

    private static class ProductScore {
        private final SanPham product;
        private final int score;

        private ProductScore(SanPham product, int score) {
            this.product = product;
            this.score = score;
        }
    }
}
