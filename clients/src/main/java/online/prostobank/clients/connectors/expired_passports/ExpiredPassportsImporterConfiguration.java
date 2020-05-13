package online.prostobank.clients.connectors.expired_passports;

import de.bytefish.pgbulkinsert.IPgBulkInsert;
import de.bytefish.pgbulkinsert.PgBulkInsert;
import de.bytefish.pgbulkinsert.mapping.AbstractMapping;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@ConfigurationProperties(prefix = "expiredpassports")
@EnableRetry
public class ExpiredPassportsImporterConfiguration {

    @Value("${spring.jpa.properties.hibernate.default_schema:public}")
    private String schema;

    private String httpsurl;

    public static final String newTableName = "expired_passports_new";

    @Bean
    public CompressorStreamProvider compressorStreamProvider() {
        return new CompressorStreamFactory();
    }

    @Bean
    public AbstractMapping<ExpiredPassport> bulkMapping() {
        return new ExpiredPassportBulkInsertMapping(schema, newTableName);
    }

    @Bean
    public IPgBulkInsert<ExpiredPassport> bulkInserter() {
        return new PgBulkInsert(bulkMapping());
    }

    public String getHttpsurl() {
        return httpsurl;
    }

    public void setHttpsurl(String httpsurl) {
        this.httpsurl = httpsurl;
    }
}
