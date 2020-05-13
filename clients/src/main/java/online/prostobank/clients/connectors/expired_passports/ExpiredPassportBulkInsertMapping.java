package online.prostobank.clients.connectors.expired_passports;

import de.bytefish.pgbulkinsert.mapping.AbstractMapping;

class ExpiredPassportBulkInsertMapping extends AbstractMapping<ExpiredPassport> {
    protected ExpiredPassportBulkInsertMapping(String schemaName, String tableName) {
        super(schemaName, tableName);

        mapShort("series", ExpiredPassport::getSeries);
        mapInteger("number", ExpiredPassport::getNumber);
    }
}