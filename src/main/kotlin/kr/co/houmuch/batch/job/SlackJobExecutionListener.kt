package kr.co.houmuch.batch.job

import kr.co.houmuch.batch.client.SlackWebhookApiClient
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("prod")
class SlackJobExecutionListener(
    private val slackWebhookApiClient: SlackWebhookApiClient
) : JobExecutionListener {
    override fun beforeJob(jobExecution: JobExecution) {

    }

    override fun afterJob(jobExecution: JobExecution) {
        val name = jobExecution.jobInstance.jobName
        val parameters = jobExecution.jobParameters.parameters
            .map { (key, value) -> "`${key}`: $value"}
            .joinToString("\n")
        val status = jobExecution.status
        val duration = ((jobExecution.endTime!!.time) - (jobExecution.createTime.time)) / 1000.0
        slackWebhookApiClient.post(template(name, parameters, status.name, duration.toString()))
    }

    private fun template(jobName: String, jobParameters: String, status: String, duration: String): String {
        // TODO: Refactor Object
        return """{
            "blocks": [
                {
                    "type": "section",
                    "text": {
                        "type": "mrkdwn",
                        "text": "*$jobName* 수행 완료"
                    }
                },
                {
                    "type": "section",
                    "text": {
                        "type": "mrkdwn",
                        "text": "*Job 파라미터 목록* \n $jobParameters"
                    }
                },
                {
                    "type": "section",
                    "fields": [
                        {
                            "type": "mrkdwn",
                            "text": "*수행 시간* $duration s"
                        },
                        {
                            "type": "mrkdwn",
                            "text": "*처리 상태* `$status`"
                        }
                    ]
                }
            ]
        }"""
    }
}
