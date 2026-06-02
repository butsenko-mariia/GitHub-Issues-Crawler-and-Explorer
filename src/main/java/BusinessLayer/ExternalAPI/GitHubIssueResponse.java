package BusinessLayer.ExternalAPI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigInteger;
import java.time.ZonedDateTime;

@Data
public class GitHubIssueResponse {
    private BigInteger id;
    private int number;
    private String title;
    private String body;
    private String state;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;

    private GitHubUserResponse user;

    @Data
    public static class GitHubUserResponse {
        private BigInteger id;
        private String login;
        @JsonProperty("html_url")
        private String htmlUrl;
    }
}