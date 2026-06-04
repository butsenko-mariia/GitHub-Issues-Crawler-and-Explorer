package app.PersistenceLayer.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "repository")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private GitHubUser owner;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String url;
    @Column(name = "total_issues", nullable = false)
    private int totalIssues;
    @Column(name = "crawled_at", nullable = false)
    private LocalDate crawledAt;
}
