/**
 *                   TRY_RESERVE                    +--------------+    NOT_CALLING              DECLINE
 *                  +------------------------------>+ Новый клиент +-----------------------+----------------------------+
 *                  |                               +------+-------+                       |                            |
 *                  |                                      | SMS        CONFIRMED          v                            v
 *                  |                                      v            +-----------> +----+-----+              +-------+-----+
 *                  |                          +-----------+------------+             | Недозвон |              |Отказ клиента|
 *                  |                          |Информация подтверждена++             +----------+              +-+--------+--+
 *                  |                          +-----------+-----------+ <-------------+                          ^        |
 *                  |                                      |             UNCONFIRMED                              |        |
 *                  |                           CHECKS     |                                                      |        |
 *                  |                                      v                                                      |        |
 *                  |                             +--------+--------+                                             |        |
 *                  |                  +----------+  Проверка лида  |                                             |        |
 *                  |                  |          +--------+--------+                                             |        |
 *                  |                  |                   |                                                      |        |
 *                  |                  |                   | CHECKS_DONE                                          |        |
 *                  |                  |                   v                                                      |        |
 *                  |                  |        +----------+----------+          DECLINE                          |        |
 *                  |                  +<-------+ Ожидание документов +-------------------------------------------+        |
 *          +-------+---------+        |        +----------+----------+                                                    |
 * +------->+ Клиент неактивен+<--------------+            | RESERVE                                                       |
 * |        +------+----------+        |      |            v             DOCS_ADDED                                        |
 * |               ^ ACCOUNT_CLOSE     |      +------------+------------------------> +-------------------+                |
 * |               |                   |      | В процессе открытия счет|             |Дозапрос документов|                |
 * |         +-----+----------+        |      +------------+------------+ <--------------------+----------+                |
 * |         | Клиент активен |        |      |            |                NEED_DOCS          |                           |
 * |         +----------------+<--------------+            |                                   |                           |
 * |                    ACCOUNT_OPEN   +------------------>-<----------------------------------+                           |
 * |                                                       |   AUTO_DECLINE                                                |
 * |                                                       v                                                               |
 * |                                                 +-----+------+                                                        |
 * +<------------------------------------------------+  Автоотказ |                                                        |
 * |                                                 +------------+                                                        |
 * |                                                                                                                       |
 * |                                                                                                                       |
 * +-----------------------------------------------------------------------------------------------------------------------+
 *
 *                                                                                   MAKE_COLD
 *
 * Описание переходов {@link online.prostobank.clients.domain.state.event.ClientEvents}
 */
package online.prostobank.clients.domain.state.event;