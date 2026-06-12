package app.PresentationLayer.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepositoryDTO {
    private UUID id;

    @JsonProperty("owner_id")
    private UUID ownerId;

    private String name;
    private String url;

    @JsonProperty("total_issues")
    private int totalIssues;

    @JsonProperty("crawled_issues_count")
    private long crawledIssuesCount;

    @JsonProperty("unique_authors_count")
    private long uniqueAuthorsCount;

    @JsonProperty("crawled_at")
    private LocalDate crawledAt;
}