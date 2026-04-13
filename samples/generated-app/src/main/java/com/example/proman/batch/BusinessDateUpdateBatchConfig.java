package com.example.proman.batch;

import com.example.proman.entity.BusinessDate;
import com.example.proman.repository.BusinessDateRepository;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * BA10701: 業務日付更新バッチ
 * Updates the BUSINESS_DATE table segment "01" to the current system date.
 */
@Configuration
public class BusinessDateUpdateBatchConfig {

    private final BusinessDateRepository businessDateRepository;

    public BusinessDateUpdateBatchConfig(BusinessDateRepository businessDateRepository) {
        this.businessDateRepository = businessDateRepository;
    }

    @Bean
    public Job businessDateUpdateJob(JobRepository jobRepository, Step businessDateUpdateStep) {
        return new JobBuilder("businessDateUpdateJob", jobRepository)
                .start(businessDateUpdateStep)
                .build();
    }

    @Bean
    public Step businessDateUpdateStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("businessDateUpdateStep", jobRepository)
                .tasklet(businessDateUpdateTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet businessDateUpdateTasklet() {
        return (contribution, chunkContext) -> {
            BusinessDate bd = businessDateRepository.findById("01")
                    .orElse(new BusinessDate());
            bd.setSegmentId("01");
            bd.setBizDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            businessDateRepository.save(bd);
            return RepeatStatus.FINISHED;
        };
    }
}
