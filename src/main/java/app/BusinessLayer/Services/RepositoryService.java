package app.BusinessLayer.Services;

import app.BusinessLayer.Exceptions.ResourceNotFoundException;
import app.PersistenceLayer.Models.Repository;
import app.PersistenceLayer.RepositoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RepositoryService {
    private RepositoryRepository repositoryRepo;

    public RepositoryService(RepositoryRepository repositoryRepo) {
        this.repositoryRepo = repositoryRepo;
    }

    public void create(Repository repository) {
        repositoryRepo.save(repository);
    }

    @Transactional(readOnly = true)
    public List<Repository> findAll() {
        return repositoryRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Repository getById(UUID id) {
        return repositoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Репозиторій не знайдено"));
    }

    @Transactional
    public Repository update(UUID id, Repository details) {
        Repository repository = getById(id);
        repository.setOwner(details.getOwner());
        repository.setName(details.getName());
        repository.setUrl(details.getUrl());
        repository.setTotalIssues(details.getTotalIssues());
        repository.setCrawledAt(details.getCrawledAt());
        return repositoryRepo.save(repository);
    }

    public void delete(Repository repository) {
        repositoryRepo.delete(repository);
    }
}