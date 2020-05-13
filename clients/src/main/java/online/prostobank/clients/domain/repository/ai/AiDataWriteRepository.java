package online.prostobank.clients.domain.repository.ai;

import org.springframework.data.util.Pair;

import java.util.List;

public interface AiDataWriteRepository {
	/**
	 * Сохранение результата AI-оценки в карточки клиента
	 * @param scores -- набор пар идентификатор/оценка для идентификаторов
	 */
	void saveAiScore(List<Pair<Long, Double>> scores);
}
