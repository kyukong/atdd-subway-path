package wooteco.subway.dao;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;

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
        sectionDao.save(1L, new Section(1L, 2L, 10));

        // then
        Integer count = jdbcTemplate.queryForObject("select count(*) from SECTION", Integer.class);
        Assertions.assertThat(count).isEqualTo(1);
    }

    @DisplayName("구간들을 생성한다.")
    @Test
    void saveAll() {
        // given
        Sections sections = new Sections(List.of(
                new Section(1L, 2L, 10),
                new Section(2L, 3L, 10),
                new Section(3L, 4L, 10)
        ));

        // when
        sectionDao.saveAll(1L, sections);

        // then
        Integer count = jdbcTemplate.queryForObject("select count(*) from SECTION", Integer.class);
        Assertions.assertThat(count).isEqualTo(3);
    }

    @DisplayName("특정 지하철 노선에 특정 지하철역이 존재하면 true 를 반환한다.")
    @Test
    void existStation() {
        // given
        sectionDao.save(1L, new Section(1L, 2L, 10));

        // when & then
        Assertions.assertThat(sectionDao.existStation(1L, 1L)).isTrue();
    }

    @DisplayName("특정 지하철 노선에 특정 상행역 아이디가 존재하면 true 를 반환한다.")
    @Test
    void existUpStation() {
        // given
        sectionDao.save(1L, new Section(1L, 2L, 10));

        // when & then
        Assertions.assertThat(sectionDao.existUpStation(1L, 1L)).isTrue();
    }

    @DisplayName("특정 지하철 노선에 특정 하행역 아이디가 존재하면 true 를 반환한다.")
    @Test
    void existDownStation() {
        // given
        sectionDao.save(1L, new Section(1L, 2L, 10));

        // when & then
        Assertions.assertThat(sectionDao.existDownStation(1L, 2L)).isTrue();
    }

    @DisplayName("구간을 수정한다.")
    @Test
    void update() {
        // given
        long sectionId = sectionDao.save(1L, new Section(1L, 2L, 10));

        // when
        sectionDao.update(new Section(sectionId, 1L, 1L, 2L, 50));

        // then
        Assertions.assertThat(sectionDao.findAllById(1L)).contains(
                new Section(sectionId, 1L, 1L, 2L, 50)
        );
    }

    @DisplayName("특정 지하철 노선에 포함되어 있는 구간을 모두 삭제한다.")
    @Test
    void delete() {
        // given
        sectionDao.save(1L, new Section(1L, 2L, 10));

        // when
        sectionDao.delete(1L);

        // then
        Integer count = jdbcTemplate.queryForObject("select count(*) from SECTION where line_id = ?", Integer.class, 1L);
        Assertions.assertThat(count).isEqualTo(0);
    }
}
