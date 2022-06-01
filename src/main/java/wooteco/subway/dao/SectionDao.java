package wooteco.subway.dao;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;

@Repository
public class SectionDao {

    private static final RowMapper<Section> SECTION_ROW_MAPPER = (resultSet, rowNum) -> {
        return Section.of(
                resultSet.getLong("id"),
                resultSet.getLong("line_id"),
                resultSet.getLong("up_station_id"),
                resultSet.getLong("down_station_id"),
                resultSet.getInt("distance")
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public SectionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long save(final Long lineId, final Section section) {
        final String sql = "insert into SECTION (line_id, up_station_id, down_station_id, distance) values (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setLong(1, lineId);
            preparedStatement.setLong(2, section.getUpStationId());
            preparedStatement.setLong(3, section.getDownStationId());
            preparedStatement.setInt(4, section.getDistance());
            return preparedStatement;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void saveAll(final Long lineId, final Sections sections) {
        for (Section section : sections.getSections()) {
            save(lineId, section);
        }
    }

    public List<Section> findAllByLineId(final Long lineId) {
        final String sql = "select id, line_id, up_station_id, down_station_id, distance from SECTION where line_id = ?";
        return jdbcTemplate.query(sql, SECTION_ROW_MAPPER, lineId);
    }

    public List<Section> findAll() {
        final String sql = "select id, line_id, up_station_id, down_station_id, distance from SECTION";
        return jdbcTemplate.query(sql, SECTION_ROW_MAPPER);
    }

    public Optional<Section> findByStationId(final Long stationId) {
        final String sql = "select id, line_id, up_station_id, down_station_id, distance " +
                "from SECTION where up_station_id = ? or down_station_id = ? limit 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, SECTION_ROW_MAPPER, stationId, stationId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean existStation(final Long lineId, final Long stationId) {
        final String sql = "select exists " +
                "(select id from SECTION where line_id = ? and (up_station_id = ? or down_station_id = ?))";
        return jdbcTemplate.queryForObject(sql, Boolean.class, lineId, stationId, stationId);
    }

    public boolean existUpStation(final Long lineId, final Long stationId) {
        final String sql = "select exists (select id from SECTION where line_id = ? and up_station_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, lineId, stationId);
    }

    public boolean existDownStation(final Long lineId, final Long stationId) {
        final String sql = "select exists (select id from SECTION where line_id = ? and down_station_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, lineId, stationId);
    }

    public void update(final Section section) {
        final String sql = "update SECTION set up_station_id = ?, down_station_id = ?, distance = ? where id = ?";
        jdbcTemplate.update(sql,
                section.getUpStationId(), section.getDownStationId(), section.getDistance(), section.getSectionId());
    }

    public void delete(final Long id) {
        final String sql = "delete from SECTION where line_id = ?";
        jdbcTemplate.update(sql, id);
    }
}
