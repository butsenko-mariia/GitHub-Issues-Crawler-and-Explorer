package app.PersistenceLayer.Models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table(name = "github_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "github_id", unique = true, nullable = false)
    private BigInteger githubId;
    @Column(nullable = false)
    private String login;
    @Column(nullable = false)
    private String name;
    @Column(name = "profile_url", unique = true,  nullable = false)
    private String profileUrl;
}

