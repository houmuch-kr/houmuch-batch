package kr.co.houmuch.batch.job.contract.parameter

import kr.co.houmuch.core.domain.JsonSerializable
import java.time.YearMonth

open class ContractJobParameter(
    val yearMonth: YearMonth
) : JsonSerializable
