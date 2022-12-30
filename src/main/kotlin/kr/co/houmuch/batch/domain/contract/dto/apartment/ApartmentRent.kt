package kr.co.houmuch.batch.domain.contract.dto.apartment

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kr.co.houmuch.batch.config.convert.NumberCommaParseDeserializer
import kr.co.houmuch.batch.domain.contract.dto.ContractDate
import kr.co.houmuch.core.domain.JsonSerializable

/*
<item>
    <갱신요구권사용> </갱신요구권사용>
    <건축년도>2007</건축년도>
    <계약구분> </계약구분>
    <계약기간> </계약기간>
    <년>2015</년>
    <법정동>필운동</법정동>
    <보증금액>65,000</보증금액>
    <아파트>신동아블루아광화문의꿈</아파트>
    <월>12</월>
    <월세금액>0</월세금액>
    <일>4</일>
    <전용면적>111.97</전용면적>
    <종전계약보증금> </종전계약보증금>
    <종전계약월세> </종전계약월세>
    <지번>254</지번>
    <지역코드>11110</지역코드>
    <층>7</층>
</item>
 */
data class ApartmentRent(
    @JsonProperty("갱신요구권사용")
    val useRefreshClaim: String?,

    @JsonProperty("건축년도")
    var builtYear: Int?,

    @JsonProperty("계약구분")
    val type: String?,

    @JsonProperty("계약기간")
    val term: String?,

    @JsonProperty("법정동")
    val dong: String?,

    @JsonDeserialize(using = NumberCommaParseDeserializer::class)
    @JsonProperty("보증금액")
    val price: Int?,

    @JsonProperty("아파트")
    var name: String?,

    @JsonDeserialize(using = NumberCommaParseDeserializer::class)
    @JsonProperty("월세금액")
    val monthlyPrice: Int?,

    @JsonProperty("전용면적")
    var squareMeter: Double?,

    @JsonProperty("종전계약보증금")
    val previousPrice: String?,

    @JsonProperty("종전계약월세")
    val previousMonthlyPrice: String?,

    @JsonProperty("지번")
    var addressDetail: String?,

    @JsonProperty("지역코드")
    var regionCode: Int?,

    @JsonProperty("층")
    val floor: Int?,

) : JsonSerializable {
    @JsonUnwrapped
    lateinit var contractedAt: ContractDate
}
