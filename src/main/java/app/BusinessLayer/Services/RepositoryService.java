package app.BusinessLayer.Services;

import app.BusinessLayer.Exceptions.ResourceNotFoundException;
import app.PersistenceLayer.Models.Repository;
import app.PersistenceLayer.RepositoryRepository;
import org.springframework.stereotype.Service;

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
        repository.setOwner(details.getOwner());
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
