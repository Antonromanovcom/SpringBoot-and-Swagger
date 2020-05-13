package online.prostobank.clients.services.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ai.InitiatePredictionResponseDTO;
import online.prostobank.clients.api.dto.ai.PredictionRequestDTO;
import online.prostobank.clients.api.dto.ai.PredictionResponseDTO;
import online.prostobank.clients.config.FeatureToggleConfig;
import online.prostobank.clients.config.properties.AiServiceProperties;
import online.prostobank.clients.domain.repository.ai.AiDataReadRepository;
import online.prostobank.clients.domain.repository.ai.AiDataWriteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoadAiScoreServiceImpl implements LoadAiScoreService {
	private static final int PAGE_SIZE = 10;
	private static final String AI_PREDICT_FEATURE = "ai_predict";

	private final RestTemplate restTemplate;
	private final AiDataWriteRepository writeRepository;
	private final AiDataReadRepository readRepository;
	private final FeatureToggleConfig toggleConfig;
	private final AiServiceProperties properties;

	@PostConstruct
	void init() {
		if (toggleConfig.isFeatureEnabled(AI_PREDICT_FEATURE)) {
			log.info("Интеграция с AI-сервисом ранжирования заявок активирована. Параметры соединения {} {} {}",
					properties.getUrlObtainingPrediction(), properties.getUrlInitPrediction(), properties.getPageSize());
		} else {
			log.info("Интеграция с AI-сервисом ранжирования заявок отключена");
		}
	}

	@Override
	@Scheduled(cron = "${ai.scheduler}")
	public void loadAndSaveAiScores() {
		if (!toggleConfig.isFeatureEnabled(AI_PREDICT_FEATURE)) {
			return;
		}

		log.info("Старт загрузки результатов AI-prediction");
		Long totalClientCount = readRepository.getTotalClientsCount();

		if (totalClientCount == null || totalClientCount <= 0) {
			log.error("Нет карточек для обработки");
			return;
		}
		log.info("Число карточек для обработки {}", totalClientCount);

		int maxPages = (int) Math.ceil((double)totalClientCount / properties.getPageSize());
		Sort sort = Sort.by(Sort.Direction.DESC, "aiScore");

		for (int i = 0; i <= maxPages; i++) {
			PageRequest page = PageRequest.of(i, properties.getPageSize(), sort);
			List<Long> ids = readRepository.getIdsForAiProcessing(page);

			if (ids != null && ids.size() > 0) {

				PredictionRequestDTO requestDTO = new PredictionRequestDTO(ids);
				HttpEntity http = new HttpEntity<>(requestDTO);
				PredictionResponseDTO responseDTO = null;
				try {
					log.info("Запрос данных для страницы - {}, число элементов - {}", i, ids.size());
					ResponseEntity<PredictionResponseDTO> responseEntity = restTemplate.exchange(
							properties.getUrlObtainingPrediction(), HttpMethod.POST, http, PredictionResponseDTO.class);
					responseDTO = responseEntity.getBody();
				} catch (HttpStatusCodeException e) {
					log.error("Обращение к AI-сервису завершилось ошибкой httpCode={}", e.getStatusCode());
				}

				if (responseDTO != null && responseDTO.isValid()) {
					log.info("Получены валидные данные для страницы - {}, число элементов - {}", i, ids.size());
					writeRepository.saveAiScore(responseDTO.toBusiness());
				} else if (responseDTO != null) {
					log.error("AI-сервис вернул ошибку status={}, message={}", responseDTO.getStatus(), responseDTO.getErrorMessage());
				} else {
					log.error("Ответ AI-сервиса пуст");
				}

			} else {
				log.error("Нет данных для страницы {}", i);
			}
		}
	}

	@Override
	public void startCalculateAiScoring() {
		if (!toggleConfig.isFeatureEnabled(AI_PREDICT_FEATURE)) {
			return;
		}

		InitiatePredictionResponseDTO responseDTO = null;
		try {
			log.info("Запуск вычисления AI-скоринга");
			ResponseEntity<InitiatePredictionResponseDTO> responseEntity = restTemplate.exchange(
					properties.getUrlInitPrediction(), HttpMethod.POST, HttpEntity.EMPTY, InitiatePredictionResponseDTO.class);
			responseDTO = responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			log.error("Попытка инициировать к AI-сервис завершилась ошибкой httpCode={}", e.getStatusCode());
		}

		if (responseDTO != null && responseDTO.isValid()) {
			log.info("AI-сервис инициирован успешно");
		} else if (responseDTO != null) {
			log.error("AI-сервис при попытке иницииации вернул ошибку status={}, message={}", responseDTO.getStatus(), responseDTO.getErrorMessage());
		} else {
			log.error("Ответ AI-сервиса при попытке иницииации пуст");
		}
	}
}
