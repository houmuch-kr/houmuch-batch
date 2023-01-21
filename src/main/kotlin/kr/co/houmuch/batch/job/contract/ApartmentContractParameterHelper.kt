package kr.co.houmuch.batch.job.contract

import kr.co.houmuch.core.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ApartmentContractParameterHelper

fun createFilePath(date: String): String = "${localDateAsPath(stringDateAsYearMonth(date), "yyyy/MM")}.json"

fun createFilePath(date: String, threadName: String): String {
    return "${localDateAsPath(stringDateAsYearMonth(date), "yyyy/MM")}/${threadName}.json"
}

fun stringDateAsYearMonth(date: String): YearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

fun stringDateAsLocalDate(date: String): LocalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

fun yearMonthAsInt(yearMonth: YearMonth): Int = yearMonth.format(DateUtils.formatter("yyyyMM")).toInt()

fun localDateAsPath(yearMonth: YearMonth, format: String): String = yearMonth.format(DateUtils.formatter(format))

