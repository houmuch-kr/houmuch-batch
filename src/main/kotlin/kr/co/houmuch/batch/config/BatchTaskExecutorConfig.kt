package kr.co.houmuch.batch.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class BatchTaskExecutorConfig {
    @Bean(name = ["batchThreadPoolTaskExecutor"])
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor() // (2)
        threadPoolTaskExecutor.corePoolSize = 10
        threadPoolTaskExecutor.maxPoolSize = 10
        threadPoolTaskExecutor.setThreadNamePrefix("multi-thread-")
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)
        threadPoolTaskExecutor.initialize()
        return threadPoolTaskExecutor
    }
}
