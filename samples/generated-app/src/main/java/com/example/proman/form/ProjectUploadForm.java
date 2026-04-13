package com.example.proman.form;

import org.springframework.web.multipart.MultipartFile;

public class ProjectUploadForm {

    private MultipartFile projectFile;

    public MultipartFile getProjectFile() { return projectFile; }
    public void setProjectFile(MultipartFile projectFile) { this.projectFile = projectFile; }
}
