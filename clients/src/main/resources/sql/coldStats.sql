select a.source                                                                 as source,
       a.creator                                                                as creator,
       a.assigned_to                                                            as operator,

       sum(case when sh.new_state = 'NEW_CLIENT' then 1 else 0 end)             as newClient,
       sum(case when sh.new_state = 'CONTACT_INFO_CONFIRMED' then 1 else 0 end) as contactInfoConfirmed,
       sum(case when sh.new_state = 'NO_ANSWER' then 1 else 0 end)              as noAnswer,
       sum(case when sh.new_state = 'CHECK_LEAD' then 1 else 0 end)             as checkLead,
       sum(case when sh.new_state = 'CLIENT_DECLINED' then 1 else 0 end)        as clientDeclined,
       sum(case when sh.new_state = 'WAIT_FOR_DOCS' then 1 else 0 end)          as waitForDocs,
       sum(case when sh.new_state = 'DOCUMENTS_EXISTS' then 1 else 0 end)       as documentsExists,
       sum(case when sh.new_state = 'REQUIRED_DOCS' then 1 else 0 end)          as requiredDocs,
       sum(case when sh.new_state = 'MANAGER_PROCESSING' then 1 else 0 end)     as managerProcessing,
       sum(case when sh.new_state = 'ACTIVE_CLIENT' then 1 else 0 end)          as activeClient,
       sum(case when sh.new_state = 'INACTIVE_CLIENT' then 1 else 0 end)        as inactiveClient,
       sum(case when sh.new_state = 'AUTO_DECLINED' then 1 else 0 end)          as autoDeclined
from sm_transition_log sh
         left join account_application a on a.id = sh.client_id
where a.source in (:source)
  and sh.previous_state <> sh.new_state
  and a.date_created between :from and :to
group by a.source, a.creator, a.assigned_to;
