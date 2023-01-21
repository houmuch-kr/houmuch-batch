package kr.co.houmuch.batch.job.coordinate

import kr.co.houmuch.batch.job.DelegatingJobExecutionListener
import kr.co.houmuch.batch.logger
import kr.co.houmuch.batch.service.coordinate.BuildingCoordinateFetchService
import kr.co.houmuch.core.domain.building.jpa.BuildingCoordinateJpo
import kr.co.houmuch.core.domain.building.jpa.BuildingJpo
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.integration.async.AsyncItemProcessor
import org.springframework.batch.integration.async.AsyncItemWriter
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Future
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

const val JOB_NAME = "buildingCoordinateJob"
const val CHUNK_SIZE = 100

@Configuration
class BuildingCoordinateJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val delegatingJobExecutionListener: DelegatingJobExecutionListener,
    private val batchThreadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val buildingCoordinateJobReader: JpaCursorItemReader<BuildingJpo>,
    private val buildingCoordinateJobProcessor: FunctionItemProcessor<BuildingJpo, BuildingCoordinateJpo>,
    private val buildingCoordinateJobWriter: JdbcBatchItemWriter<BuildingCoordinateJpo>,
    private val buildingCoordinateJobAsyncProcessor: AsyncItemProcessor<BuildingJpo, BuildingCoordinateJpo>,
    private val buildingCoordinateJobAsyncWriter: AsyncItemWriter<BuildingCoordinateJpo>,
    private val dataSource: DataSource,
    private val buildingCoordinateFetchService: BuildingCoordinateFetchService,
    private val entityManagerFactory: EntityManagerFactory,
) {
    val log = logger<BuildingCoordinateJobConfig>()

    @Bean(name = [JOB_NAME])
    fun job(): Job = jobBuilderFactory.get(JOB_NAME)
        .start(step())
        .listener(delegatingJobExecutionListener)
        .build();

    @JobScope
    @Bean(name = ["${JOB_NAME}Step"])
    fun step(): Step = stepBuilderFactory.get("${JOB_NAME}Step")
        .chunk<BuildingJpo, Future<BuildingCoordinateJpo>>(CHUNK_SIZE)
        .reader(buildingCoordinateJobReader)
        .processor(buildingCoordinateJobAsyncProcessor)
        .writer(buildingCoordinateJobAsyncWriter)
        .build()

    @StepScope
    @Bean(name = ["${JOB_NAME}Reader"])
    fun reader(): JpaCursorItemReader<BuildingJpo> = JpaCursorItemReaderBuilder<BuildingJpo>()
        .name("${JOB_NAME}Reader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT b FROM BuildingJpo b " +
                "INNER JOIN FETCH b.areaCode ba " +
                "LEFT OUTER JOIN FETCH ba.coordinate " +
                "LEFT OUTER JOIN FETCH b.coordinate bc " +
                "WHERE bc.id IS NULL " +
                "ORDER BY b.id")
        .build()

    @StepScope
    @Bean(name = ["${JOB_NAME}AsyncProcessor"])
    fun asyncProcessor(): AsyncItemProcessor<BuildingJpo, BuildingCoordinateJpo> {
        val processor = AsyncItemProcessor<BuildingJpo, BuildingCoordinateJpo>()
        processor.setDelegate(buildingCoordinateJobProcessor)
        processor.setTaskExecutor(batchThreadPoolTaskExecutor)
        return processor
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}AsyncWriter"])
    fun asyncWriter(): AsyncItemWriter<BuildingCoordinateJpo> {
        val writer = AsyncItemWriter<BuildingCoordinateJpo>()
        writer.setDelegate(buildingCoordinateJobWriter)
        return writer
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Processor"])
    fun processor(): FunctionItemProcessor<BuildingJpo, BuildingCoordinateJpo> = FunctionItemProcessor {
        val buildingCoordinate: BuildingCoordinateJpo? = buildingCoordinateFetchService.fetch(it)
        buildingCoordinate?.id = it.id
        buildingCoordinate
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Writer"])
    fun writer(): JdbcBatchItemWriter<BuildingCoordinateJpo> = JdbcBatchItemWriterBuilder<BuildingCoordinateJpo>()
        .sql("INSERT INTO building_coordinate (building_id, latitude, longitude) " +
                "VALUES (?, ?, ?)")
        .itemPreparedStatementSetter { item, ps ->
            ps.setString(1, item.id)
            ps.setDouble(2, item.coordinate.latitude)
            ps.setDouble(3, item.coordinate.longitude)
        }
        .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
        .dataSource(dataSource)
        .build()
}
