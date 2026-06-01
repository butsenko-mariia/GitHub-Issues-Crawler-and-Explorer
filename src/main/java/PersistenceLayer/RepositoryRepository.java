package PersistenceLayer;

import PersistenceLayer.Models.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepositoryRepository extends JpaRepository<Repository, UUID> {}