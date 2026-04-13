package com.example.proman.controller;

import com.example.proman.form.ProjectUploadForm;
import com.example.proman.security.LoginUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/project/upload")
public class ProjectUploadController {

    @Value("${app.batch.output-dir:./batch-output}")
    private String uploadDir;

    @GetMapping
    public String init(@ModelAttribute("form") ProjectUploadForm form) {
        return "project/upload";
    }

    @PostMapping("/confirm")
    public String confirm(@ModelAttribute("form") ProjectUploadForm form, Model model) {
        MultipartFile file = form.getProjectFile();
        if (file == null || file.isEmpty()) {
            model.addAttribute("errorMessage", "ファイルが指定されていません。");
            return "project/upload";
        }
        if (file.getSize() > 500 * 1024) {
            model.addAttribute("errorMessage", "ファイルが500KBを超えています。");
            return "project/upload";
        }
        model.addAttribute("fileName", file.getOriginalFilename());
        model.addAttribute("fileSize", file.getSize());
        return "project/confirmUpload";
    }

    @PostMapping("/execute")
    public String execute(@ModelAttribute("form") ProjectUploadForm form,
                          @AuthenticationPrincipal LoginUserDetails user,
                          @RequestParam(value = "back", required = false) String back,
                          Model model) throws IOException {
        if (back != null) {
            return "redirect:/project/upload";
        }
        MultipartFile file = form.getProjectFile();
        if (file == null || file.isEmpty()) {
            model.addAttribute("errorMessage",
                    "プロジェクト一括登録の要求に失敗しました。ファイルアップロードからやり直して下さい。");
            return "project/upload";
        }

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Path target = dir.resolve("upload_" + System.currentTimeMillis() + ".csv");
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "redirect:/project/upload/complete";
    }

    @GetMapping("/complete")
    public String complete() {
        return "project/completeUpload";
    }
}
