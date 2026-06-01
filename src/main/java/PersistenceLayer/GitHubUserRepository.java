package PersistenceLayer;

import PersistenceLayer.Models.GitHubUser;
import PersistenceLayer.Models.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GitHubUserRepository extends JpaRepository<GitHubUser, UUID> {}