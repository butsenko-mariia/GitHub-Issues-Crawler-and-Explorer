package app.BusinessLayer.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String modelName;

    private final RestTemplate restTemplate;

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateSummary(String title, String body) {
        if (apiKey == null || apiKey.equals("твій_справжній_api_key_тут") || apiKey.isEmpty()) {
            return "Помилка ШІ: API-ключ не налаштовано в application.properties.";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String prompt = String.format(
                    "Зроби короткий та зрозумілий підсумок (summary) для цієї проблеми з GitHub українською мовою. " +
                            "Опиши суть багу або запиту та що пропонують зробити.\n\nЗаголовок: %s\nОпис:\n%s",
                    title, body
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "Ти технічний асистент, який допомагає розробникам швидко розуміти суть GitHub Issues. Відповідай виключно українською мовою."));
            messages.add(Map.of("role", "user", "content", prompt));

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 300);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }

            return "Не вдалося отримати текст підсумку від ШІ.";

        } catch (Exception e) {
            return "Помилка під час генерації ШІ-підсумку: " + e.getMessage();
        }
    }
}