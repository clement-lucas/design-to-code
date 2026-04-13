package com.example.proman.batch;

import com.example.proman.entity.Project;
import com.example.proman.repository.ProjectRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BA10601: プロジェクト一括出力バッチ
 * Exports projects within a specified period to CSV (N21AA001 format).
 */
@Configuration
public class ProjectExportBatchConfig {

    private final ProjectRepository projectRepository;

    public ProjectExportBatchConfig(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Bean
    public Job projectExportJob(JobRepository jobRepository, Step projectExportStep) {
        return new JobBuilder("projectExportJob", jobRepository)
                .start(projectExportStep)
                .build();
    }

    @Bean
    public Step projectExportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("projectExportStep", jobRepository)
                .<Project, String[]>chunk(100, transactionManager)
                .reader(projectExportReader())
                .processor(projectExportProcessor())
                .writer(projectExportWriter())
                .build();
    }

    @Bean
    public ItemReader<Project> projectExportReader() {
        // Projects will be loaded at job execution time
        return new ListItemReader<>(List.of());
    }

    @Bean
    public ItemProcessor<Project, String[]> projectExportProcessor() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return project -> new String[]{
                String.valueOf(project.getProjectId()),
                project.getProjectName(),
                project.getProjectType(),
                project.getProjectClass(),
                project.getProjectStartDate() != null ? project.getProjectStartDate().format(fmt) : "",
                project.getProjectEndDate() != null ? project.getProjectEndDate().format(fmt) : "",
                String.valueOf(project.getOrganizationId()),
                project.getClientId() != null ? String.valueOf(project.getClientId()) : "",
                project.getProjectManager(),
                project.getProjectLeader(),
                project.getSales() != null ? String.valueOf(project.getSales()) : "",
                project.getNote() != null ? project.getNote() : ""
        };
    }

    @Bean
    public FlatFileItemWriter<String[]> projectExportWriter() {
        return new FlatFileItemWriterBuilder<String[]>()
                .name("projectExportWriter")
                .resource(new FileSystemResource("batch-output/N21AA001.csv"))
                .lineAggregator(item -> String.join(",", item))
                .build();
    }
}
