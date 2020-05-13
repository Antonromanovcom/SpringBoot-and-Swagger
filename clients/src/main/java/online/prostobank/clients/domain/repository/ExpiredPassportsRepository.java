package online.prostobank.clients.domain.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ExpiredPassportsRepository {

    private JdbcTemplate template;

    private final String existsQuery = "SELECT count(1) FROM public.expired_passports WHERE number = ? AND SERIES = ?";

    public ExpiredPassportsRepository(JdbcTemplate template){

        this.template = template;
    }

    public boolean exists(String series, String number) {

        short parsedSeries = Short.parseShort(series);
        int parsedNumber = Integer.parseInt(number);

        return exists(parsedSeries, parsedNumber);
    }

    public boolean exists(short series, int number) {
        Integer exists = template.queryForObject(existsQuery,
                new Object[] { number, series },
                Integer.class);

        return exists != null && exists > 0;
    }
}
