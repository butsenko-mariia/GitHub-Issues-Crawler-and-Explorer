package app.BusinessLayer.Services;

import app.PresentationLayer.DTOs.CrawlProgressDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CrawlProgressService {

    private final Map<UUID, CrawlProgressDTO> progressMap = new ConcurrentHashMap<>();

    public void start(UUID repoId) {
        progressMap.put(repoId, CrawlProgressDTO.builder()
                .status("RUNNING")
                .crawledCount(0)
                .currentPage(1)
                .build());
    }

    public void update(UUID repoId, int crawledCount, int currentPage) {
        CrawlProgressDTO current = progressMap.getOrDefault(repoId,
                CrawlProgressDTO.builder().build());
        current.setStatus("RUNNING");
        current.setCrawledCount(crawledCount);
        current.setCurrentPage(currentPage);
        progressMap.put(repoId, current);
    }

    public void done(UUID repoId, int totalCrawled) {
        progressMap.put(repoId, CrawlProgressDTO.builder()
                .status("DONE")
                .crawledCount(totalCrawled)
                .build());
    }

    public void error(UUID repoId, String message) {
        CrawlProgressDTO current = progressMap.getOrDefault(repoId,
                CrawlProgressDTO.builder().build());
        current.setStatus("ERROR");
        current.setMessage(message);
        progressMap.put(repoId, current);
    }

    public CrawlProgressDTO getProgress(UUID repoId) {
        return progressMap.getOrDefault(repoId, CrawlProgressDTO.builder()
                .status("IDLE")
                .crawledCount(0)
                .build());
    }

    public boolean isRunning(UUID repoId) {
        CrawlProgressDTO progress = progressMap.get(repoId);
        return progress != null && "RUNNING".equals(progress.getStatus());
    }
}