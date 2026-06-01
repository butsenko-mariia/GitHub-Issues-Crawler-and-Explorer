package PresentationLayer.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitHubUserDTO {
    private UUID id;
    @JsonProperty("github_id")
    private BigInteger githubId;
    private String login;
    private String name;
    @JsonProperty("profile_url")
    private String profileUrl;
}
