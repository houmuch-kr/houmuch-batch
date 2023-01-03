package kr.co.houmuch.batch.config

import kr.co.houmuch.batch.job.DelegatingJobExecutionListener
import org.springframework.batch.core.JobExecutionListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DelegatingJobExecutionListenerRegistrationBean {
    @Bean
    fun delegatingJobExecutionListener(listeners: List<JobExecutionListener>): DelegatingJobExecutionListener {
        val delegator = DelegatingJobExecutionListener()
        delegator.setListeners(listeners)
        return delegator
    }
}
