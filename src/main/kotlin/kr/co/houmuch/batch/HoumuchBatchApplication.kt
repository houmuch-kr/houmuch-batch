package kr.co.houmuch.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HoumuchBatchApplication

fun main(args: Array<String>) {
    runApplication<HoumuchBatchApplication>(*args)
}
