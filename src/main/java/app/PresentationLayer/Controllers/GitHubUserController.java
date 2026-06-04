package app.PresentationLayer.Controllers;

import app.BusinessLayer.Services.GitHubUserService;
import app.PersistenceLayer.Models.GitHubUser;
import app.PresentationLayer.DTOs.GitHubUserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class GitHubUserController {

    private final GitHubUserService gitHubUserService;

    public GitHubUserController(GitHubUserService gitHubUserService) {
        this.gitHubUserService = gitHubUserService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GitHubUserDTO> getAuthorDetails(@PathVariable UUID id) {
        GitHubUser user = gitHubUserService.getById(id);

        GitHubUserDTO dto = GitHubUserDTO.builder()
                .id(user.getId())
                .githubId(user.getGithubId())
                .login(user.getLogin())
                .name(user.getName())
                .profileUrl(user.getProfileUrl())
                .build();

        return ResponseEntity.ok(dto);
    }
}