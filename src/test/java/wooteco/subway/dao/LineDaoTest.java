package wooteco.subway.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.domain.Line;

@DisplayName("지하철 노선 관련 DAO 테스트")
@JdbcTest
class LineDaoTest {

    private static final Line LINE = Line.of("신분당선", "bg-red-600", 0);

    private LineDao lineDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        lineDao = new LineDao(jdbcTemplate);
    }

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void save() {
        lineDao.save(LINE);

        Integer count = jdbcTemplate.queryForObject("select count(*) from LINE", Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @DisplayName("중복된 아이디의 지하철 노선이 있다면 true 를 반환한다.")
    @Test
    void existLineById() {
        long lineId = lineDao.save(LINE);

        assertThat(lineDao.existLineById(lineId)).isTrue();
    }

    @DisplayName("중복된 이름의 지하철 노선이 있다면 true 를 반환한다.")
    @Test
    void existLineByName() {
        lineDao.save(LINE);

        assertThat(lineDao.existLineByName("신분당선")).isTrue();
    }

    @DisplayName("중복된 색상의 지하철 노선이 있다면 true 를 반환한다.")
    @Test
    void existLineByColor() {
        lineDao.save(LINE);

        assertThat(lineDao.existLineByColor("bg-red-600")).isTrue();
    }

    @DisplayName("지하철 노선의 목록을 조회한다.")
    @Test
    void findAll() {
        lineDao.save(LINE);
        lineDao.save(Line.of("다른분당선", "bg-green-600", 0));

        List<Line> lines = lineDao.findAll();

        assertThat(lines).hasSize(2);
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void find() {
        long lineId = lineDao.save(LINE);

        Optional<Line> line = lineDao.find(lineId);

        assertThat(line).isNotNull();
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void update() {
        long lineId = lineDao.save(LINE);
        Line updatedLine = Line.of("다른분당선", "bg-red-600", 0);

        lineDao.update(lineId, updatedLine);

        assertThat(lineDao.find(lineId).orElseThrow().getName()).isEqualTo("다른분당선");
    }

    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void delete() {
        long lineId = lineDao.save(LINE);

        lineDao.delete(lineId);

        Integer count = jdbcTemplate.queryForObject("select count(*) from LINE", Integer.class);
        assertThat(count).isEqualTo(0);
    }
}
