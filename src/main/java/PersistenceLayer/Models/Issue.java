package PersistenceLayer.Models;

import PersistenceLayer.Enums.IssueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "issue")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "repo_id", nullable = false)
    private UUID repoId;
    @Column(name = "author_id", nullable = false)
    private UUID authorId;
    @Column(name = "github_id", unique = true, nullable = false)
    private BigInteger githubId;
    @Column(name = "issue_number", nullable = false)
    private int issueNumber;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String body;
    @Column(nullable = false)
    private IssueStatus state;
    @Column(name = "html_html", unique = true,  nullable = false)
    private String htmlUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;
}
