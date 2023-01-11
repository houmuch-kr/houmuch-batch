package kr.co.houmuch.batch.service.coordinate

import kr.co.houmuch.batch.logger
import kr.co.houmuch.core.client.naver.NaverMapApiClient
import kr.co.houmuch.core.domain.area.jpa.AreaCoordinateJpo
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.util.JsonUtils.asJson
import org.springframework.stereotype.Service

@Service
class AreaCoordinateFetchService(
    private val naverMapApiClient: NaverMapApiClient
) {
    var log = logger<AreaCoordinateFetchService>()

    fun fetch(areaCodeJpo: AreaCodeJpo): AreaCoordinateJpo {
        val coordinate = naverMapApiClient.fetchGeocode(areaCodeJpo.fullAddress)
            .doOnError { log.info("네이버 Geocode 조회 결과 없음 --> {}", asJson(areaCodeJpo)) }
            .blockOptional()
            .orElseThrow { throw RuntimeException() }
            .first()
            .asCoordinate()
        return AreaCoordinateJpo.builder()
            .id(areaCodeJpo.id)
            .areaCode(areaCodeJpo)
            .coordinate(coordinate.asJpo())
            .build()
    }
}
