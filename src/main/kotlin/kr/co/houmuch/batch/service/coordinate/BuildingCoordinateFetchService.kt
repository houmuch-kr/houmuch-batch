package kr.co.houmuch.batch.service.coordinate

import kr.co.houmuch.batch.logger
import kr.co.houmuch.core.client.naver.NaverMapApiClient
import kr.co.houmuch.core.domain.building.jpa.BuildingCoordinateJpo
import kr.co.houmuch.core.domain.building.jpa.BuildingJpo
import org.springframework.stereotype.Service

@Service
class BuildingCoordinateFetchService(
    private val naverMapApiClient: NaverMapApiClient
) {
    var log = logger<BuildingCoordinateFetchService>()

    fun fetch(buildingJpo: BuildingJpo): BuildingCoordinateJpo? {
        val address = naverMapApiClient.fetchGeocode(buildingJpo.fullAddress)
            .doOnError { log.info("네이버 Geocode 조회 결과 없음 --> {}", buildingJpo.toString()) }
            .blockOptional()
            .orElseThrow { throw RuntimeException() }
            .firstOrNull()
        return if (address != null) {
            BuildingCoordinateJpo.builder()
                .coordinate(address.asCoordinate().asJpo())
                .build()
        } else null
    }
}
