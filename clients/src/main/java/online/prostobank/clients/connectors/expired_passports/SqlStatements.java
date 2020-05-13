//language=PostgreSQL

package online.prostobank.clients.connectors.expired_passports;

class SqlStatements {
    public static final String createNewTableNumberIndex = "CREATE INDEX IX_expired_passports_new_number ON public.expired_passports_new (\"number\")";
    public static final String dropNewTableIfExists = "DROP TABLE IF EXISTS public.expired_passports_new";
    public static final String dropOldTableIfExists = "DROP TABLE IF EXISTS public.expired_passports_old";
    public static final String createNewTable = "CREATE TABLE public.expired_passports_new (\"number\" integer NOT NULL, \"series\" smallint NOT NULL)";
    public static final String renameOldTable = "ALTER TABLE IF EXISTS public.expired_passports RENAME TO expired_passports_old";
    public static final String renameNewTable = "ALTER TABLE public.expired_passports_new RENAME TO expired_passports";
    public static final String renameNewTableIndex = "ALTER INDEX IF EXISTS IX_expired_passports_new_number RENAME TO IX_expired_passports_number";
    public static final String renameOldTableIndex = "ALTER INDEX IF EXISTS IX_expired_passports_number RENAME TO IX_expired_passports_old_number";
}
