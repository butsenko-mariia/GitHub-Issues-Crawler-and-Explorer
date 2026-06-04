package app.BusinessLayer.Services;

import app.BusinessLayer.Exceptions.ResourceNotFoundException;
import app.PersistenceLayer.Enums.IssueStatus;
import app.PersistenceLayer.IssueRepository;
import app.PersistenceLayer.Models.Issue;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IssueService {
    private IssueRepository issueRepo;

    public IssueService(IssueRepository issueRepo) {
        this.issueRepo = issueRepo;
    }

    public void create(Issue issue){
        issueRepo.save(issue);
    }

    public List<Issue> findAll() {
        return issueRepo.findAll();
    }

    public Issue getById(UUID issueId){
        return  issueRepo.findById(issueId).orElseThrow(() -> new ResourceNotFoundException("Issue not found!"));
    }

    public Issue update(UUID id,  Issue details){
        Issue issue =  getById(id);
        issue.setAuthor(details.getAuthor());
        issue.setGithubId( details.getGithubId() );
        issue.setIssueNumber( details.getIssueNumber() );
        issue.setTitle( details.getTitle() );
        issue.setBody( details.getBody() );
        issue.setState(details.getState());
        issue.setHtmlUrl(details.getHtmlUrl());
        issue.setCreatedAt( details.getCreatedAt() );
        issue.setUpdatedAt( details.getUpdatedAt() );
        issue.setAiSummary( details.getAiSummary() );

        return issueRepo.save(issue);
    }

    public void delete(Issue issue){
        issueRepo.delete(issue);
    }

    public List<Issue> getIssuesByRepository(UUID repoId) {
        return issueRepo.findByRepositoryId(repoId);
    }

    public List<Issue> getIssuesByRepositoryAndStatus(UUID repoId, IssueStatus status) {
        return issueRepo.findByRepositoryIdAndState(repoId, status);
    }

    public String getMostActiveAuthor(UUID repoId) {
        return issueRepo.findTopAuthorLoginByRepositoryId(repoId)
                .orElse("Авторів не знайдено");
    }

    public long getUniqueAuthorsCount(UUID repoId) {
        return issueRepo.countUniqueAuthorsByRepositoryId(repoId);
    }
}
