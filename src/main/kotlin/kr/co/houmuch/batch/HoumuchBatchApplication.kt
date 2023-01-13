package kr.co.houmuch.batch

import kr.co.houmuch.batch.config.properties.BatchProperties
import kr.co.houmuch.batch.config.properties.ContractOpenApiProperties
import kr.co.houmuch.batch.config.properties.SlackWebhookProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)

@SpringBootApplication(scanBasePackages = [BASE_PACKAGE])
@EntityScan(basePackages = [BASE_PACKAGE])
@EnableJpaRepositories(basePackages = [BASE_PACKAGE])
@EnableConfigurationProperties(ContractOpenApiProperties::class, SlackWebhookProperties::class, BatchProperties::class)
@EnableBatchProcessing
class HoumuchBatchApplication

const val BASE_PACKAGE = "kr.co.houmuch"

fun main(args: Array<String>) {
    runApplication<HoumuchBatchApplication>(*args)
}
