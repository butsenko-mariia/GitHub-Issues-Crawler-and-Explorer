package app.BusinessLayer.Services;

import app.BusinessLayer.ExternalAPI.GitHubIssueResponse;
import app.PersistenceLayer.Enums.IssueStatus;
import app.PersistenceLayer.GitHubUserRepository;
import app.PersistenceLayer.IssueRepository;
import app.PersistenceLayer.Models.GitHubUser;
import app.PersistenceLayer.Models.Issue;
import app.PersistenceLayer.Models.Repository;
import app.PersistenceLayer.RepositoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Map;

@Service
public class CrawlerService {

    private final RepositoryRepository repositoryRepo;
    private final IssueRepository issueRepo;
    private final GitHubUserRepository userRepo;
    private final RestTemplate restTemplate;

    public CrawlerService(RepositoryRepository repositoryRepo, IssueRepository issueRepo, GitHubUserRepository userRepo) {
        this.repositoryRepo = repositoryRepo;
        this.issueRepo = issueRepo;
        this.userRepo = userRepo;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public Repository addNewRepository(String url) {
        if (repositoryRepo.findByUrl(url).isPresent()) {
            throw new IllegalStateException("Цей репозиторій вже був доданий до системи.");
        }
        return processCrawling(url);
    }

    @Transactional
    public Repository reCrawlRepository(String url) {
        if (repositoryRepo.findByUrl(url).isEmpty()) {
            throw new IllegalArgumentException("Репозиторій не знайдено. Спочатку додайте його.");
        }
        return processCrawling(url);
    }

    private Repository processCrawling(String url) {
        if (url == null || !url.startsWith("https://github.com/")) {
            throw new IllegalArgumentException("Недійсна URL-адреса. Формат має бути: https://github.com/owner/repo");
        }

        String cleanUrl = url.replace("https://github.com/", "");
        String[] parts = cleanUrl.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Недійсна URL-адреса. Не вдалося визначити власника та назву.");
        }
        String ownerName = parts[0];
        String repoName = parts[1];

        int totalIssuesCount = fetchRepositoryStats(ownerName, repoName);

        GitHubUser repoOwner = getOrCreateUserFromApi(ownerName);
        Repository repository = repositoryRepo.findByUrl(url).orElse(new Repository());
        repository.setUrl(url);
        repository.setName(repoName);
        repository.setOwner(repoOwner);
        repository.setTotalIssues(totalIssuesCount);
        repository.setCrawledAt(LocalDate.now());
        repository = repositoryRepo.save(repository);

        int page = 1;
        boolean hasMoreIssues = true;

        while (hasMoreIssues) {
            String issuesApiUrl = String.format("https://api.github.com/repos/%s/%s/issues?state=all&per_page=100&page=%d", ownerName, repoName, page);

            try {
                GitHubIssueResponse[] issuesFromApi = restTemplate.getForObject(issuesApiUrl, GitHubIssueResponse[].class);

                if (issuesFromApi == null || issuesFromApi.length == 0) {
                    hasMoreIssues = false;
                    break;
                }

                for (GitHubIssueResponse apiIssue : issuesFromApi) {
                    if (apiIssue.getPullRequest() != null) {
                        continue;
                    }

                    saveIssue(apiIssue, repository);
                }
                page++;

            } catch (HttpClientErrorException e) {
                handleGitHubApiError(e);
            } catch (Exception e) {
                throw new RuntimeException("Дані не вдалося зберегти: " + e.getMessage());
            }
        }

        return repository;
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

    private int fetchRepositoryStats(String owner, String repo) {
        String repoApiUrl = "https://api.github.com/repos/" + owner + "/" + repo;
        try {
            Map<String, Object> repoData = restTemplate.getForObject(repoApiUrl, Map.class);
            if (repoData != null && repoData.containsKey("open_issues_count")) {
                return (Integer) repoData.get("open_issues_count");
            }
        } catch (HttpClientErrorException e) {
            handleGitHubApiError(e);
        }
        return 0;
    }

    private void handleGitHubApiError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new IllegalArgumentException("Репозиторій не існує, є приватним або недоступним.");
        } else if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new RuntimeException("Досягнуто ліміту запитів (rate limit) GitHub. Спробуйте пізніше.");
        } else {
            throw new RuntimeException("Запит до GitHub завершився невдачею: " + e.getMessage());
        }
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

    private GitHubUser getOrCreateUserFromApi(String username) {
        String profileUrl = "https://github.com/" + username;
        return userRepo.findByProfileUrl(profileUrl).orElseGet(() -> {
            GitHubUser user = new GitHubUser();
            user.setGithubId(BigInteger.valueOf(username.hashCode()));
            user.setLogin(username);
            user.setName(username);
            user.setProfileUrl(profileUrl);
            return userRepo.save(user);
        });
    }
}