package app.PersistenceLayer.Models;

import app.PersistenceLayer.Enums.IssueStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private Repository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private GitHubUser author;

    @Column(name = "github_id", unique = true, nullable = false)
    private BigInteger githubId;
    @Column(name = "issue_number", nullable = false)
    private int issueNumber;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus state;

    @Column(name = "html_url", unique = true,  nullable = false)
    private String htmlUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;
}
