package app.BusinessLayer.ExternalAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitHubApiClient {

    @Value("${github.api.token:}")
    private String token;

    @Value("${github.api.user-agent:GitHubIssuesCrawler/1.0}")
    private String userAgent;

    private final RestTemplate restTemplate = new RestTemplate();

    public <T> T get(String url, Class<T> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
        ResponseEntity<T> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                responseType
        );
        return response.getBody();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.set("User-Agent", userAgent);

        headers.set("Accept", "application/vnd.github+json");

        headers.set("X-GitHub-Api-Version", "2022-11-28");

        if (token != null && !token.isBlank()) {
            headers.set("Authorization", "Bearer " + token);
        }

        return headers;
    }

    public boolean hasToken() {
        return token != null && !token.isBlank();
    }
}