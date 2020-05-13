package online.prostobank.clients.domain.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.statistics.dto.*;
import online.prostobank.clients.domain.statuses.StatusValue;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static online.prostobank.clients.domain.statistics.ReportUtils.sqlFromFile;
import static online.prostobank.clients.domain.statuses.Status.decodeStatus;

@Slf4j
@RequiredArgsConstructor
@Repository
public class StatisticsRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static String fullReportSql = sqlFromFile("sql/createAccountFullReport.sql");
    private static String accountCountSql = sqlFromFile("sql/accountCount.sql");
    private static String coldStatsSql = sqlFromFile("sql/coldStats.sql");

    public List<DailyTransitionStats> readStatusTransitions(Instant from, Instant to) {
        // TODO: заменить array_agg на нормальный last()
        // TODO: уточнить производительность агрегации по date_trunc(), может быть субоптимальной
        // date_trunc('day') превращает '2019-02-03 21:00' в '2019-02-03 00:00',
        // часовые интервалы схлопываются до суточных.
        //
        // array_agg(...)[1] - убогий агрегат last() для бедных,
        // агрегируемые значения в обратном порядке складываются в массив и берется первое
        // (чтобы не усложнять запрос поиском размера массива)
        return jdbcTemplate.query(
                "select " +
                        "date_trunc('day', timeslice) as timeslice, city.name as city, status_category," +
                        "sum(t_from) as t_from, sum(t_to) as t_to, " +
                        "(array_agg(last_total ORDER BY timeslice DESC))[1] as last_total " +
                        "from statistics.status_transitions " +
                        "join city on city.id = city_id " +
                        "where timeslice >= :from and timeslice < :to " +
                        "group by (date_trunc('day',timeslice), status_category, city) " +
                        "order by 1 desc, 2",
                getSqlParameterSource(from, to),
                (it, count) -> new DailyTransitionStats() {{
                    date = LocalDate.from(it.getTimestamp("timeslice").toLocalDateTime());
                    readableStatus = decodeStatus(it.getString("status_category"));
                    city = it.getString("city");
                    from = it.getLong("t_from");
                    to = it.getLong("t_to");
                    total = it.getLong("last_total");
                }});
    }

    public void writeStatusTransition(AccountApplication aa, StatusValue oldStatus, StatusValue newStatus) {
        // TODO: подумать, а что тут брать вообще
        // TODO: выяснить, выкидываются ли подзапросы в невыполняющихся ветках апсертов
        Instant timestamp = Instant.now();
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("timestamp", Timestamp.from(timestamp))
                .addValue("old_category", statusKey(oldStatus))
                .addValue("new_category", statusKey(newStatus))
                .addValue("city", aa.getCity().getId());
        // здесь происходят подряд два insert-or-update.
        // timeslice - таймстамп события, округленный до часа вниз '2019-02-03 21:17:23' -> '2019-02-03 21:00:00',
        // status_category - изначально планировалось группировать статусы в категории, сейчас этого нет
        // t_from - transitions from [status category]
        // t_to - transitions to [status category]
        // last_total - количество заявок в status category на момент завершения интервала
        //
        // первый апсерт - про предыдущий статус заявки.
        // если апсерт превращается в вставку - from=1, to=0
        // если в апдейт - from += 1, last_total -= 1
        //
        // второй - про новый статус
        // вставка - from=0, to=1
        // апдейт - to += 1, last_total += 1
        if (oldStatus != null) {
            jdbcTemplate.update(
                    "insert into statistics.status_transitions(" +
                            "timeslice, status_category, city_id, t_from, t_to, last_total" +
                            ") values (" +
                            "date_trunc('hour', :timestamp::timestamp), :old_category, :city, 1, 0, (" +
                            "select count(1)-1 from account_application where status = :old_category and city_id = :city" +
                            ")) " +
                            "on conflict(timeslice, status_category, city_id) do update set " +
                            "t_from = status_transitions.t_from + 1," +
                            "last_total = status_transitions.last_total - 1",
                    namedParameters);
        }
        jdbcTemplate.update(
                "insert into statistics.status_transitions(" +
                        "timeslice, status_category, city_id, t_from, t_to, last_total" +
                        ") values (" +
                        "date_trunc('hour', :timestamp::timestamp), :new_category, :city, 0, 1, (" +
                        "select count(1)+1 from account_application where status = :new_category and city_id = :city" +
                        ")) " +
                        "on conflict(timeslice, status_category, city_id) do update set " +
                        "t_to = status_transitions.t_to + 1," +
                        "last_total = status_transitions.last_total + 1",
                namedParameters);
    }

    /**
     * Выборка заявок, оформленных от имени источника {@param source} в заданных границах дат. При этом на выходе
     * поле sumNew должно содержать все заявки, прошедшие статус NEW или находящиеся в нем (т.е. кумулятивно все успешно оформленные
     * заявки), а в остальных столбцах - число заявок, находящихся в соотв. статусе в текущий момент. Иначе говоря,
     * отчет реализует логику "за период было всего успешно оформлено Х заявок, ИЗ КОТОРЫХ в статусе Y сейчас находится M штук"
     */
    public List<ColdStats> coldStats(@NotNull Instant from, @NotNull Instant to, List<String> source) {
        return jdbcTemplate.query(coldStatsSql,
                getSqlParameterSource(from, to).addValue("source", source),
                new BeanPropertyRowMapper<>(ColdStats.class));
    }

    /**
     * Запись факта использования сервиса распознавания скана документа.
     */
    public void writeSmartEngineUsage(AttachmentFunctionalType functionalType) {
        Instant timestamp = Instant.now();
        String user = SecurityContextHelper.getCurrentUsername();
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("time", Timestamp.from(timestamp))
                .addValue("username", user)
                .addValue("type", functionalType.name());
        jdbcTemplate.update("insert into smartengine_usage (time, username, functional_type) values (:time, :username, :type)", namedParameters);
    }

    /**
     * Отчет об использовании сервиса распознавания скана документа
     */
    public List<SmartEngineUsage> smartEngineUsage(@NotNull Instant from, @NotNull Instant to) {
        return jdbcTemplate.query("select s.username, s.functional_type, sum(1) as count " +
                        "from smartengine_usage s " +
                        "where s.time between :from and :to " +
                        "group by s.username, s.functional_type",
                getSqlParameterSource(from, to),
                new BeanPropertyRowMapper<>(SmartEngineUsage.class));
    }

    public List<AccountCount> accountCount(Instant from, Instant to) {
        return jdbcTemplate.query(accountCountSql,
                getSqlParameterSource(from, to),
                new BeanPropertyRowMapper<>(AccountCount.class));
    }

    public List<AllAttributes> allAttributes(Instant from, Instant to) {
        return jdbcTemplate.query(fullReportSql,
                getSqlParameterSource(from, to),
                (it, count) -> new AllAttributes(it));
    }

    public List<AnalysisAttributes> analysisAttributes(Instant from, Instant to) {
        return jdbcTemplate.query(fullReportSql,
                getSqlParameterSource(from, to),
                (it, count) -> new AnalysisAttributes(it));
    }

    private MapSqlParameterSource getSqlParameterSource(Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
    }

    private String statusKey(StatusValue s) {
        return Optional.ofNullable(s)
                .map(StatusValue::getValue)
                .map(Enum::name)
                .orElse("NULL");
    }
}
