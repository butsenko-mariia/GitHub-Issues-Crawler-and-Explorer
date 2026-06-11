package app.PresentationLayer.Controllers;

import app.BusinessLayer.Services.GitHubUserService;
import app.BusinessLayer.Services.IssueService;
import app.PersistenceLayer.Models.GitHubUser;
import app.PresentationLayer.DTOs.GitHubUserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "http://localhost:5173")
public class GitHubUserController {

    private final GitHubUserService gitHubUserService;
    private final IssueService issueService;

    public GitHubUserController(GitHubUserService gitHubUserService, IssueService issueService) {
        this.gitHubUserService = gitHubUserService;
        this.issueService = issueService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GitHubUserDTO> getAuthorDetails(@PathVariable UUID id) {
        GitHubUser user = gitHubUserService.getById(id);

        GitHubUserDTO dto = GitHubUserDTO.builder()
                .id(user.getId())
                .githubId(user.getGithubId())
                .login(user.getLogin())
                .name(user.getName())
                .profileUrl(user.getProfileUrl())
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{authorId}/repositories/{repoId}/stats")
    public ResponseEntity<GitHubUserDTO> getAuthorStatsInRepo(
            @PathVariable UUID authorId,
            @PathVariable UUID repoId) {

        GitHubUser user = gitHubUserService.getById(authorId);

        long issuesCount = issueService.getIssuesByRepository(repoId).stream()
                .filter(issue -> issue.getAuthor() != null
                        && issue.getAuthor().getId().equals(authorId))
                .count();

        GitHubUserDTO dto = GitHubUserDTO.builder()
                .id(user.getId())
                .githubId(user.getGithubId())
                .login(user.getLogin())
                .name(user.getName())
                .profileUrl(user.getProfileUrl())
                .issuesCount(issuesCount)
                .build();

        return ResponseEntity.ok(dto);
    }
}