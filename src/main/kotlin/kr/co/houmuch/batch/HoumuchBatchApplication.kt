package kr.co.houmuch.batch

import kr.co.houmuch.batch.config.properties.ContractOpenApiProperties
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(ContractOpenApiProperties::class)
@EnableBatchProcessing
class HoumuchBatchApplication

fun main(args: Array<String>) {
    runApplication<HoumuchBatchApplication>(*args)
}
