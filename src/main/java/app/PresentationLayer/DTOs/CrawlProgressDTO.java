package app.PresentationLayer.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlProgressDTO {
    private String status;

    @JsonProperty("crawled_count")
    private int crawledCount;

    @JsonProperty("current_page")
    private int currentPage;

    private String message;
}