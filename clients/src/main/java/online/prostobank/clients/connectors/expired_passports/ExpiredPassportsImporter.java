package online.prostobank.clients.connectors.expired_passports;

import de.bytefish.pgbulkinsert.IPgBulkInsert;
import de.bytefish.pgbulkinsert.util.PostgreSqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamProvider;
import org.apache.commons.lang3.math.NumberUtils;
import org.postgresql.PGConnection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.stream.Stream;

@Slf4j
@Component
@ConditionalOnProperty(
        value="expiredpassports.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ExpiredPassportsImporter {

    private JdbcTemplate jdbcTemplate;
    private ExpiredPassportsImporterConfiguration configuration;
    private final CompressorStreamProvider streamFactory;
    private final IPgBulkInsert<ExpiredPassport> bulkInsert;

    private final int oneMinute = 60000;

    public ExpiredPassportsImporter(JdbcTemplate jdbcTemplate,
                                    ExpiredPassportsImporterConfiguration configuration,
                                    CompressorStreamProvider streamFactory,
                                    IPgBulkInsert<ExpiredPassport> bulkInsert) {

        this.jdbcTemplate = jdbcTemplate;
        this.configuration = configuration;
        this.streamFactory = streamFactory;
        this.bulkInsert = bulkInsert;
    }

    @Transactional
    @Scheduled(cron = "${expiredpassports.cron}")
    @Retryable(maxAttempts = 5, value = { Exception.class }, backoff = @Backoff(delay = 5 * oneMinute))
    public void run() {
        try {
            log.info("Начинаю задачу по загрузке недействительных паспортов");
            InputStream fileStream = new BufferedInputStream(getHttpInputStream());
            CompressorInputStream input = streamFactory.createCompressorInputStream(CompressorStreamFactory.BZIP2, fileStream, false);
            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input))) {
                jdbcTemplate.execute((StatementCallback<Object>) statement ->  {
                    PGConnection pgConnection = PostgreSqlUtils.getPGConnection(statement.getConnection());
                    statement.executeUpdate(SqlStatements.dropNewTableIfExists);
                    statement.executeUpdate(SqlStatements.createNewTable);
                    bulkInsert.saveAll(pgConnection, getExpriedPassportsStream(bufferedReader));
                    statement.executeUpdate(SqlStatements.createNewTableNumberIndex);
                    statement.executeUpdate(SqlStatements.renameOldTable);
                    statement.executeUpdate(SqlStatements.renameNewTable);
                    statement.executeUpdate(SqlStatements.renameOldTableIndex);
                    statement.executeUpdate(SqlStatements.renameNewTableIndex);
                    statement.executeUpdate(SqlStatements.dropOldTableIfExists);
                    return null;
                });
            }
        } catch (Exception ex) {
            log.error("Ошибка при загрузке недействительных паспортов", ex);
        }
    }

    private static Stream<ExpiredPassport> getExpriedPassportsStream(BufferedReader reader) {
        Stream<ExpiredPassport> resultStream = reader.lines()
                .map(line -> {
                    String[] splitted = line.split(",");
                    short series = NumberUtils.toShort(splitted[0], (short) -1);
                    int number = NumberUtils.toInt(splitted[1], -1);

                    ExpiredPassport expiredPassport = new ExpiredPassport();
                    expiredPassport.setNumber(number);
                    expiredPassport.setSeries(series);
                    return expiredPassport;
                })
                .filter(ep -> ep.getNumber() != -1 && ep.getSeries() != -1);

        return resultStream;
    }

    private InputStream getHttpInputStream() throws IOException {
        URL archiveUrl = new URL(configuration.getHttpsurl());
        HttpsURLConnection conn = (HttpsURLConnection) archiveUrl.openConnection();
        conn.setReadTimeout(oneMinute);
        conn.setConnectTimeout(oneMinute);
        return conn.getInputStream();
    }

    private static short tryParseShort(String string){
        try {
            return Short.parseShort(string);
        }
        catch (NumberFormatException ex){
            return -1;
        }
    }

    private static int tryParseInt(String string){
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException ex){
            return -1;
        }
    }
}
