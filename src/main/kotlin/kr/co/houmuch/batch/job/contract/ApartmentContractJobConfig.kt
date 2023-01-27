package kr.co.houmuch.batch.job.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.co.houmuch.batch.config.properties.BatchProperties
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.job.DelegatingJobExecutionListener
import kr.co.houmuch.batch.logger
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.item.json.JacksonJsonObjectReader
import org.springframework.batch.item.json.JsonItemReader
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource

@Configuration
class ApartmentContractJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val apartmentContractJobBuildingStep: Step,
    private val apartmentContractJobStep: Step,
    private val delegatingJobExecutionListener: DelegatingJobExecutionListener,
    private val batchProperties: BatchProperties
) {
    val log = logger<ApartmentContractJobConfig>()

    companion object {
        const val JOB_NAME = "apartmentContractJob"
    }

    @Bean(name = [JOB_NAME])
    fun job(): Job {
        return jobBuilderFactory.get(JOB_NAME)
            .start(apartmentContractJobBuildingStep)
            .next(apartmentContractJobStep)
            .listener(delegatingJobExecutionListener)
            .build()
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Reader"])
    fun reader(@Value("#{jobParameters[begin]}") begin: String,
               @Value("#{jobParameters[end]}") end: String): JsonItemReader<BaseApartmentContract> {
        return JsonItemReaderBuilder<BaseApartmentContract>()
            .name("${JOB_NAME}Reader")
            .resource(FileSystemResource("${batchProperties.jsonOutputDirectory}/${createFilePath(begin)}"))
            .jsonObjectReader(JacksonJsonObjectReader(jacksonObjectMapper(), BaseApartmentContract::class.java))
            .build()
    }
}

