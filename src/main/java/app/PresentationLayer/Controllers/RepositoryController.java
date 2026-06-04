package app.PresentationLayer.Controllers;

import app.BusinessLayer.Services.CrawlerService;
import app.BusinessLayer.Services.IssueService;
import app.BusinessLayer.Services.RepositoryService;
import app.PersistenceLayer.Enums.IssueStatus;
import app.PersistenceLayer.Models.GitHubUser;
import app.PersistenceLayer.Models.Issue;
import app.PersistenceLayer.Models.Repository;
import app.PresentationLayer.DTOs.GitHubUserDTO;
import app.PresentationLayer.DTOs.IssueDTO;
import app.PresentationLayer.DTOs.RepositoryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repositories")
@CrossOrigin(origins = "*")
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final CrawlerService crawlerService;
    private final IssueService issueService;

    public RepositoryController(RepositoryService repositoryService, CrawlerService crawlerService, IssueService issueService) {
        this.repositoryService = repositoryService;
        this.crawlerService = crawlerService;
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<List<RepositoryDTO>> getAllRepositories() {
        List<RepositoryDTO> result = repositoryService.findAll().stream()
                .map(this::convertToRepositoryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<RepositoryDTO> addRepository(@RequestBody Map<String, String> payload) {
        String url = payload.get("url");
        Repository savedRepo = crawlerService.addNewRepository(url);
        return ResponseEntity.ok(convertToRepositoryDTO(savedRepo));
    }

    @PostMapping("/{id}/crawl")
    public ResponseEntity<RepositoryDTO> reCrawlRepository(@PathVariable UUID id) {
        Repository repo = repositoryService.getById(id);
        Repository updatedRepo = crawlerService.reCrawlRepository(repo.getUrl());
        return ResponseEntity.ok(convertToRepositoryDTO(updatedRepo));
    }

    @GetMapping("/{id}/issues")
    public ResponseEntity<List<IssueDTO>> getRepositoryIssues(
            @PathVariable UUID id,
            @RequestParam(required = false) String state) {

        List<Issue> issues = issueService.findAll().stream()
                .filter(issue -> issue.getRepository().getId().equals(id))
                .collect(Collectors.toList());

        if (state != null && !state.isEmpty()) {
            IssueStatus statusEnum = IssueStatus.valueOf(state.toUpperCase());
            issues = issues.stream()
                    .filter(issue -> issue.getState() == statusEnum)
                    .collect(Collectors.toList());
        }

        List<IssueDTO> result = issues.stream().map(this::convertToIssueDTO).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/authors")
    public ResponseEntity<List<Map<String, Object>>> getRepositoryAuthors(@PathVariable UUID id) {
        List<Issue> repoIssues = issueService.findAll().stream()
                .filter(issue -> issue.getRepository().getId().equals(id))
                .collect(Collectors.toList());

        Map<GitHubUser, Long> authorCounts = repoIssues.stream()
                .collect(Collectors.groupingBy(Issue::getAuthor, Collectors.counting()));

        List<Map<String, Object>> result = authorCounts.entrySet().stream()
                .sorted(Map.Entry.<GitHubUser, Long>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("author", convertToUserDTO(entry.getKey()));
                    map.put("issues_count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private RepositoryDTO convertToRepositoryDTO(Repository repo) {
        return RepositoryDTO.builder()
                .id(repo.getId())
                .ownerId(repo.getOwner() != null ? repo.getOwner().getId() : null)
                .name(repo.getName())
                .url(repo.getUrl())
                .totalIssues(repo.getTotalIssues())
                .crawledAt(repo.getCrawledAt())
                .build();
    }

    private IssueDTO convertToIssueDTO(Issue issue) {
        return IssueDTO.builder()
                .id(issue.getId())
                .repoId(issue.getRepository().getId())
                .authorId(issue.getAuthor().getId())
                .githubId(issue.getGithubId())
                .issueNumber(issue.getIssueNumber())
                .title(issue.getTitle())
                .state(issue.getState())
                .htmlUrl(issue.getHtmlUrl())
                .createdAt(issue.getCreatedAt())
                .build();
    }

    private GitHubUserDTO convertToUserDTO(GitHubUser user) {
        return GitHubUserDTO.builder()
                .id(user.getId())
                .githubId(user.getGithubId())
                .login(user.getLogin())
                .name(user.getName())
                .profileUrl(user.getProfileUrl())
                .build();
    }
}