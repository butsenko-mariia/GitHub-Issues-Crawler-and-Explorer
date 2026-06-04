package BusinessLayer.Services;

import BusinessLayer.Exceptions.ResourceNotFoundException;
import PersistenceLayer.GitHubUserRepository;
import PersistenceLayer.Models.GitHubUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GitHubUserService {
    private GitHubUserRepository gitHubUserRepo;

    public GitHubUserService(GitHubUserRepository gitHubUserRepo) {
        this.gitHubUserRepo = gitHubUserRepo;
    }

    public void create(GitHubUser gitHubUser){
        gitHubUserRepo.save(gitHubUser);
    }

    public List<GitHubUser> findAll() {
        return gitHubUserRepo.findAll();
    }

    public GitHubUser getById(UUID userId){
        return  gitHubUserRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
    }

    public GitHubUser update(UUID id,  GitHubUser details){
        GitHubUser user =  getById(id);
        user.setName(details.getName());
        user.setLogin(details.getLogin());
        user.setProfileUrl(details.getProfileUrl());
        user.setGithubId(details.getGithubId());
        return gitHubUserRepo.save(user);
    }

    public void delete(GitHubUser gitHubUser){
        gitHubUserRepo.delete(gitHubUser);
    }
}
