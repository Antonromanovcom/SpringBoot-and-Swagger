package online.prostobank.clients.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiOperation;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.domain.statistics.StatisticsRepository;
import online.prostobank.clients.domain.statistics.dto.DailyTransitionStats;
import online.prostobank.clients.services.StatisticService;
import online.prostobank.clients.utils.aspects.JsonLogger;
import online.prostobank.clients.utils.aspects.aj.CallCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static online.prostobank.clients.api.ApiConstants.STATISTIC_CONTROLLER;
import static online.prostobank.clients.utils.Utils.YYYY_MM_DD;
import static online.prostobank.clients.utils.Utils.toInstant;

@JsonLogger
@RestController
@RequestMapping(STATISTIC_CONTROLLER)
public class StatisticsEndpoint {

    private final StatisticsRepository stats;

    private final StatisticService statisticService;

    @Autowired @Qualifier("benchmarkCounters") Set<CallCounter> benchmark;

    public StatisticsEndpoint(StatisticsRepository stats, StatisticService statisticService) {
        this.stats = stats;
        this.statisticService = statisticService;
    }

    @GetMapping(value = "/transitions")
    @ResponseBody
    public Stream<TransitionStats> rawTransitions(
            @NotNull(message = "Параметр не задан") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @NotNull(message = "Параметр не задан") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return getDailyTransitionStats(from, to).stream().map(it -> new TransitionStats(){{
            city = it.city;
            date = it.date;
            status = it.readableStatus;
            from = it.from;
            to = it.to;
            amount = it.total;
        }});
    }

    @RequestMapping(value = "/transitions", method = RequestMethod.GET, produces = {"text/csv;charset=UTF-8"})
    public ResponseEntity<List<TransitionStats>> getTransitions(
            @NotNull(message = "Параметр не задан") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @NotNull(message = "Параметр не задан") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // этот метод отличается от предыдущего, потому что на выходе мы хотим увидеть КРАСИВЫЙ csv.
        // красивость заключается в том, что значения город+дата пишутся только один раз
        // на отдельной строке для целой группы статусов, примерно так:
        // город;дата;;;;
        // "";"";статус1;...
        // "";"";статус2;...
        // ....

        List<TransitionStats> a = new ArrayList<>();
        String lastKey = "";

        for(DailyTransitionStats it: getDailyTransitionStats(from, to)) {
            // из репозитория мы получаем уже правильно упорядоченные значения,
            // остается только в точки смены города+даты вставить заголовки,
            // что и реализуется простым сравнением ключей из предыдущей и текущей строк
            String key = it.date.toString()+it.city;

            if (!lastKey.equals(key)) {
                // заголовок
                a.add(new TransitionStats(){{
                    city = it.city;
                    date = it.date;
                }});
                lastKey = key;
            }
            // обычная строка
            a.add(new TransitionStats(){{
                status = it.readableStatus;
                from = it.from;
                to = it.to;
                amount = it.total;
            }});
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("content-disposition", "attachment;filename=\"transition-stats.csv\"")
                .body(a);
    }

    @GetMapping("scoring")
    @ResponseBody
    public byte[] getScoringStatistic(
            @NotNull(message = "Параметр не задан") String from,
            @NotNull(message = "Параметр не задан") String to,
            HttpServletResponse response){
        if(null == from || null == to) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new byte[]{};
        }
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"statistic_%s_%s.csv\"",
                from.replaceAll("\\.", "_"),
                to.replaceAll("\\.", "_")));
        return statisticService.getScoringStatisticByInterval(from, to, response);
    }

    @ApiOperation(value = ("Статистика производительности"))
    @GetMapping("benchmark")
    @ResponseBody
    public Object getBenchmarkData() {
        return ResponseDTO.goodResponse(
                "OK",
                benchmark
        );
    }


    private List<DailyTransitionStats> getDailyTransitionStats(LocalDate from, LocalDate to) {
        LocalDate end = ((to != null) ? to : LocalDate.now());
        LocalDate start = (from != null) ? from : end.minusWeeks(1);
        end = end.plusDays(1);

        return stats.readStatusTransitions(toInstant(start), toInstant(end));
    }

    public static class TransitionStats {
        public String city;
        @JsonFormat(pattern = YYYY_MM_DD)
        public LocalDate date;

        public String status;
        public Long from;
        public Long to;
        public Long amount;

    }
}
