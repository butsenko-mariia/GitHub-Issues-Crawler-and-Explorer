package BusinessLayer.Services;

import BusinessLayer.Exceptions.ResourceNotFoundException;
import PersistenceLayer.Enums.IssueStatus;
import PersistenceLayer.GitHubUserRepository;
import PersistenceLayer.IssueRepository;
import PersistenceLayer.Models.GitHubUser;
import PersistenceLayer.Models.Issue;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
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
        issue.setAuthorId(details.getAuthorId());
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
}
