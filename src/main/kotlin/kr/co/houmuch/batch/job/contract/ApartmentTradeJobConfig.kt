package kr.co.houmuch.batch.job.contract

import kr.co.houmuch.batch.logger
import kr.co.houmuch.batch.service.contract.ApartmentTradeFetchService
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.domain.contract.ContractJpo
import kr.co.houmuch.core.util.RandomGenerator
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import javax.persistence.EntityManagerFactory

@Configuration
class ApartmentTradeJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val areaCodeJpaRepository: AreaCodeJpaRepository,
    private val apartmentTradeFetchService: ApartmentTradeFetchService,
    private val entityManagerFactory: EntityManagerFactory,
) {
    val log = logger<ApartmentTradeJobConfig>()

    companion object {
        const val JOB_NAME = "apartmentTradeJob"
        const val CHUNK_SIZE = 10
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Executor"])
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor() // (2)
        threadPoolTaskExecutor.corePoolSize = 10
        threadPoolTaskExecutor.maxPoolSize = 10
        threadPoolTaskExecutor.setThreadNamePrefix("multi-thread-")
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)
        threadPoolTaskExecutor.initialize()
        return threadPoolTaskExecutor
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
            .taskExecutor(threadPoolTaskExecutor())
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
    fun processor(@Value("#{jobParameters[yearMonth]}") yearMonth: String): FunctionItemProcessor<AreaCodeJpo, List<ContractJpo>> = FunctionItemProcessor { areaCode ->
        val tradeList = apartmentTradeFetchService.fetch(areaCode.getIdBy(0, 5), yearMonth.toInt())
        if (tradeList!!.isEmpty()) {
            log.info("결과 없음 --> 지역코드 : {} {}", areaCode.getIdBy(0, 5), areaCode.address)
        }
        tradeList.map {
            ContractJpo.builder()
                .id(RandomGenerator.generatorLong(10))
                .type("TRADE")
                .buildingType("APARTMENT")
                .areaCode(areaCode)
                .builtAt(it.builtYear)
                .contractedAt(it.contractedAt.asLocalDate())
                .name(it.name)
                .build()
        }.toList()
    }

    @StepScope
    @Bean(name = ["${JOB_NAME}Writer"])
    fun writer(): ItemWriter<List<ContractJpo>> {
        return ItemWriter { lists ->
            val builder = JpaItemWriterBuilder<ContractJpo>()
                .entityManagerFactory(entityManagerFactory)
                .build()
            val list = lists.stream()
                .flatMap { it.stream() }
                .toList()
            builder.write(list)
        }
    }
}

