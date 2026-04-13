package com.example.proman.batch;

import com.example.proman.entity.Project;
import com.example.proman.entity.ProjectWork;
import com.example.proman.repository.ProjectRepository;
import com.example.proman.repository.ProjectWorkRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * BA10602: プロジェクト一括取込バッチ
 * Step 1: CSV → PROJECT_WORK (work table)
 * Step 2: PROJECT_WORK → PROJECT (main table)
 */
@Configuration
public class ProjectImportBatchConfig {

    private final ProjectWorkRepository projectWorkRepository;
    private final ProjectRepository projectRepository;

    public ProjectImportBatchConfig(ProjectWorkRepository projectWorkRepository,
                                     ProjectRepository projectRepository) {
        this.projectWorkRepository = projectWorkRepository;
        this.projectRepository = projectRepository;
    }

    @Bean
    public Job projectImportJob(JobRepository jobRepository,
                                 Step csvToWorkStep,
                                 Step workToMainStep) {
        return new JobBuilder("projectImportJob", jobRepository)
                .start(csvToWorkStep)
                .next(workToMainStep)
                .build();
    }

    // ==================== Step 1: CSV → PROJECT_WORK ====================

    @Bean
    public Step csvToWorkStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("csvToWorkStep", jobRepository)
                .<ProjectWork, ProjectWork>chunk(100, transactionManager)
                .reader(csvProjectReader())
                .writer(projectWorkWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<ProjectWork> csvProjectReader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("projectName", "projectType", "projectClass",
                "projectStartDate", "projectEndDate", "organizationId",
                "clientId", "projectManager", "projectLeader", "sales", "note");

        DefaultLineMapper<ProjectWork> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            ProjectWork pw = new ProjectWork();
            pw.setProjectName(fieldSet.readString("projectName"));
            pw.setProjectType(fieldSet.readString("projectType"));
            pw.setProjectClass(fieldSet.readString("projectClass"));
            String startDate = fieldSet.readString("projectStartDate");
            if (!startDate.isEmpty()) {
                pw.setProjectStartDate(java.time.LocalDate.parse(startDate,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            }
            String endDate = fieldSet.readString("projectEndDate");
            if (!endDate.isEmpty()) {
                pw.setProjectEndDate(java.time.LocalDate.parse(endDate,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            }
            String orgId = fieldSet.readString("organizationId");
            if (!orgId.isEmpty()) {
                pw.setOrganizationId(Integer.parseInt(orgId));
            }
            String clientId = fieldSet.readString("clientId");
            if (!clientId.isEmpty()) {
                pw.setClientId(Integer.parseInt(clientId));
            }
            pw.setProjectManager(fieldSet.readString("projectManager"));
            pw.setProjectLeader(fieldSet.readString("projectLeader"));
            String sales = fieldSet.readString("sales");
            if (!sales.isEmpty()) {
                pw.setSales(Integer.parseInt(sales));
            }
            pw.setNote(fieldSet.readString("note"));
            return pw;
        });

        return new FlatFileItemReaderBuilder<ProjectWork>()
                .name("csvProjectReader")
                .resource(new FileSystemResource("batch-input/N21AA002.csv"))
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public ItemWriter<ProjectWork> projectWorkWriter() {
        return items -> {
            for (ProjectWork pw : items) {
                projectWorkRepository.save(pw);
            }
        };
    }

    // ==================== Step 2: PROJECT_WORK → PROJECT ====================

    @Bean
    public Step workToMainStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new StepBuilder("workToMainStep", jobRepository)
                .<ProjectWork, Project>chunk(100, transactionManager)
                .reader(projectWorkReader())
                .processor(workToProjectProcessor())
                .writer(projectMainWriter())
                .build();
    }

    @Bean
    public ItemReader<ProjectWork> projectWorkReader() {
        return new org.springframework.batch.item.support.ListItemReader<>(
                projectWorkRepository.findAll());
    }

    @Bean
    public ItemProcessor<ProjectWork, Project> workToProjectProcessor() {
        return work -> {
            Project p = new Project();
            p.setProjectName(work.getProjectName());
            p.setProjectType(work.getProjectType());
            p.setProjectClass(work.getProjectClass());
            p.setProjectStartDate(work.getProjectStartDate());
            p.setProjectEndDate(work.getProjectEndDate());
            p.setOrganizationId(work.getOrganizationId());
            p.setClientId(work.getClientId());
            p.setProjectManager(work.getProjectManager());
            p.setProjectLeader(work.getProjectLeader());
            p.setSales(work.getSales());
            p.setNote(work.getNote());
            p.setVersionNo(0L);
            return p;
        };
    }

    @Bean
    public ItemWriter<Project> projectMainWriter() {
        return items -> {
            for (Project p : items) {
                projectRepository.save(p);
            }
        };
    }
}
