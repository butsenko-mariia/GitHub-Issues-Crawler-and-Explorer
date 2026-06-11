package app.PresentationLayer.Controllers;

import app.BusinessLayer.Services.AiService;
import app.BusinessLayer.Services.IssueService;
import app.PersistenceLayer.Models.Issue;
import app.PresentationLayer.DTOs.IssueDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "http://localhost:5173")
public class IssueController {

    private final IssueService issueService;
    private final AiService aiService;

    public IssueController(IssueService issueService, AiService aiService) {
        this.issueService = issueService;
        this.aiService = aiService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDTO> getIssueDetails(@PathVariable UUID id) {
        Issue issue = issueService.getById(id);

        IssueDTO dto = IssueDTO.builder()
                .id(issue.getId())
                .repoId(issue.getRepository().getId())
                .authorId(issue.getAuthor().getId())
                .githubId(issue.getGithubId())
                .issueNumber(issue.getIssueNumber())
                .title(issue.getTitle())
                .body(issue.getBody())
                .state(issue.getState())
                .htmlUrl(issue.getHtmlUrl())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .aiSummary(issue.getAiSummary())
                .authorLogin(issue.getAuthor() != null ? issue.getAuthor().getLogin() : "Невідомо")
                .build();

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/summarize")
    public ResponseEntity<IssueDTO> summarizeIssue(@PathVariable UUID id) {
        Issue issue = issueService.getById(id);

        String realSummary = aiService.generateSummary(issue.getTitle(), issue.getBody());

        issue.setAiSummary(realSummary);
        issueService.update(issue.getId(), issue);

        IssueDTO dto = IssueDTO.builder()
                .id(issue.getId())
                .aiSummary(issue.getAiSummary())
                .build();

        return ResponseEntity.ok(dto);
    }
}