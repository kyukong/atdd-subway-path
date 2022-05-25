package wooteco.subway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Station;
import wooteco.subway.service.dto.request.SectionRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("구간 관련 service 테스트")
@JdbcTest
public class SectionServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SectionService sectionService;
    private SectionDao sectionDao;
    private StationDao stationDao;

    @BeforeEach
    void setUp() {
        sectionDao = new SectionDao(jdbcTemplate);
        stationDao = new StationDao(jdbcTemplate);

        sectionService = new SectionService(sectionDao, stationDao);
    }

    @DisplayName("구간 생성 시 상행역에 해당하는 지하철역이 존재하지 경우 예외가 발생한다.")
    @Test
    void saveNotExistUpStation() {
        // when & then
        assertThatThrownBy(
                () -> sectionService.save(1L, new SectionRequest(1L, 2L, 10))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상행역이 존재하지 않습니다.");
    }

    @DisplayName("구간 생성 시 하행역에 해당하는 지하철역이 존재하지 경우 예외가 발생한다.")
    @Test
    void saveNotExistDownStation() {
        // given
        long stationId = stationDao.save(new Station(1L, "강남역"));

        // when & then
        assertThatThrownBy(
                () -> sectionService.save(1L, new SectionRequest(stationId, 2L, 10))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("하행역이 존재하지 않습니다.");
    }

    @DisplayName("구간 생성 시 상행역과 하행역이 이미 지하철 노선에 존재하면 예외가 발생한다.")
    @Test
    void saveAlreadyExistAllSection() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));

        sectionDao.save(1L, Section.of(station1Id, station2Id, 10));

        // when & then
        assertThatThrownBy(
                () -> sectionService.save(1L, new SectionRequest(station1Id, station2Id, 10))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상행역과 하행역이 이미 지하철 노선에 존재합니다.");
    }

    @DisplayName("구간 생성 시 상행 종점을 등록한다.")
    @Test
    void saveNewUpStation() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station2Id, station3Id, 10));

        // when
        sectionService.save(1L, new SectionRequest(station1Id, station2Id, 10));

        // then
        List<Section> sections = sectionDao.findAllByLineId(1L);
        assertThat(sections).contains(
                Section.of(station1Id, station2Id, 10),
                Section.of(station2Id, station3Id, 10)
        );
    }

    @DisplayName("구간 생성 시 하행 종점을 등록한다.")
    @Test
    void saveNewDownStation() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station2Id, 10));

        // when
        sectionService.save(1L, new SectionRequest(station2Id, station3Id, 10));

        // then
        List<Section> sections = sectionDao.findAllByLineId(1L);
        assertThat(sections).contains(
                Section.of(station1Id, station2Id, 10),
                Section.of(station2Id, station3Id, 10)
        );
    }

    @DisplayName("구간 생성 시 상행역을 등록한다.")
    @Test
    void saveUpStation() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station3Id, 10));

        // when
        sectionService.save(1L, new SectionRequest(station2Id, station3Id, 5));

        // then
        List<Section> sections = sectionDao.findAllByLineId(1L);
        assertThat(sections).contains(
                Section.of(station1Id, station2Id, 5),
                Section.of(station2Id, station3Id, 5)
        );
    }

    @DisplayName("구간 생성 시 하행역을 등록한다.")
    @Test
    void saveDownStation() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station3Id, 10));

        // when
        sectionService.save(1L, new SectionRequest(station1Id, station2Id, 5));

        // then
        List<Section> sections = sectionDao.findAllByLineId(1L);
        assertThat(sections).contains(
                Section.of(station1Id, station2Id, 5),
                Section.of(station2Id, station3Id, 5)
        );
    }

    @DisplayName("구간 사이에 새로운 구간을 등록할 경우 새 구간 거리가 기존 구간 거리보다 짧으면 등록한다.")
    @Test
    void saveUnderDistance() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station3Id, 10));

        // when
        sectionService.save(1L, new SectionRequest(station1Id, station2Id, 5));

        // then
        List<Section> sections = sectionDao.findAllByLineId(1L);
        assertThat(sections).contains(
                Section.of(station1Id, station2Id, 5),
                Section.of(station2Id, station3Id, 5)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15})
    @DisplayName("구간 사이에 새로운 구간을 등록할 경우 새 구간 거리가 기존 구간 거리보다 크거나 같으면 예외가 발생한다.")
    void saveOverDistance(int distance) {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station3Id, 10));

        // when & then
        assertThatThrownBy(
                () -> sectionService.save(1L, new SectionRequest(station1Id, station2Id, distance))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기존 구간의 길이를 벗어납니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    @DisplayName("등록하려는 구간 거리가 1 미만일 경우 예외가 발생한다.")
    void saveUnder1(int distance) {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station3Id, 10));

        // when & then
        assertThatThrownBy(
                () -> sectionService.save(1L, new SectionRequest(station1Id, station2Id, distance))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구간 거리는 1 이상이어야 합니다.");
    }

    @DisplayName("구간 생성 시 다른 구간과 연결되어 있지 않으면 예외가 발생한다.")
    @Test
    void saveNotExistAnyStation() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));
        long station4Id = stationDao.save(new Station(4L, "선릉역"));

        sectionDao.save(1L, Section.of(station1Id, station2Id, 10));

        // when & then
        assertThatThrownBy(
                () -> sectionService.save(1L, new SectionRequest(station3Id, station4Id, 10))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가하려는 구간이 노선에 포함되어 있지 않습니다.");
    }

    @DisplayName("구간 삭제 시 상행 종점을 삭제한다.")
    @Test
    void deleteEndUpStation() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station2Id, 10));
        sectionDao.save(1L, Section.of(station2Id, station3Id, 10));

        // when
        sectionService.delete(1L, station1Id);

        // then
        assertThat(sectionDao.findAllByLineId(1L)).contains(
                Section.of(station2Id, station3Id, 10)
        );
    }

    @DisplayName("구간 삭제 시 하행 종점을 삭제한다.")
    @Test
    void deleteEndDownStation() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station2Id, 10));
        sectionDao.save(1L, Section.of(station2Id, station3Id, 10));

        // when
        sectionService.delete(1L, station3Id);

        // then
        assertThat(sectionDao.findAllByLineId(1L)).contains(
                Section.of(station1Id, station2Id, 10)
        );
    }

    @DisplayName("구간 삭제 시 중간역을 삭제한다.")
    @Test
    void deleteStationInSection() {
        // given
        long station1Id = stationDao.save(new Station(1L, "강남역"));
        long station2Id = stationDao.save(new Station(2L, "역삼역"));
        long station3Id = stationDao.save(new Station(3L, "삼성역"));

        sectionDao.save(1L, Section.of(station1Id, station2Id, 10));
        sectionDao.save(1L, Section.of(station2Id, station3Id, 10));

        // when
        sectionService.delete(1L, station2Id);

        // then
        assertThat(sectionDao.findAllByLineId(1L)).contains(
                Section.of(station1Id, station3Id, 20)
        );
    }
}
