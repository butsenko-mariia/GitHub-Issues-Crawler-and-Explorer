package app.PersistenceLayer;

import app.PersistenceLayer.Models.GitHubUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.math.BigInteger;
import java.util.UUID;

@Repository
public interface GitHubUserRepository extends JpaRepository<GitHubUser, UUID> {
    Optional<GitHubUser> findByGithubId(BigInteger githubId);
    Optional<GitHubUser> findByProfileUrl(String profileUrl);
}