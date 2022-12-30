package kr.co.houmuch.batch.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class AsyncTaskExecutorConfig {
    companion object {
        const val POOL_SIZE = 20
    }

    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor() // (2)
        threadPoolTaskExecutor.corePoolSize = POOL_SIZE
        threadPoolTaskExecutor.maxPoolSize = POOL_SIZE
        threadPoolTaskExecutor.setThreadNamePrefix("async-thread-")
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)
        threadPoolTaskExecutor.initialize()
        return threadPoolTaskExecutor
    }
}
