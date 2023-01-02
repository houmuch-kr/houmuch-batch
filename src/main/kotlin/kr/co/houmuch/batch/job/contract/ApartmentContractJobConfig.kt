package kr.co.houmuch.batch.job.contract

import kr.co.houmuch.batch.job.contract.mapper.ApartmentContractModelMapper
import kr.co.houmuch.batch.logger
import kr.co.houmuch.batch.service.contract.ApartmentContractFetchService
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.domain.contract.ContractAdditionalJpo
import kr.co.houmuch.core.domain.contract.ContractDetailJpo
import kr.co.houmuch.core.domain.contract.ContractJpo
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

@Configuration
class ApartmentContractJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val areaCodeJpaRepository: AreaCodeJpaRepository,
    private val apartmentContractFetchService: ApartmentContractFetchService,
    private val dataSource: DataSource,
    private val batchThreadPoolTaskExecutor: ThreadPoolTaskExecutor,
) {
    val log = logger<ApartmentContractJobConfig>()

    companion object {
        const val JOB_NAME = "apartmentContractJob"
        const val CHUNK_SIZE = 1
    }

    @Bean(name = [JOB_NAME])
    fun job(step: Step): Job {
        return jobBuilderFactory.get(JOB_NAME)
            .start(step)
            .build()
    }

    @JobScope
    @Bean(name = ["${JOB_NAME}Step"])
    fun step(processor: FunctionItemProcessor<AreaCodeJpo, List<ContractJpo>>): Step {
        return stepBuilderFactory.get("${JOB_NAME}Step")
            .chunk<AreaCodeJpo, List<ContractJpo>>(CHUNK_SIZE)
            .reader(reader())
            .processor(processor)
            .writer(writer())
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
    fun processor(@Value("#{jobParameters[yearMonth]}") yearMonth: String):
            FunctionItemProcessor<AreaCodeJpo, List<ContractJpo>> =
        FunctionItemProcessor { areaCode ->
            val tradeList = apartmentContractFetchService.fetchAsync(areaCode.getIdBy(0, 5), yearMonth.toInt())
            if (tradeList!!.isEmpty()) {
                log.info("결과 없음 --> 지역코드 : {} {}", areaCode.getIdBy(0, 5), areaCode.address)
            }
            tradeList.map { ApartmentContractModelMapper.mapping(it, areaCode) }
        }

    @StepScope
    @Bean(name = ["${JOB_NAME}Writer"])
    fun writer(): ItemWriter<List<ContractJpo>> {
        return ItemWriter { lists ->
            val contractList = lists.flatMap { it.stream().toList() }
                val contractWriter = JdbcBatchItemWriterBuilder<ContractJpo>()
                .sql("INSERT INTO contract (id, type, building_type, area_code, contracted_at, serial_number, name) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)")
                .itemPreparedStatementSetter { item, ps ->
                    ps.setLong(1, item.id)
                    ps.setString(2, item.type.name)
                    ps.setString(3, item.buildingType.name)
                    ps.setLong(4, item.areaCode.id)
                    ps.setString(5, item.contractedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    ps.setString(6, item.serialNumber)
                    ps.setString(7, item.name)
                }
                .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
                .dataSource(dataSource)
                .build()
            val contractDetailWriter = JdbcBatchItemWriterBuilder<ContractDetailJpo>()
                .sql("INSERT INTO contract_detail (id, price, monthly_price, floor, address_detail, square_meter, built_at) " +
                        "VALUES (:id, :price, :monthlyPrice, :floor, :addressDetail, :squareMeter, :builtAt)")
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

