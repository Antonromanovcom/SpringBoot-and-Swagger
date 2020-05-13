SELECT a.id,
       a.client_name,
       a.phone,
       city.name                          as city_name,
       a.client_email,
       a.inn,
       a.ogrn,
       a.resident_address,
       a.head,
       a.head_tax_number,
       a.primary_codes,
       a.secondary_codes,
       a.black_listed_codes,
       a.risky_codes,
       a.billing_plan,
       hi.text                            as last_comment_text,
       hi.created_at                      as last_comment_time,
       hi.event_initiator                 as last_comment_initiator,
       a.p550check,
       a.p550check_head,
       a.p550check_founder,
       a.kontur_error_text,
       a.kontur_check,
       a.smev_check,
       a.passport_check,
       a.arrests_fns,
       a.income,
       a.tax_form,
       a.account_number,
       a.client_state,
       a.date_created,
       sw.status                          as start_work_status,
       a.source,
       a.creator,
       utm.utm_source,
       utm.utm_medium,
       utm.utm_term,
       utm.utm_campaign,
       utm.url,
       shi4.created_at                    as manager_processing_time,
       shi4.created_by                    as manager_processing_initiator

FROM account_application AS a
         LEFT JOIN city
                   on a.city_id = city.id
         LEFT JOIN history_item hi
                   on a.id = hi.app_id AND hi.created_at =
                                           (SELECT max(history_item.created_at)
                                            FROM history_item
                                            WHERE history_item.app_id = a.id
                                              AND history_item.item_type = 0)
         LEFT JOIN start_work sw
                   on a.id = sw.app_id AND sw.start_at =
                                           (SELECT min(start_work.start_at)
                                            FROM start_work
                                            WHERE start_work.app_id = a.id)
         LEFT JOIN utm
                   on utm.id = a.utm_id
         LEFT JOIN sm_transition_log shi4
                   on a.id = shi4.client_id AND shi4.created_at =
                                             (SELECT max(sm_transition_log.created_at)
                                              FROM sm_transition_log
                                              WHERE sm_transition_log.client_id = a.id
                                                AND sm_transition_log.new_state = 'MANAGER_PROCESSING')
WHERE a.date_created BETWEEN :from AND :to
  AND a.active = true
