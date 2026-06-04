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
@CrossOrigin(origins = "*")
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
                .body(issue.getBody()) // ТУТ передаємо повний текст тікета
                .state(issue.getState())
                .htmlUrl(issue.getHtmlUrl())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .aiSummary(issue.getAiSummary())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/summarize")
    public ResponseEntity<IssueDTO> summarizeIssue(@PathVariable UUID id) {
        Issue issue = issueService.getById(id);

        // Викликаємо СПРАВЖНІЙ ШІ, передаючи заголовок та опис тікета
        String realSummary = aiService.generateSummary(issue.getTitle(), issue.getBody());

        // Зберігаємо згенерований підсумок в базу даних (Вимога Бонус 4)
        issue.setAiSummary(realSummary);
        issueService.update(issue.getId(), issue);

        // Повертаємо оновлений DTO користувачу
        IssueDTO dto = IssueDTO.builder()
                .id(issue.getId())
                .aiSummary(issue.getAiSummary())
                .build();

        return ResponseEntity.ok(dto);
    }
}