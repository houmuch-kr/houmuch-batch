package kr.co.houmuch.batch.job.contract.step

import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.job.contract.ApartmentContractJobConfig
import kr.co.houmuch.batch.job.contract.mapper.mapping
import kr.co.houmuch.batch.job.contract.stringDateAsLocalDate
import kr.co.houmuch.batch.service.contract.ApartmentContractProcessService
import kr.co.houmuch.core.domain.contract.jpa.ContractAdditionalJpo
import kr.co.houmuch.core.domain.contract.jpa.ContractDetailJpo
import kr.co.houmuch.core.domain.contract.jpa.ContractJpo
import kr.co.houmuch.core.util.DateUtils
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.integration.async.AsyncItemProcessor
import org.springframework.batch.integration.async.AsyncItemWriter
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.json.JsonItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.format.DateTimeFormatter
import java.util.concurrent.Future
import javax.sql.DataSource

@Configuration
class ApartmentContractStepConfig(
    private val apartmentContractProcessService: ApartmentContractProcessService,
    private val stepBuilderFactory: StepBuilderFactory,
    private val dataSource: DataSource,
    private val batchThreadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val apartmentContractJobReader: JsonItemReader<BaseApartmentContract>,
    private val apartmentContractJobStepProcessor: FunctionItemProcessor<BaseApartmentContract, ContractJpo>,
    private val apartmentContractJobStepAsyncProcessor: AsyncItemProcessor<BaseApartmentContract, ContractJpo>,
    private val apartmentContractJobStepWriter: ItemWriter<ContractJpo>,
    private val apartmentContractJobStepAsyncWriter: AsyncItemWriter<ContractJpo>,
) {
    companion object {
        const val STEP_NAME = "${ApartmentContractJobConfig.JOB_NAME}Step"
        const val CHUNK_SIZE = 10000
    }

    @JobScope
    @Bean(name = [STEP_NAME])
    fun step(): Step {
        return stepBuilderFactory.get(STEP_NAME)
            .chunk<BaseApartmentContract, Future<ContractJpo>>(CHUNK_SIZE)
            .reader(apartmentContractJobReader)
            .processor(apartmentContractJobStepAsyncProcessor)
            .writer(apartmentContractJobStepAsyncWriter)
            .build()
    }


    @StepScope
    @Bean(name = ["${STEP_NAME}AsyncProcessor"])
    fun asyncProcessor(): AsyncItemProcessor<BaseApartmentContract, ContractJpo> {
        val processor = AsyncItemProcessor<BaseApartmentContract, ContractJpo>()
        processor.setDelegate(apartmentContractJobStepProcessor)
        processor.setTaskExecutor(batchThreadPoolTaskExecutor)
        return processor
    }

    @StepScope
    @Bean(name = ["${STEP_NAME}Processor"])
    fun processor(@Value("#{jobParameters[begin]}") begin: String,
                  @Value("#{jobParameters[end]}") end: String):
            FunctionItemProcessor<BaseApartmentContract, ContractJpo> = FunctionItemProcessor<BaseApartmentContract, ContractJpo> {
        val localDate = it.contractedAt.asLocalDate()
        val isContainsDate = DateUtils.isBetween(localDate, stringDateAsLocalDate(begin), stringDateAsLocalDate(end))
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
    @Bean(name = ["${STEP_NAME}AsyncWriter"])
    fun asyncWriter(): AsyncItemWriter<ContractJpo> {
        val writer = AsyncItemWriter<ContractJpo>()
        writer.setDelegate(apartmentContractJobStepWriter)
        return writer
    }

    @StepScope
    @Bean(name = ["${STEP_NAME}Writer"])
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
                    ps.setString(5, item.building?.let { item.building.id })
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
