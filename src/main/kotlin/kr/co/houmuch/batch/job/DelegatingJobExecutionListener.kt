package kr.co.houmuch.batch.job

import kr.co.houmuch.batch.logger
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import javax.annotation.PostConstruct

open class DelegatingJobExecutionListener : JobExecutionListener {
    private val log = logger<DelegatingJobExecutionListener>()
    private val listeners: MutableList<JobExecutionListener> = mutableListOf()

    @PostConstruct
    fun postConstruct() {
        log.info("DelegatingJobExecutionListener 활성화됨")
    }

    fun addListener(listener: JobExecutionListener) {
        this.listeners.add(listener)
    }

    fun setListeners(listeners: List<JobExecutionListener>) {
        this.listeners.addAll(listeners)
    }

    override fun beforeJob(jobExecution: JobExecution) {
        listeners.filter { it != this }.forEach {
            it.beforeJob(jobExecution)
            log.debug("${it.javaClass.name} beforeJob 실행됨")
        }
    }

    override fun afterJob(jobExecution: JobExecution) {
        listeners.filter { it != this }.forEach {
            it.afterJob(jobExecution)
            log.debug("${it.javaClass.name} afterJob 실행됨")
        }
    }
}
