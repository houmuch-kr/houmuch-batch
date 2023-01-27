package kr.co.houmuch.batch.job.contract.step

import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.job.contract.ApartmentContractJobConfig
import kr.co.houmuch.batch.job.contract.stringDateAsLocalDate
import kr.co.houmuch.batch.service.contract.ApartmentContractProcessService
import kr.co.houmuch.core.domain.building.jpa.BuildingJpo
import kr.co.houmuch.core.util.DateUtils
import kr.co.houmuch.core.util.RandomGenerator
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.item.json.JsonItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class ApartmentContractBuildingStepConfig(
    private val dataSource: DataSource,
    private val stepBuilderFactory: StepBuilderFactory,
    private val apartmentContractProcessService: ApartmentContractProcessService,
    private val apartmentContractJobReader: JsonItemReader<BaseApartmentContract>,
    private val apartmentContractJobBuildingStepProcessor: FunctionItemProcessor<BaseApartmentContract, BuildingJpo>,
    private val apartmentContractJobBuildingStepWriter: JdbcBatchItemWriter<BuildingJpo>,
) {
    companion object {
        const val STEP_NAME = "${ApartmentContractJobConfig.JOB_NAME}BuildingStep"
        const val CHUNK_SIZE = 10000
    }

    @JobScope
    @Bean(name = [STEP_NAME])
    fun step(): Step {
        return stepBuilderFactory.get(STEP_NAME)
            .chunk<BaseApartmentContract, BuildingJpo>(CHUNK_SIZE)
            .reader(apartmentContractJobReader)
            .processor(apartmentContractJobBuildingStepProcessor)
            .writer(apartmentContractJobBuildingStepWriter)
            .build()
    }

    @StepScope
    @Bean(name = ["${STEP_NAME}Processor"])
    fun processor(@Value("#{jobParameters[begin]}") begin: String,
                  @Value("#{jobParameters[end]}") end: String): FunctionItemProcessor<BaseApartmentContract, BuildingJpo> =
        FunctionItemProcessor<BaseApartmentContract, BuildingJpo> { contract ->
            val localDate = contract.contractedAt.asLocalDate()
            val isContainsDate = DateUtils.isBetween(localDate, stringDateAsLocalDate(begin), stringDateAsLocalDate(end))
            if (isContainsDate) {
                val areaCode = apartmentContractProcessService.processAreaCode(contract)
                BuildingJpo.builder()
                    .id(RandomGenerator.generator(10))
                    .name(contract.getOptionalName())
                    .areaCode(areaCode)
                    .type(contract.buildingType)
                    .squareMeter(contract.getOptionalSquareMeter())
                    .builtAt(contract.getOptionalBuiltYear()?.value)
                    .addressDetail(contract.getOptionalAddressDetail())
                    .build()
            } else {
                null
            }
        }

    @StepScope
    @Bean(name = ["${STEP_NAME}Writer"])
    fun writer(): JdbcBatchItemWriter<BuildingJpo> = JdbcBatchItemWriterBuilder<BuildingJpo>()
        .sql("INSERT INTO building (id, name, area_code, type, address_detail, square_meter, built_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE square_meter = ?")
        .itemPreparedStatementSetter { item, ps ->
            ps.setString(1, item.id)
            ps.setString(2, item.name)
            ps.setLong(3, item.areaCode.id)
            ps.setString(4, item.type.name)
            ps.setString(5, item.addressDetail)
            ps.setDouble(6, item.squareMeter)
            ps.setInt(7, item.builtAt ?: 0)
            ps.setDouble(8, item.squareMeter)
        }
        .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
        .dataSource(dataSource)
        .build()
}
