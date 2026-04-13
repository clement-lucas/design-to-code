package com.example.proman.batch;

import com.example.proman.entity.Project;
import com.example.proman.entity.ProjectsByUser;
import com.example.proman.entity.ProjectsByUserRequest;
import com.example.proman.repository.ProjectRepository;
import com.example.proman.repository.ProjectsByUserRepository;
import com.example.proman.repository.ProjectsByUserRequestRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BA10603: ユーザ別従事プロジェクト一覧出力バッチ
 * Processes pending requests: exports user's projects to CSV (N21AA003 format).
 */
@Configuration
public class UserProjectExportBatchConfig {

    private final ProjectsByUserRequestRepository requestRepository;
    private final ProjectsByUserRepository projectsByUserRepository;
    private final ProjectRepository projectRepository;

    public UserProjectExportBatchConfig(ProjectsByUserRequestRepository requestRepository,
                                         ProjectsByUserRepository projectsByUserRepository,
                                         ProjectRepository projectRepository) {
        this.requestRepository = requestRepository;
        this.projectsByUserRepository = projectsByUserRepository;
        this.projectRepository = projectRepository;
    }

    @Bean
    public Job userProjectExportJob(JobRepository jobRepository, Step userProjectExportStep) {
        return new JobBuilder("userProjectExportJob", jobRepository)
                .start(userProjectExportStep)
                .build();
    }

    @Bean
    public Step userProjectExportStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("userProjectExportStep", jobRepository)
                .tasklet(userProjectExportTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet userProjectExportTasklet() {
        return (contribution, chunkContext) -> {
            // Find all pending requests (status = "01")
            List<ProjectsByUserRequest> requests = requestRepository.findAll()
                    .stream()
                    .filter(r -> "01".equals(r.getStatus()))
                    .toList();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            Path outputDir = Paths.get("batch-output");
            Files.createDirectories(outputDir);

            for (ProjectsByUserRequest request : requests) {
                Integer userId = request.getUserId();
                List<Project> projects = projectRepository.findProjectsByUserId(userId);

                String fileName = "N21AA003_user" + userId + "_" + System.currentTimeMillis() + ".csv";
                Path filePath = outputDir.resolve(fileName);

                try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                    for (Project p : projects) {
                        writer.write(String.join(",",
                                String.valueOf(p.getProjectId()),
                                p.getProjectName(),
                                p.getProjectType(),
                                p.getProjectClass(),
                                p.getProjectStartDate() != null ? p.getProjectStartDate().format(fmt) : "",
                                p.getProjectEndDate() != null ? p.getProjectEndDate().format(fmt) : "",
                                String.valueOf(p.getOrganizationId()),
                                p.getClientId() != null ? String.valueOf(p.getClientId()) : "",
                                p.getProjectManager(),
                                p.getProjectLeader(),
                                p.getSales() != null ? String.valueOf(p.getSales()) : "",
                                p.getNote() != null ? p.getNote() : ""
                        ));
                        writer.newLine();
                    }
                }

                // Update request status to completed ("02")
                request.setStatus("02");
                requestRepository.save(request);

                // Update PROJECTS_BY_USER record
                ProjectsByUser pbu = projectsByUserRepository.findById(userId).orElse(new ProjectsByUser());
                pbu.setUserId(userId);
                pbu.setRequestId(request.getId());
                pbu.setFileName(fileName);
                pbu.setCreateDatetime(LocalDateTime.now());
                projectsByUserRepository.save(pbu);
            }

            return RepeatStatus.FINISHED;
        };
    }
}
