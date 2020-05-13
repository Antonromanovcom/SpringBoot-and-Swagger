package online.prostobank.clients.domain.repository.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AiDataWriteRepositoryImpl implements AiDataWriteRepository {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public void saveAiScore(List<Pair<Long, Double>> scores) {
		if (scores == null) {
			log.error("Набор данных ИИ-скоринга имеет значение NULL");
			return;
		}
		try {
			jdbcTemplate.batchUpdate("UPDATE account_application SET ai_score = ? WHERE id = ?",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int i) throws SQLException {
							ps.setDouble(1, scores.get(i).getSecond());
							ps.setLong(2, scores.get(i).getFirst());
						}

						@Override
						public int getBatchSize() {
							return scores.size();
						}
					});
		} catch (DataAccessException ex) {
			log.error("Не удалось сохранить данные AI-скоринга");
		}
	}
}
