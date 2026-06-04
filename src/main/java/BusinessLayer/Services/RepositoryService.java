package BusinessLayer.Services;

import BusinessLayer.Exceptions.ResourceNotFoundException;
import PersistenceLayer.GitHubUserRepository;
import PersistenceLayer.Models.GitHubUser;
import PersistenceLayer.Models.Repository;
import PersistenceLayer.RepositoryRepository;
import jakarta.persistence.Column;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RepositoryService {
    private RepositoryRepository repositoryRepo;

    public RepositoryService(RepositoryRepository repositoryRepo) {
        this.repositoryRepo = repositoryRepo;
    }

    public void create(Repository repository){
        repositoryRepo.save(repository);
    }

    public List<Repository> findAll() {
        return repositoryRepo.findAll();
    }

    public Repository getById(UUID userId){
        return  repositoryRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
    }

    public Repository update(UUID id, Repository details){
        Repository repository =  getById(id);
        repository.setOwnerId(details.getOwnerId());
        repository.setName(details.getName());
        repository.setUrl(details.getUrl());
        repository.setTotalIssues(details.getTotalIssues());
        repository.setCrawledAt(details.getCrawledAt());

        return repositoryRepo.save(repository);
    }

    public void delete(Repository repository){
        repositoryRepo.delete(repository);
    }
}
