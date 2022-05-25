package wooteco.subway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Station;
import wooteco.subway.service.dto.request.LineSaveRequest;
import wooteco.subway.service.dto.request.LineUpdateRequest;
import wooteco.subway.service.dto.response.LineResponse;
import wooteco.subway.service.dto.response.StationResponse;

@DisplayName("지하철 노선 관련 service 테스트")
@JdbcTest
class LineServiceTest {

    private static LineSaveRequest LINE_SAVE_REQUEST;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private LineService lineService;
    private StationDao stationDao;

    @BeforeEach
    void setUp() {
        LineDao lineDao = new LineDao(jdbcTemplate);
        SectionDao sectionDao = new SectionDao(jdbcTemplate);
        stationDao = new StationDao(jdbcTemplate);

        lineService = new LineService(lineDao, sectionDao, stationDao);

        long upStationId = stationDao.save(new Station("출발역"));
        long downStationId = stationDao.save(new Station("도착역"));
        LINE_SAVE_REQUEST = new LineSaveRequest("신분당선", "bg-red-600", upStationId, downStationId, 10, 0);
    }

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void save() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        LineSaveRequest lineSaveRequest = new LineSaveRequest("신분당선", "bg-red-600", station1Id, station2Id, 10, 0);

        // when
        LineResponse lineResponse = lineService.save(lineSaveRequest);

        // then
        List<String> stationNames = lineResponse.getStations().stream()
                .map(StationResponse::getName)
                .collect(Collectors.toList());

        assertAll(
                () -> assertThat(lineResponse.getName()).isEqualTo("신분당선"),
                () -> assertThat(lineResponse.getColor()).isEqualTo("bg-red-600"),
                () -> assertThat(lineResponse.getExtraFare()).isEqualTo(0),
                () -> assertThat(stationNames).contains("강남역", "역삼역")
        );
    }

    @DisplayName("중복된 이름의 지하철 노선을 생성할 경우 예외를 발생시킨다.")
    @Test
    void saveDuplicatedName() {
        // given
        lineService.save(LINE_SAVE_REQUEST);

        // when & then
        assertThatThrownBy(
                () -> lineService.save(new LineSaveRequest("신분당선", "bg-green-600", 1L, 2L, 10, 0))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지하철 노선 이름이 중복됩니다.");
    }

    @DisplayName("중복된 색상의 지하철 노선을 생성할 경우 예외를 발생시킨다.")
    @Test
    void saveDuplicatedColor() {
        // given
        lineService.save(LINE_SAVE_REQUEST);

        // when & then
        assertThatThrownBy(
                () -> lineService.save(new LineSaveRequest("다른분당선", "bg-red-600", 1L, 2L, 10, 0))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지하철 노선 색상이 중복됩니다.");
    }

    @DisplayName("지하철 노선의 목록을 조회한다.")
    @Test
    void findAll() {
        // given
        lineService.save(LINE_SAVE_REQUEST);

        // when
        List<String> lineNames = lineService.findAll().stream()
                .map(LineResponse::getName)
                .collect(Collectors.toList());

        // then
        assertThat(lineNames).contains("신분당선");
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void find() {
        // given
        LineResponse lineSavedResponse = lineService.save(LINE_SAVE_REQUEST);
        long lineId = lineSavedResponse.getId();

        // when
        LineResponse lineResponse = lineService.find(lineId);

        // then
        assertAll(
                () -> assertThat(lineResponse.getName()).isEqualTo("신분당선"),
                () -> assertThat(lineResponse.getColor()).isEqualTo("bg-red-600"),
                () -> assertThat(lineResponse.getExtraFare()).isEqualTo(0)
        );
    }

    @DisplayName("존재하지 않는 지하철 노선을 조회할 경우 예외를 발생시킨다.")
    @Test
    void findNotExistLine() {
        // when & then
        assertThatThrownBy(() -> lineService.find(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 지하철 노선입니다.");
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void update() {
        // given
        LineResponse lineResponse = lineService.save(LINE_SAVE_REQUEST);
        long lineId = lineResponse.getId();

        // when
        lineService.update(lineId, new LineUpdateRequest("다른분당선", "bg-green-600", 0));

        // then
        List<String> lineNames = lineService.findAll().stream()
                .map(LineResponse::getName)
                .collect(Collectors.toList());

        assertThat(lineNames).contains("다른분당선");
    }

    @DisplayName("중복된 이름으로 지하철 노선을 수정할 경우 예외를 발생시킨다.")
    @Test
    void updateDuplicatedName() {
        // given
        LineResponse lineResponse = lineService.save(LINE_SAVE_REQUEST);
        long lineId = lineResponse.getId();

        // when & then
        assertThatThrownBy(() -> lineService.update(lineId, new LineUpdateRequest("신분당선", "bg-green-600", 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지하철 노선 이름이 중복됩니다.");
    }

    @DisplayName("중복된 색상으로 지하철 노선을 수정할 경우 예외를 발생시킨다.")
    @Test
    void updateDuplicatedColor() {
        // given
        LineResponse lineResponse = lineService.save(LINE_SAVE_REQUEST);
        long lineId = lineResponse.getId();

        // when & then
        assertThatThrownBy(() -> lineService.update(lineId, new LineUpdateRequest("다른분당선", "bg-red-600", 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지하철 노선 색상이 중복됩니다.");
    }

    @DisplayName("존재하지 않는 지하철 노선을 수정할 경우 예외를 발생시킨다.")
    @Test
    void updateNotExistLine() {
        // when & then
        assertThatThrownBy(() -> lineService.update(1L, new LineUpdateRequest("다른분당선", "bg-green-600", 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 지하철 노선입니다.");
    }

    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void delete() {
        // given
        LineResponse lineResponse = lineService.save(LINE_SAVE_REQUEST);
        long lineId = lineResponse.getId();

        // when
        lineService.delete(lineId);

        // then
        List<String> lineNames = lineService.findAll().stream()
                .map(LineResponse::getName)
                .collect(Collectors.toList());

        assertThat(lineNames).doesNotContain("신분당선");
    }

    @DisplayName("존재하지 않는 지하철 노선을 삭제할 경우 예외를 발생시킨다.")
    @Test
    void deleteNotExistLine() {
        // when & then
        assertThatThrownBy(() -> lineService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 지하철 노선입니다.");
    }
}
