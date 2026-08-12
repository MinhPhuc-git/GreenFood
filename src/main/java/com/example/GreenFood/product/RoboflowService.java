package com.example.GreenFood.product;

import com.example.GreenFood.config.RoboflowConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoboflowService {

    private static final Map<String, String> LABEL_VI = Map.ofEntries(
            Map.entry("tomato", "Cà chua"),
            Map.entry("ca chua", "Cà chua"),
            Map.entry("egg", "Trứng gà"),
            Map.entry("trung ga", "Trứng gà"),
            Map.entry("onion", "Hành tây"),
            Map.entry("hanh tay", "Hành tây"),
            Map.entry("carrot", "Cà rốt"),
            Map.entry("ca rot", "Cà rốt"),
            Map.entry("potato", "Khoai tây"),
            Map.entry("khoai tay", "Khoai tây"),
            Map.entry("cabbage", "Bắp cải"),
            Map.entry("bac cai", "Bắp cải"),
            Map.entry("lettuce", "Xà lách"),
            Map.entry("xa lach", "Xà lách"),
            Map.entry("cucumber", "Dưa chuột"),
            Map.entry("dua chuot", "Dưa chuột"),
            Map.entry("bell pepper", "Ớt chuông"),
            Map.entry("pepper", "Ớt"),
            Map.entry("garlic", "Tỏi"),
            Map.entry("toi", "Tỏi"),
            Map.entry("ginger", "Gừng"),
            Map.entry("gung", "Gừng"),
            Map.entry("mushroom", "Nấm"),
            Map.entry("nam", "Nấm"),
            Map.entry("broccoli", "Bông cải xanh"),
            Map.entry("corn", "Ngô"),
            Map.entry("ngo", "Ngô"),
            Map.entry("apple", "Táo"),
            Map.entry("tao", "Táo"),
            Map.entry("banana", "Chuối"),
            Map.entry("chuoi", "Chuối"),
            Map.entry("orange", "Cam"),
            Map.entry("cam", "Cam"),
            Map.entry("lemon", "Chanh"),
            Map.entry("chanh", "Chanh"),
            Map.entry("beef", "Thịt bò"),
            Map.entry("thit bo", "Thịt bò"),
            Map.entry("chicken", "Thịt gà"),
            Map.entry("thit ga", "Thịt gà"),
            Map.entry("pork", "Thịt heo"),
            Map.entry("thit heo", "Thịt heo"),
            Map.entry("fish", "Cá"),
            Map.entry("ca", "Cá"),
            Map.entry("shrimp", "Tôm"),
            Map.entry("tom", "Tôm"),
            Map.entry("tofu", "Đậu phụ"),
            Map.entry("dau phu", "Đậu phụ"),
            Map.entry("rice", "Gạo"),
            Map.entry("gao", "Gạo"),
            Map.entry("noodle", "Mì"),
            Map.entry("mi", "Mì")
    );

    private final RoboflowConfig config;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RoboflowService(RoboflowConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public record DetectedIngredient(String name, String className, double confidence) {
    }

    public List<DetectedIngredient> detectIngredients(MultipartFile file) {
        validateImage(file);

        if (!config.isConfigured()) {
            System.err.println("Roboflow chưa được cấu hình. Chức năng nhận diện bị vô hiệu hóa.");
            return List.of();
        }

        try {
            String modelName = config.getModel();
            if (modelName != null && modelName.contains("/")) {
                modelName = modelName.substring(modelName.lastIndexOf("/") + 1);
            }

            String url = String.format(
                    Locale.US,
                    "%s/%s/%s?api_key=%s&confidence=%d",
                    trimTrailingSlash(config.getApiUrl()),
                    modelName,
                    config.getVersion(),
                    config.getApiKey(),
                    config.getConfidence());

            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                }
            });

            String responseBody = restClient.post()
                    .uri(url)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parsePredictions(responseBody);
        } catch (Exception ex) {
            System.err.println("Lỗi kết nối Roboflow (sai API key hoặc lỗi mạng), chức năng nhận diện tạm thời không hoạt động: " + ex.getMessage());
            return List.of();
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn ảnh nguyên liệu");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File phải là hình ảnh (JPG, PNG, ...)");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh không được vượt quá 10MB");
        }
    }

    private List<DetectedIngredient> parsePredictions(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode predictions = root.path("predictions");
        if (!predictions.isArray() || predictions.isEmpty()) {
            return List.of();
        }

        Map<String, DetectedIngredient> bestByClass = new LinkedHashMap<>();
        for (JsonNode prediction : predictions) {
            String className = prediction.path("class").asText("").trim();
            if (className.isBlank()) {
                continue;
            }
            double confidence = prediction.path("confidence").asDouble(0);
            String key = className.toLowerCase(Locale.ROOT);
            DetectedIngredient current = bestByClass.get(key);
            if (current == null || confidence > current.confidence()) {
                bestByClass.put(key, new DetectedIngredient(toVietnameseLabel(className), className, confidence));
            }
        }

        return bestByClass.values().stream()
                .sorted(Comparator.comparingDouble(DetectedIngredient::confidence).reversed())
                .toList();
    }

    private String toVietnameseLabel(String className) {
        String normalized = className.toLowerCase(Locale.ROOT).trim();
        return LABEL_VI.getOrDefault(normalized, capitalizeWords(className));
    }

    private String capitalizeWords(String value) {
        String[] parts = value.trim().split("\\s+");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            result.add(part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT));
        }
        return String.join(" ", result);
    }

    private String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "https://detect.roboflow.com";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
