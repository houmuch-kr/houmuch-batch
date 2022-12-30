package kr.co.houmuch.batch.domain.contract.dto.apartment

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kr.co.houmuch.batch.config.convert.NumberCommaParseDeserializer
import kr.co.houmuch.batch.config.convert.StringTrimDeserializer
import kr.co.houmuch.batch.domain.contract.dto.ContractDate
import kr.co.houmuch.batch.domain.contract.dto.LegalAddress
import kr.co.houmuch.batch.domain.contract.dto.RoadAddress
import kr.co.houmuch.core.domain.JsonSerializable

/*
<item>
    <거래금액> 82,500</거래금액>
    <거래유형> </거래유형>
    <건축년도>2008</건축년도>
    <년>2015</년>
    <도로명>사직로8길</도로명>
    <도로명건물본번호코드>00004</도로명건물본번호코드>
    <도로명건물부번호코드>00000</도로명건물부번호코드>
    <도로명시군구코드>11110</도로명시군구코드>
    <도로명일련번호코드>03</도로명일련번호코드>
    <도로명지상지하코드>0</도로명지상지하코드>
    <도로명코드>4100135</도로명코드>
    <법정동> 사직동</법정동>
    <법정동본번코드>0009</법정동본번코드>
    <법정동부번코드>0000</법정동부번코드>
    <법정동시군구코드>11110</법정동시군구코드>
    <법정동읍면동코드>11500</법정동읍면동코드>
    <법정동지번코드>1</법정동지번코드>
    <아파트>광화문스페이스본(101동~105동)</아파트>
    <월>12</월>
    <일>10</일>
    <일련번호>11110-2203</일련번호>
    <전용면적>94.51</전용면적>
    <중개사소재지> </중개사소재지>
    <지번>9</지번>
    <지역코드>11110</지역코드>
    <층>11</층>
    <해제사유발생일> </해제사유발생일>
    <해제여부> </해제여부>
</item>
 */
data class ApartmentContractTradeDetail(
    @JsonDeserialize(using = NumberCommaParseDeserializer::class)
    @JsonProperty("거래금액")
    var price: Int?,

    @JsonDeserialize(using = StringTrimDeserializer::class)
    @JsonProperty("거래유형")
    var type: String?,

    @JsonProperty("건축년도")
    var builtYear: Int?,

    @JsonProperty("아파트")
    val name: String,

    @JsonProperty("일련번호")
    val serialNumber: String,

    @JsonProperty("전용면적")
    var squareMeter: Double?,

    @JsonProperty("중개사소재지")
    var broker: String?,

    @JsonProperty("지번")
    var addressDetail: String?,

    @JsonProperty("지역코드")
    val regionCode: Int?,

    @JsonProperty("층")
    val floor: Int?,

    @JsonProperty("해제사유발생일")
    var releasedAt: String?,

    @JsonProperty("해제여부")
    val release: String?,

) : JsonSerializable {
    @JsonUnwrapped
    lateinit var roadAddress: RoadAddress

    @JsonUnwrapped
    lateinit var legalAddress: LegalAddress

    @JsonUnwrapped
    lateinit var contractedAt: ContractDate
}
