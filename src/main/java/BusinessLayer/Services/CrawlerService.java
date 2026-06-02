package BusinessLayer.Services;

import BusinessLayer.ExternalAPI.GitHubIssueResponse;
import PersistenceLayer.Enums.IssueStatus;
import PersistenceLayer.GitHubUserRepository;
import PersistenceLayer.IssueRepository;
import PersistenceLayer.Models.GitHubUser;
import PersistenceLayer.Models.Issue;
import PersistenceLayer.Models.Repository;
import PersistenceLayer.RepositoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

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
        this.restTemplate = new RestTemplate(); // Інструмент для HTTP запитів
    }

    // Головний метод, який робить усю магію.
    // @Transactional гарантує, що якщо щось зламається посеред процесу, база даних не запише напівпорожні дані.
    @Transactional
    public Repository crawlRepository(String url) {
        // 1. Витягуємо owner та repo з посилання (наприклад, з https://github.com/facebook/react)
        String cleanUrl = url.replace("https://github.com/", "");
        String[] parts = cleanUrl.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Неправильний URL. Формат має бути: https://github.com/owner/repo");
        }
        String ownerName = parts[0];
        String repoName = parts[1];

        // 2. Створюємо або дістаємо існуючого власника репозиторію
        GitHubUser repoOwner = getOrCreateUserFromApi(ownerName);

        // 3. Дістаємо загальну статистику репо з GitHub API (щоб дізнатися totalIssues)
        String repoApiUrl = "https://api.github.com/repos/" + ownerName + "/" + repoName;
        int totalIssuesCount = 0;
        try {
            Map<String, Object> repoData = restTemplate.getForObject(repoApiUrl, Map.class);
            if (repoData != null && repoData.containsKey("open_issues_count")) {
                totalIssuesCount = (Integer) repoData.get("open_issues_count");
            }
        } catch (Exception e) {
            System.out.println("Не вдалося отримати статистику репозиторію: " + e.getMessage());
        }

        // 4. Шукаємо цей репозиторій в нашій БД. Якщо немає — створюємо новий.
        // (Тут передбачається, що в RepositoryRepository ти додала метод Optional<Repository> findByUrl(String url);)
        Repository repository = repositoryRepo.findByUrl(url)
                .orElse(new Repository());

        repository.setUrl(url);
        repository.setName(repoName);
        repository.setOwner(repoOwner); // Використовуємо об'єкт (ManyToOne), як ми обговорювали раніше!
        repository.setTotalIssues(totalIssuesCount);
        repository.setCrawledAt(LocalDate.now());

        repositoryRepo.save(repository);

        // 5. Завантажуємо самі issues (беремо перші 100 штук для прикладу)
        String issuesApiUrl = "https://api.github.com/repos/" + ownerName + "/" + repoName + "/issues?state=all&per_page=100";
        GitHubIssueResponse[] issuesFromApi = restTemplate.getForObject(issuesApiUrl, GitHubIssueResponse[].class);

        if (issuesFromApi != null) {
            for (GitHubIssueResponse apiIssue : issuesFromApi) {
                // GitHub віддає Pull Requests разом з Issues. Якщо є ключ pull_request, це не ішю, пропускаємо.
                // (Для спрощення зараз обробляємо все, але май на увазі)

                // Знаходимо або створюємо автора тікета
                GitHubUser author = getOrCreateUser(apiIssue.getUser());

                // 6. Перевірка на дублікати (Вимога 8: "re-crawl without creating duplicate issues")
                // (Тут треба додати метод Optional<Issue> findByGithubId(BigInteger githubId) у IssueRepository)
                Issue issue = issueRepo.findByGithubId(apiIssue.getId())
                        .orElse(new Issue());

                issue.setGithubId(apiIssue.getId());
                issue.setIssueNumber(apiIssue.getNumber());
                issue.setTitle(apiIssue.getTitle());
                issue.setBody(apiIssue.getBody());
                issue.setState(IssueStatus.valueOf(apiIssue.getState().toUpperCase()));
                issue.setHtmlUrl(apiIssue.getHtmlUrl());

                // Конвертуємо ZonedDateTime від GitHub у твій LocalDate
                issue.setCreatedAt(apiIssue.getCreatedAt().toLocalDate());
                if (apiIssue.getUpdatedAt() != null) {
                    issue.setUpdatedAt(apiIssue.getUpdatedAt().toLocalDate());
                }

                issue.setAuthor(author); // ManyToOne зв'язок
                issue.setRepository(repository); // ManyToOne зв'язок

                issueRepo.save(issue);
            }
        }

        return repository;
    }

    // --- Допоміжні методи ---

    private GitHubUser getOrCreateUser(GitHubIssueResponse.GitHubUserResponse apiUser) {
        // Шукаємо юзера в базі
        Optional<GitHubUser> existingUser = userRepo.findByGithubId(apiUser.getId());
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Якщо немає - створюємо
        GitHubUser newUser = new GitHubUser();
        newUser.setGithubId(apiUser.getId());
        newUser.setLogin(apiUser.getLogin());
        newUser.setName(apiUser.getLogin()); // GitHub Issue API не віддає повне ім'я, використовуємо логін
        newUser.setProfileUrl(apiUser.getHtmlUrl());
        return userRepo.save(newUser);
    }

    private GitHubUser getOrCreateUserFromApi(String username) {
        // Це спрощений метод для створення власника репозиторію.
        // В ідеалі теж треба зробити запит до https://api.github.com/users/{username}
        // Але для тесту можна створити базовий об'єкт.
        String profileUrl = "https://github.com/" + username;
        return userRepo.findByProfileUrl(profileUrl).orElseGet(() -> {
            GitHubUser user = new GitHubUser();
            user.setGithubId(BigInteger.valueOf(username.hashCode())); // Тимчасовий ID, бо ми не робили запит
            user.setLogin(username);
            user.setName(username);
            user.setProfileUrl(profileUrl);
            return userRepo.save(user);
        });
    }
}