package app.PresentationLayer.DTOs;

import app.PersistenceLayer.Enums.IssueStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueDTO {
    private UUID id;
    @JsonProperty("repo_id")
    private UUID repoId;
    @JsonProperty("author_id")
    private UUID authorId;
    @JsonProperty("github_id")
    private BigInteger githubId;
    @JsonProperty("issue_number")
    private int issueNumber;
    private String title;
    private String body;
    private IssueStatus state;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("created_at")
    private LocalDate createdAt;
    @JsonProperty("updated_at")
    private LocalDate updatedAt;
    @JsonProperty("ai_summary")
    private String aiSummary;
    @JsonProperty("author_login")
    private String authorLogin;
}
