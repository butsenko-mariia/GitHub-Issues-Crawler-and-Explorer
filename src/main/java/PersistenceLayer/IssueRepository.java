package PersistenceLayer;

import PersistenceLayer.Enums.IssueStatus;
import PersistenceLayer.Models.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    Optional<Issue> findByGithubId(BigInteger githubId);

    List<Issue> findByRepositoryId(UUID repositoryId);

    List<Issue> findByRepositoryIdAndState(UUID repositoryId, IssueStatus state);

    @Query("SELECT i.author.login FROM Issue i WHERE i.repository.id = :repoId GROUP BY i.author.id ORDER BY COUNT(i) DESC LIMIT 1")
    Optional<String> findTopAuthorLoginByRepositoryId(@Param("repoId") UUID repoId);

    @Query("SELECT COUNT(DISTINCT i.author.id) FROM Issue i WHERE i.repository.id = :repoId")
    long countUniqueAuthorsByRepositoryId(@Param("repoId") UUID repoId);
}