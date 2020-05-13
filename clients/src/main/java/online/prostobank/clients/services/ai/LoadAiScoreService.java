package online.prostobank.clients.services.ai;

public interface LoadAiScoreService {
	/**
	 * Обновить данные о результатах AI-скоринга
	 */
	void loadAndSaveAiScores();

	/**
	 * Инициировать процесс вычисления AI-скоринга
	 */
	void startCalculateAiScoring();
}
