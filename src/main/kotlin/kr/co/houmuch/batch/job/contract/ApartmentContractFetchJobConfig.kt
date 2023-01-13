package kr.co.houmuch.batch.job.contract

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.houmuch.batch.config.properties.BatchProperties
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.job.DelegatingJobExecutionListener
import kr.co.houmuch.batch.logger
import kr.co.houmuch.batch.service.contract.ApartmentContractFetchService
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.util.DateUtils
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.json.JsonFileItemWriter
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.data.domain.Sort
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private fun <T, T1> JsonFileItemWriter<T>.write(toList: List<T1>) {
    this.write(toList)
}

@Configuration
class ApartmentContractFetchJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val batchThreadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val delegatingJobExecutionListener: DelegatingJobExecutionListener,
    private val apartmentContractFetchService: ApartmentContractFetchService,
    private val areaCodeJpaRepository: AreaCodeJpaRepository,
    private val apartmentContractFetchJobProcessor: FunctionItemProcessor<AreaCodeJpo, List<BaseApartmentContract>>,
    private val apartmentContractFetchJobWriter: JsonFileItemWriter<List<BaseApartmentContract>>,
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
            .chunk<AreaCodeJpo, List<BaseApartmentContract>>(CHUNK_SIZE)
            .reader(reader())
            .processor(apartmentContractFetchJobProcessor)
            .writer(apartmentContractFetchJobWriter)
            .taskExecutor(batchThreadPoolTaskExecutor)
            .build()
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Reader"])
    fun reader(): ItemReader<AreaCodeJpo> {
        return RepositoryItemReaderBuilder<AreaCodeJpo>()
            .name("${JOB_NAME}Reader")
            .repository(areaCodeJpaRepository)
            .methodName("findSgg")
            .pageSize(1)
            .sorts(mapOf(Pair("id", Sort.Direction.ASC)))
            .build()
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Processor"])
    fun processor(@Value("#{jobParameters[date]}") date: String): FunctionItemProcessor<AreaCodeJpo, List<BaseApartmentContract>> =
        FunctionItemProcessor { areaCode ->
            val yearMonth: YearMonth = stringDateAsYearMonth(date)
            val tradeList = apartmentContractFetchService.fetchAsync(areaCode.getIdBy(0, 5), yearMonthAsInt(yearMonth))
            if (tradeList!!.isEmpty()) {
                log.info("결과 없음 --> 지역코드 : {} {}", areaCode.getIdBy(0, 5), areaCode.address)
            }
            tradeList
        }

    @StepScope
    @Bean(name = ["${JOB_NAME}Writer"])
    fun writer(@Value("#{jobParameters[date]}") date: String): JsonFileItemWriter<List<BaseApartmentContract>> =
        JsonFileItemWriterBuilder<List<BaseApartmentContract>>()
            .name("${JOB_NAME}Writer")
            .resource(FileSystemResource("${batchProperties.jsonOutputDirectory}/${createFilePath(date)}"))
            .jsonObjectMarshaller { list ->
                if (list.isEmpty()) {
                    ""
                } else {
                    list.joinToString(",") { ObjectMapper().writeValueAsString(it) }
                }
            }
            .build()

    private fun createFilePath(date: String) = "${localDateAsPath(stringDateAsYearMonth(date), "yyyy/MM")}.json"
    private fun stringDateAsYearMonth(date: String) = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    private fun yearMonthAsInt(yearMonth: YearMonth) = yearMonth.format(DateUtils.formatter("yyyyMM")).toInt()
    private fun localDateAsPath(yearMonth: YearMonth, format: String) = yearMonth.format(DateUtils.formatter(format))
}
