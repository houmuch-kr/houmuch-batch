package kr.co.houmuch.batch.job

import kr.co.houmuch.batch.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DelegatingJobExecutionListener(
    private val listeners: MutableList<JobExecutionListener> = mutableListOf()
) : JobExecutionListener {
    val log = logger<DelegatingJobExecutionListener>()

    @PostConstruct
    fun postConstruct() {
        log.info("DelegatingJobExecutionListener 활성화됨")
    }

    fun addListener(listener: JobExecutionListener) {
        this.listeners.add(listener)
    }

    fun setListeners(listeners: Array<JobExecutionListener>) {
        this.listeners.addAll(listeners)
    }

    override fun beforeJob(jobExecution: JobExecution) {
        listeners.forEach {
            it.beforeJob(jobExecution)
            log.debug("${it.javaClass.name} beforeJob 실행됨")
        }
    }

    override fun afterJob(jobExecution: JobExecution) {
        listeners.forEach {
            it.afterJob(jobExecution)
            log.debug("${it.javaClass.name} afterJob 실행됨")
        }
    }
}
