package online.prostobank.clients.api;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiConstants {

    public static final String REGION = "RU";

    // mainAnketaEndpoint
    public static final int MAX_AVAILABLE_ATTEMPTS                      = 3;
    public static final String AUTHORIZATION                            = "Authorization";
    public static final String BEARER_                                  = "Bearer ";
    public static final String BIDS_CONTACT_INFO_CONFIRM                = "bids/contact-info-confirm";
    public static final String BIDS_CONTACT_INFO_VERIFY                 = "bids/contact-info-verify";
    public static final String BIDS_CREATE                              = "bids/create";
    public static final String BOOK                                     = "book";
    public static final String CHECK_SMS_CODE_CACHE                     = "checkSmsCodeCache";
    public static final String CITIES                                   = "dictionaries/cities";
    public static final String ORGANIZATIONS                            = "organizations";
    public static final String SEND_SMS_CODE_CACHE                      = "sendSmsCodeCache";
    public static final String PROMOCODE                                = "promocode";

    public static final String X_CSRF_TOKEN                             = "X-CSRF-TOKEN";
    public static final String X_FORWARDED_FOR                          = "X-FORWARDED-FOR";

    // Endpoints
    public static final String API_ROOT                                = "/api/";
    public static final String ACCOUNT_APPLICATION_AVRO_CONTROLLER     = "/api/avro/account_application/";
    public static final String ACCOUNT_APPLICATION_CONTROLLER          = "/api/account_application/";
    public static final String ANKETA_MAIN                             = "/api/main/";
    public static final String ANKETA_NEW_CONTROLLER                   = "/api/new/";
    public static final String ANKETA_V2_CONTROLLER                    = "/api/v2/main/";
    public static final String ANKETA_V3_CONTROLLER                    = "/api/v3/main/";
    public static final String ANKETA_V4_CONTROLLER                    = "/api/v4/main/";
    public static final String ATTACHMENT_CONTROLLER                   = "/api/attachment";
    public static final String CALL_CENTER_CONTROLLER                  = "/callcenter/api/";
    public static final String CHATBOT_CONTROLLER                      = "/api/chatbot/";
    public static final String CONFIG_CONTROLLER                       = "/config/";
    public static final String STATISTIC_CONTROLLER                    = "/api/stats/";
    public static final String CLIENT_ATTACHMENT_CONTROLLER            = "/api/client_attachment/";
    public static final String ORGANIZATION_CONTROLLER                 = "/api/organisation/";
    public static final String DICTIONARY_CONTROLLER                   = "/api/dictionary/";
    public static final String CLIENT_CONTROLLER                       = "/api/client/";
    public static final String CLIENT_DBO_CONTROLLER                   = "/api/dbo/client/";
    public static final String CLIENT_POS_CONTROLLER                   = "/api/pos/client/";
    public static final String USER_SECURITY_CONTROLLER                = "/api/users/";
    public static final String AML_CONTROLLER                          = "/api/aml/";
    //external endpoints
    public static final String DCC_CONTROLLER = "/dcc/api/";

    // endpoints accountApplication rest controller
    public static final String CHANGE_ASSIGNER                    = "change_assigner";
    public static final String COUNT_ACTIVE_APPLICATIONS_BY_EMAIL = "count_all_by_client_email_and_active_is_true";
    public static final String DEACTIVATE_APPLICATION             = "deactivate_application";
    public static final String FIND_BY_ACCOUNT_ACCOUNT_NUMBER     = "find_by_account_account_number";
    public static final String FIND_BY_DATE_CREATED_RANGE         = "find_by_date_created_between_and_active_true_order_by_date_created";
    public static final String FIND_BY_ID                         = "find_by_id";
    public static final String FIND_BY_LOGIN_URL_AND_ACTIVE       = "find_by_login_url_and_active";
    public static final String FIND_BY_PHONE_UNCONFIRMED_APPS     = "find_unconfirmed_apps_by_phone";
    public static final String FIND_BY_STATUS_AND_EMAIL           = "find_by_status_and_email";
    public static final String FIND_BY_STATUS_AND_REQUEST_ID      = "find_all_by_status_with_request_id";
    public static final String FIND_BY_ACTIVE_BY_INN_OGRN         = "find_by_active_inn_ogrn";
    public static final String FIND_BY_ACCOUNT_NUMBER             = "find_by_account_number";
    public static final String FIND_BY_KEYCLOAK_ID                = "find_by_keycloak_id";
    public static final String FIND_UNCONFIRMED_APPS_BY_INN       = "find_unconfirmed_apps_by_inn";
    public static final String GET_ALL                            = "get_all";
    public static final String GET_ALL_CLIENTS                    = "get_all_clients";
    public static final String GET_ASSIGNED_TO                    = "get_assigned";
    public static final String GET_ALL_BY_EXAMPLE                 = "get_all_by_example";
    public static final String GET_ATTACHMENTS_SIZE               = "get_attachments_size";
    public static final String SAVE_ALL                           = "save_all";
    public static final String CREATE_COLD_APPLICATION            = "create_cold_application";

    public static final String CREATE_CLIENT_CARD                 = "create_client_card";
    public static final String SAVE_EDIT_CLIENT_INFO              = "save_edit_client_info";
    public static final String SAVE_EDIT_QUESTIONNAIRE            = "save_edit_questionnaire";
    public static final String CHANGE_CLIENT_STATUS               = "change_client_status";
    public static final String CHECK_CLIENT                       = "check_client";
    public static final String SAVE_COMMENT                       = "save_comment";
    public static final String SEND_TO_USER                       = "send_to_user";
    public static final String SMS_REMINDER                       = "sms_reminder";
    public static final String EXPORT_PDF                         = "export_pdf";
    public static final String ASSIGN_TO_ME                       = "assign_to_me";
    public static final String ASSIGN_TO                          = "assign_to";
    public static final String START_WORK                         = "start_work";
    public static final String SMS_CHECK                          = "sms_check";
    public static final String RESET_SMS_CHECK                    = "reset_sms_check";
    public static final String SMS_CONFIRMATION                   = "sms_confirmation";
    public static final String EDIT_PASSPORT                      = "edit_passport";
    public static final String SEND_MESSAGE_TO_CLIENT             = "send_message_to_client";
    public static final String SAVE_HISTORY_ITEM                  = "save_history_item";
    public static final String NEED_DOCS                          = "need_docs";
    public static final String DECLINE                            = "decline";
    public static final String SAVE_AUX_INFO                      = "save_client_info";

    public static final String CARD                               = "card";
    public static final String HUMAN                              = "human";
    public static final String EMPLOYEES                          = "employees";
    public static final String BENEFICIARIES                      = "beneficiaries";
    public static final String EMPLOYEE                           = "employee";
    public static final String BENEFICIARY                        = "beneficiary";
    public static final String PASSPORT                           = "passport";
    public static final String EMAIL                              = "email";
    public static final String PHONE                              = "phone";

    public static final String GET_STATUSES                       = "statuses";
    public static final String GET_CITIES                         = "cities";
    public static final String GET_DOC_TYPES                      = "doc_types";
    public static final String GET_USER_ROLES                     = "user_roles";

    // KeycloakController
    public static final String GET_ALL_KEYCLOAK_USERS = "getAllKeycloakUsers";
    public static final String GET_KEYCLOAK_USER_BY_USERNAME = "getKeycloakUserByUsername";
    public static final String EXIST_APPLICATIONS = "existApplications";

    // response messages
    public static final String ACCEPTED                                    = "Accepted";
    public static final String NOT_FOUND                                   = "Not found";
    public static final String CLIENT_NOT_FOUND                            = "Client not found";
    public static final String DICTIONARY_NOT_FOUND                        = "Dictionary not found";
    public static final String CLIENT_CARD_NOT_FOUND                       = "Карточка клиента не найдена";
    public static final String CLIENT_CARD_NOT_FOUND_OR_NOT_MODIFIED       = "Карточка клиента не найдена или её модификация не удалась";
    public static final String HUMAN_NOT_FOUND_OR_NOT_MODIFIED             = "Сведения о физлице не найдены или их модификация не удалась";
    public static final String PASSPORT_NOT_FOUND                          = "Паспорт не найден";
    public static final String EMAIL_NOT_FOUND                             = "Почта не найдена";
    public static final String PHONE_NOT_FOUND                             = "Телефон не найден";
    public static final String EMPLOYEE_NOT_FOUND_OR_NOT_MODIFIED          = "Сотрудник не найден или сведения о нём не удалось модифицировать";
    public static final String BENEFICIARY_NOT_FOUND_OR_NOT_MODIFIED       = "Бенифициар не найден или сведения о нём не удалось модифицировать";

    public static final String EXCEPTION_MESSAGE = "Unable to process response";

    // StateController

    public static final String API_STATE                                   = "/state";
    public static final String API_STATE_AVAILABLE                         = "/available_states";

}
