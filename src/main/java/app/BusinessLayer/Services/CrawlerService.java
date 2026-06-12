package app.BusinessLayer.Services;

import app.BusinessLayer.ExternalAPI.GitHubApiClient;
import app.PersistenceLayer.GitHubUserRepository;
import app.PersistenceLayer.RepositoryRepository;
import app.PersistenceLayer.Models.GitHubUser;
import app.PersistenceLayer.Models.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Map;

@Service
public class CrawlerService {

    private final RepositoryRepository repositoryRepo;
    private final GitHubUserRepository userRepo;
    private final GitHubApiClient gitHubApiClient;
    private final AsyncCrawlerTask asyncCrawlerTask;

    public CrawlerService(RepositoryRepository repositoryRepo,
                          GitHubUserRepository userRepo,
                          GitHubApiClient gitHubApiClient,
                          AsyncCrawlerTask asyncCrawlerTask) {
        this.repositoryRepo = repositoryRepo;
        this.userRepo = userRepo;
        this.gitHubApiClient = gitHubApiClient;
        this.asyncCrawlerTask = asyncCrawlerTask;
    }

    @Transactional
    public Repository addNewRepository(String url) {
        if (repositoryRepo.findByUrl(url).isPresent()) {
            throw new IllegalStateException("Цей репозиторій вже був доданий до системи.");
        }
        return processCrawlingStart(url);
    }

    @Transactional
    public Repository reCrawlRepository(String url) {
        if (repositoryRepo.findByUrl(url).isEmpty()) {
            throw new IllegalArgumentException("Репозиторій не знайдено. Спочатку додайте його.");
        }
        return processCrawlingStart(url);
    }

    private Repository processCrawlingStart(String url) {
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

        asyncCrawlerTask.runCrawlingInBackground(repository, ownerName, repoName);

        return repository;
    }

    private int fetchRepositoryStats(String owner, String repo) {
        String repoApiUrl = "https://api.github.com/repos/" + owner + "/" + repo;
        try {
            Map<String, Object> repoData = gitHubApiClient.get(repoApiUrl, Map.class);
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

    private GitHubUser getOrCreateUserFromApi(String username) {
        String profileUrl = "https://github.com/" + username;
        return userRepo.findByProfileUrl(profileUrl).orElseGet(() -> {
            try {
                String apiUrl = "https://api.github.com/users/" + username;
                Map<String, Object> userData = gitHubApiClient.get(apiUrl, Map.class);

                GitHubUser user = new GitHubUser();
                user.setLogin(username);
                user.setProfileUrl(profileUrl);

                if (userData != null) {
                    user.setGithubId(new BigInteger(userData.get("id").toString()));
                    Object nameObj = userData.get("name");
                    user.setName(nameObj != null ? nameObj.toString() : username);
                } else {
                    user.setGithubId(BigInteger.valueOf(Math.abs((long) username.hashCode())));
                    user.setName(username);
                }
                return userRepo.save(user);

            } catch (Exception e) {
                GitHubUser user = new GitHubUser();
                user.setGithubId(BigInteger.valueOf(Math.abs((long) username.hashCode())));
                user.setLogin(username);
                user.setName(username);
                user.setProfileUrl(profileUrl);
                return userRepo.save(user);
            }
        });
    }
}