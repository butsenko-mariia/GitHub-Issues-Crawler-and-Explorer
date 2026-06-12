package app.BusinessLayer.Services;

import app.BusinessLayer.ExternalAPI.GitHubApiClient;
import app.BusinessLayer.ExternalAPI.GitHubIssueResponse;
import app.PersistenceLayer.Enums.IssueStatus;
import app.PersistenceLayer.GitHubUserRepository;
import app.PersistenceLayer.IssueRepository;
import app.PersistenceLayer.Models.GitHubUser;
import app.PersistenceLayer.Models.Issue;
import app.PersistenceLayer.Models.Repository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AsyncCrawlerTask {

    private final GitHubApiClient gitHubApiClient;
    private final CrawlProgressService progressService;
    private final IssueRepository issueRepo;
    private final GitHubUserRepository userRepo;

    public AsyncCrawlerTask(GitHubApiClient gitHubApiClient,
                            CrawlProgressService progressService,
                            IssueRepository issueRepo,
                            GitHubUserRepository userRepo) {
        this.gitHubApiClient = gitHubApiClient;
        this.progressService = progressService;
        this.issueRepo = issueRepo;
        this.userRepo = userRepo;
    }

    @Async
    public void runCrawlingInBackground(Repository repository, String ownerName, String repoName) {
        UUID repoId = repository.getId();
        progressService.start(repoId);

        int page = 1;
        boolean hasMoreIssues = true;
        int totalCrawled = 0;

        try {
            while (hasMoreIssues) {
                String issuesApiUrl = String.format(
                        "https://api.github.com/repos/%s/%s/issues?state=all&per_page=100&page=%d",
                        ownerName, repoName, page);

                GitHubIssueResponse[] issuesFromApi = gitHubApiClient.get(issuesApiUrl, GitHubIssueResponse[].class);

                if (issuesFromApi == null || issuesFromApi.length == 0) {
                    hasMoreIssues = false;
                    break;
                }

                for (GitHubIssueResponse apiIssue : issuesFromApi) {
                    if (apiIssue.getPullRequest() != null) continue;

                    saveIssue(apiIssue, repository); // Зберігаємо проблему
                    totalCrawled++;
                }

                progressService.update(repoId, totalCrawled, page);
                page++;
            }
            progressService.done(repoId, totalCrawled);

        } catch (Exception e) {
            progressService.error(repoId, "Помилка парсингу: " + e.getMessage());
        }
    }

    private void saveIssue(GitHubIssueResponse apiIssue, Repository repository) {
        GitHubUser author = getOrCreateUser(apiIssue.getUser());
        Issue issue = issueRepo.findByGithubId(apiIssue.getId()).orElse(new Issue());
        issue.setGithubId(apiIssue.getId());
        issue.setIssueNumber(apiIssue.getNumber());
        issue.setTitle(apiIssue.getTitle());

        String body = apiIssue.getBody() != null ? apiIssue.getBody() : "";
        issue.setBody(body.length() > 5000 ? body.substring(0, 5000) + "..." : body);

        issue.setState(IssueStatus.valueOf(apiIssue.getState().toUpperCase()));
        issue.setHtmlUrl(apiIssue.getHtmlUrl());
        issue.setCreatedAt(apiIssue.getCreatedAt().toLocalDate());
        if (apiIssue.getUpdatedAt() != null) {
            issue.setUpdatedAt(apiIssue.getUpdatedAt().toLocalDate());
        }

        issue.setAuthor(author);
        issue.setRepository(repository);
        issueRepo.save(issue);
    }

    private GitHubUser getOrCreateUser(GitHubIssueResponse.GitHubUserResponse apiUser) {
        if (apiUser == null) return null;
        return userRepo.findByGithubId(apiUser.getId()).orElseGet(() -> {
            GitHubUser newUser = new GitHubUser();
            newUser.setGithubId(apiUser.getId());
            newUser.setLogin(apiUser.getLogin());
            newUser.setName(apiUser.getLogin());
            newUser.setProfileUrl(apiUser.getHtmlUrl());
            return userRepo.save(newUser);
        });
    }
}