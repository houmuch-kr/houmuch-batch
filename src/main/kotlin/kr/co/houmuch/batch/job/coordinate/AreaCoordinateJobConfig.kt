package kr.co.houmuch.batch.job.coordinate

import kr.co.houmuch.batch.job.DelegatingJobExecutionListener
import kr.co.houmuch.batch.job.contract.ApartmentContractJobConfig
import kr.co.houmuch.batch.logger
import kr.co.houmuch.batch.service.coordinate.AreaCoordinateFetchService
import kr.co.houmuch.core.domain.area.jpa.AreaCoordinateJpo
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import javax.persistence.EntityManagerFactory

@Configuration
class AreaCoordinateJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val batchThreadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val entityManagerFactory: EntityManagerFactory,
    private val areaCodeJpaRepository: AreaCodeJpaRepository,
    private val areaCoordinateFetchService: AreaCoordinateFetchService,
    private val delegatingJobExecutionListener: DelegatingJobExecutionListener
) {
    val log = logger<ApartmentContractJobConfig>()

    companion object {
        const val JOB_NAME = "areaCoordinateJob"
        const val CHUNK_SIZE = 1000
    }

    @Bean(name = [JOB_NAME])
    fun job(): Job = jobBuilderFactory.get(JOB_NAME)
        .start(step())
        .listener(delegatingJobExecutionListener)
        .build();

    @JobScope
    @Bean(name = ["${JOB_NAME}Step"])
    fun step(): Step = stepBuilderFactory.get("${JOB_NAME}Step")
        .chunk<AreaCodeJpo, AreaCodeJpo>(CHUNK_SIZE)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .taskExecutor(batchThreadPoolTaskExecutor)
        .build()

    @StepScope
    @Bean(name = ["${JOB_NAME}Reader"])
    fun reader(): ItemReader<AreaCodeJpo> =
        RepositoryItemReaderBuilder<AreaCodeJpo>()
            .name("${JOB_NAME}Reader")
            .repository(areaCodeJpaRepository)
            .methodName("findSgg")
            .pageSize(CHUNK_SIZE)
            .sorts(mapOf(Pair("id", Sort.Direction.ASC)))
            .build()

    @StepScope
    @Bean(name = ["${JOB_NAME}Processor"])
    fun processor(): ItemProcessor<AreaCodeJpo, AreaCodeJpo> = ItemProcessor {
        val areaCoordinateJpo: AreaCoordinateJpo = areaCoordinateFetchService.fetch(it)
        it.coordinate = areaCoordinateJpo
        it
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Writer"])
    fun writer(): ItemWriter<AreaCodeJpo> = ItemWriter {
        val jpaItemWriter = JpaItemWriterBuilder<AreaCodeJpo>()
            .entityManagerFactory(entityManagerFactory)
            .build()
        jpaItemWriter.write(it)
    }
}
