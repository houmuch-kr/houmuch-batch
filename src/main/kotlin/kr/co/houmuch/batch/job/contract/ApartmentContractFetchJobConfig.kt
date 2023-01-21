package kr.co.houmuch.batch.job.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.co.houmuch.batch.config.properties.BatchProperties
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.job.DelegatingJobExecutionListener
import kr.co.houmuch.batch.logger
import kr.co.houmuch.batch.service.contract.ApartmentContractFetchService
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.json.JsonFileItemWriter
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder
import org.springframework.batch.item.support.PassThroughItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource

@Configuration
class ApartmentContractFetchJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val delegatingJobExecutionListener: DelegatingJobExecutionListener,
    private val apartmentContractFetchService: ApartmentContractFetchService,
    private val areaCodeJpaRepository: AreaCodeJpaRepository,
    private val apartmentContractFetchJobReader: RestApartmentContractItemReader,
    private val apartmentContractFetchJobProcessor: PassThroughItemProcessor<MutableList<BaseApartmentContract>>,
    private val apartmentContractFetchJobWriter: JsonFileItemWriter<MutableList<BaseApartmentContract>>,
    private val batchProperties: BatchProperties
) {
    val log = logger<ApartmentContractFetchJobConfig>()

    companion object {
        const val JOB_NAME = "apartmentContractFetchJob"
        const val CHUNK_SIZE = 1
    }

    @Bean(name = [JOB_NAME])
    fun job(): Job {
        return jobBuilderFactory.get(JOB_NAME)
            .start(step())
            .listener(delegatingJobExecutionListener)
            .build();
    }

    @JobScope
    @Bean(name = ["${JOB_NAME}Step"])
    fun step(): Step {
        return stepBuilderFactory.get("${JOB_NAME}Step")
            .chunk<MutableList<BaseApartmentContract>, MutableList<BaseApartmentContract>>(CHUNK_SIZE)
            .reader(apartmentContractFetchJobReader)
            .processor(apartmentContractFetchJobProcessor)
            .writer(apartmentContractFetchJobWriter)
            .build()
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Reader"])
    fun reader(@Value("#{jobParameters[date]}") date: String): RestApartmentContractItemReader {
        return RestApartmentContractItemReader(areaCodeJpaRepository, apartmentContractFetchService, date)
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Processor"])
    fun processor(@Value("#{jobParameters[date]}") date: String) = PassThroughItemProcessor<MutableList<BaseApartmentContract>>()

    @StepScope
    @Bean(name = ["${JOB_NAME}Writer"])
    fun writer(@Value("#{jobParameters[date]}") date: String): JsonFileItemWriter<MutableList<BaseApartmentContract>> {
        return JsonFileItemWriterBuilder<MutableList<BaseApartmentContract>>()
            .name("${JOB_NAME}Writer")
            .resource(FileSystemResource("${batchProperties.jsonOutputDirectory}/${createFilePath(date)}"))
            .jsonObjectMarshaller { list ->
                list.joinToString { jacksonObjectMapper().writeValueAsString(it) }
            }
            .build()
    }
}
