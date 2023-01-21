package kr.co.houmuch.batch.job.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.co.houmuch.batch.config.properties.BatchProperties
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.job.DelegatingJobExecutionListener
import kr.co.houmuch.batch.job.contract.mapper.mapping
import kr.co.houmuch.batch.logger
import kr.co.houmuch.batch.service.contract.ApartmentContractProcessService
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.contract.jpa.ContractAdditionalJpo
import kr.co.houmuch.core.domain.contract.jpa.ContractDetailJpo
import kr.co.houmuch.core.domain.contract.jpa.ContractJpo
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.integration.async.AsyncItemProcessor
import org.springframework.batch.integration.async.AsyncItemWriter
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.json.JacksonJsonObjectReader
import org.springframework.batch.item.json.JsonItemReader
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.format.DateTimeFormatter
import java.util.concurrent.Future
import javax.sql.DataSource

@Configuration
class ApartmentContractJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val dataSource: DataSource,
    private val batchThreadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val apartmentContractJobReader: JsonItemReader<BaseApartmentContract>,
    private val apartmentContractJobProcessor: FunctionItemProcessor<BaseApartmentContract, ContractJpo>,
    private val apartmentContractJobAsyncProcessor: AsyncItemProcessor<BaseApartmentContract, ContractJpo>,
    private val apartmentContractJobWriter: ItemWriter<ContractJpo>,
    private val apartmentContractJobAsyncWriter: AsyncItemWriter<ContractJpo>,
    private val delegatingJobExecutionListener: DelegatingJobExecutionListener,
    private val apartmentContractProcessService: ApartmentContractProcessService,
    private val batchProperties: BatchProperties
) {
    val log = logger<ApartmentContractJobConfig>()

    companion object {
        const val JOB_NAME = "apartmentContractJob"
        const val CHUNK_SIZE = 10000
    }

    @Bean(name = [JOB_NAME])
    fun job(@Qualifier("${JOB_NAME}Step") step: Step): Job {
        return jobBuilderFactory.get(JOB_NAME)
            .start(step)
            .listener(delegatingJobExecutionListener)
            .build()
    }

    @JobScope
    @Bean(name = ["${JOB_NAME}Step"])
    fun step(): Step {
        return stepBuilderFactory.get("${JOB_NAME}Step")
            .chunk<BaseApartmentContract, Future<ContractJpo>>(CHUNK_SIZE)
            .reader(apartmentContractJobReader)
            .processor(apartmentContractJobAsyncProcessor)
            .writer(apartmentContractJobAsyncWriter)
            .build()
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Reader"])
    fun reader(@Value("#{jobParameters[date]}") date: String): JsonItemReader<BaseApartmentContract> {
        return JsonItemReaderBuilder<BaseApartmentContract>()
            .name("${JOB_NAME}Reader")
            .resource(FileSystemResource("${batchProperties.jsonOutputDirectory}/${createFilePath(date)}"))
            .jsonObjectReader(JacksonJsonObjectReader(jacksonObjectMapper(), BaseApartmentContract::class.java))
            .build()
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}AsyncProcessor"])
    fun asyncProcessor(): AsyncItemProcessor<BaseApartmentContract, ContractJpo> {
        val processor = AsyncItemProcessor<BaseApartmentContract, ContractJpo>()
        processor.setDelegate(apartmentContractJobProcessor)
        processor.setTaskExecutor(batchThreadPoolTaskExecutor)
        return processor
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Processor"])
    fun processor(@Value("#{jobParameters[date]}") date: String, areaCodeJpaRepository: AreaCodeJpaRepository):
            FunctionItemProcessor<BaseApartmentContract, ContractJpo> = FunctionItemProcessor<BaseApartmentContract, ContractJpo> {
        val isContainsDate = it.contractedAt.asLocalDate().isEqual(stringDateAsLocalDate(date))
        if (isContainsDate) {
            val areaCode = apartmentContractProcessService.processAreaCode(it)
            val building = apartmentContractProcessService.processBuilding(it, areaCode)
            val contractJpo = mapping(it)
            contractJpo.building = building
            contractJpo
        } else {
            null
        }
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}AsyncWriter"])
    fun asyncWriter(): AsyncItemWriter<ContractJpo> {
        val writer = AsyncItemWriter<ContractJpo>()
        writer.setDelegate(apartmentContractJobWriter)
        return writer
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Writer"])
    fun writer(): ItemWriter<ContractJpo> {
        return ItemWriter { contractList ->
            val contractWriter = JdbcBatchItemWriterBuilder<ContractJpo>()
                .sql("INSERT INTO contract (id, type, contracted_at, serial_number, building_id) " +
                        "VALUES (?, ?, ?, ?, ?)")
                .itemPreparedStatementSetter { item, ps ->
                    ps.setString(1, item.id)
                    ps.setString(2, item.type.name)
                    ps.setString(3, item.contractedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    ps.setString(4, item.serialNumber)
                    ps.setString(5, item.building.id)
                }
                .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
                .dataSource(dataSource)
                .build()
            val contractDetailWriter = JdbcBatchItemWriterBuilder<ContractDetailJpo>()
                .sql("INSERT INTO contract_detail (id, price, monthly_price, floor) " +
                        "VALUES (:id, :price, :monthlyPrice, :floor)")
                .beanMapped()
                .dataSource(dataSource)
                .build()
            contractDetailWriter.afterPropertiesSet()
            val contractAdditionalWriter = JdbcBatchItemWriterBuilder<ContractAdditionalJpo>()
                .sql("INSERT INTO contract_additional " +
                        "(id, contract_type, term, use_refresh_claim, previous_price, previous_monthly_price, release_at, `release`) " +
                        "VALUES (:id, :contractType, :term, :useRefreshClaim, :previousPrice, :previousMonthlyPrice, :releaseAt, :release)")
                .beanMapped()
                .dataSource(dataSource)
                .build()
            contractAdditionalWriter.afterPropertiesSet()
            contractWriter.write(contractList)
            contractDetailWriter.write(contractList.map { it.detail })
            contractAdditionalWriter.write(contractList.map { it.additional })
        }
    }
}

