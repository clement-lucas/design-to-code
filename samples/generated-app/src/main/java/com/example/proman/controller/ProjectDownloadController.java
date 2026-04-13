package com.example.proman.controller;

import com.example.proman.entity.ProjectsByUser;
import com.example.proman.entity.ProjectsByUserRequest;
import com.example.proman.security.LoginUserDetails;
import com.example.proman.service.ProjectDownloadService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/project/download")
public class ProjectDownloadController {

    private final ProjectDownloadService downloadService;

    public ProjectDownloadController(ProjectDownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping
    public String init(@AuthenticationPrincipal LoginUserDetails user, Model model) {
        downloadService.findByUserId(user.getUserId()).ifPresent(pbu -> {
            model.addAttribute("projectsByUser", pbu);
        });
        return "project/download";
    }

    @PostMapping("/confirm")
    public String confirm(@AuthenticationPrincipal LoginUserDetails user, Model model) {
        return "project/confirmDownload";
    }

    @PostMapping("/execute")
    public String execute(@AuthenticationPrincipal LoginUserDetails user,
                          @RequestParam(value = "back", required = false) String back,
                          Model model) {
        if (back != null) {
            return "redirect:/project/download";
        }
        downloadService.createRequest(user.getUserId());
        return "redirect:/project/download/complete";
    }

    @GetMapping("/complete")
    public String complete() {
        return "project/completeDownload";
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@AuthenticationPrincipal LoginUserDetails user) {
        ProjectsByUser pbu = downloadService.findByUserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("No download available"));

        Path filePath = Paths.get("batch-output", pbu.getFileName());
        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pbu.getFileName() + "\"")
                .body(resource);
    }
}
