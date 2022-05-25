package wooteco.subway.dao;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("구간 관련 DAO 테스트")
@JdbcTest
class SectionDaoTest {

    private SectionDao sectionDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        sectionDao = new SectionDao(jdbcTemplate);
    }

    @DisplayName("구간을 생성한다.")
    @Test
    void save() {
        // when
        sectionDao.save(1L, Section.of(1L, 2L, 10));

        // then
        Integer count = jdbcTemplate.queryForObject("select count(*) from SECTION", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("구간들을 생성한다.")
    @Test
    void saveAll() {
        // given
        Sections sections = new Sections(List.of(
                Section.of(1L, 2L, 10),
                Section.of(2L, 3L, 10),
                Section.of(3L, 4L, 10)
        ));

        // when
        sectionDao.saveAll(1L, sections);

        // then
        Integer count = jdbcTemplate.queryForObject("select count(*) from SECTION", Integer.class);
        assertThat(count).isEqualTo(3);
    }

    @DisplayName("노선 아이디를 이용하여 해당 노선에 속해있는 구간을 조회한다.")
    @Test
    void findById() {
        // given
        Sections savedSections = new Sections(List.of(
                Section.of(1L, 2L, 10),
                Section.of(2L, 3L, 10),
                Section.of(3L, 4L, 10)
        ));
        sectionDao.saveAll(1L, savedSections);

        // when
        List<Section> sections = sectionDao.findAllByLineId(1L);

        // then
        assertThat(sections).contains(
                Section.of(1L, 2L, 10),
                Section.of(2L, 3L, 10),
                Section.of(3L, 4L, 10)
        );
    }

    @DisplayName("모든 구간목록을 조회한다.")
    @Test
    void findAll() {
        // given
        Sections savedSections = new Sections(List.of(
                Section.of(1L, 2L, 10),
                Section.of(2L, 3L, 10),
                Section.of(3L, 4L, 10)
        ));
        sectionDao.saveAll(1L, savedSections);

        // when
        List<Section> sections = sectionDao.findAll();

        // then
        assertThat(sections).contains(
                Section.of(1L, 2L, 10),
                Section.of(2L, 3L, 10),
                Section.of(3L, 4L, 10)
        );
    }

    @DisplayName("특정 지하철 노선에 특정 지하철역이 존재하면 true 를 반환한다.")
    @Test
    void existStation() {
        // given
        sectionDao.save(1L, Section.of(1L, 2L, 10));

        // when & then
        assertThat(sectionDao.existStation(1L, 1L)).isTrue();
    }

    @DisplayName("특정 지하철 노선에 특정 상행역 아이디가 존재하면 true 를 반환한다.")
    @Test
    void existUpStation() {
        // given
        sectionDao.save(1L, Section.of(1L, 2L, 10));

        // when & then
        assertThat(sectionDao.existUpStation(1L, 1L)).isTrue();
    }

    @DisplayName("특정 지하철 노선에 특정 하행역 아이디가 존재하면 true 를 반환한다.")
    @Test
    void existDownStation() {
        // given
        sectionDao.save(1L, Section.of(1L, 2L, 10));

        // when & then
        assertThat(sectionDao.existDownStation(1L, 2L)).isTrue();
    }

    @DisplayName("구간을 수정한다.")
    @Test
    void update() {
        // given
        long sectionId = sectionDao.save(1L, Section.of(1L, 2L, 10));

        // when
        sectionDao.update(Section.of(sectionId, 1L, 1L, 2L, 50));

        // then
        assertThat(sectionDao.findAllByLineId(1L)).contains(
                Section.of(sectionId, 1L, 1L, 2L, 50)
        );
    }

    @DisplayName("특정 지하철 노선에 포함되어 있는 구간을 모두 삭제한다.")
    @Test
    void delete() {
        // given
        sectionDao.save(1L, Section.of(1L, 2L, 10));

        // when
        sectionDao.delete(1L);

        // then
        Integer count = jdbcTemplate.queryForObject("select count(*) from SECTION where line_id = ?", Integer.class, 1L);
        assertThat(count).isEqualTo(0);
    }
}
