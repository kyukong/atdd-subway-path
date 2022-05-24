package wooteco.subway.service.dto.response;

import wooteco.subway.domain.Station;

import java.util.List;
import java.util.stream.Collectors;

public class StationResponse {

    private Long id;
    private String name;

    private StationResponse() {
    }

    public StationResponse(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public static StationResponse of(final Station station) {
        return new StationResponse(station.getId(), station.getName());
    }

    public static List<StationResponse> convertStationResponses(final List<Station> stations) {
        return stations.stream()
                .map(StationResponse::of)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
