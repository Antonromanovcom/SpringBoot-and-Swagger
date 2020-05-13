with source_prepare as (
    select source,
           status,
           active,
           count(*) as all_c
    from account_application
    where date_created BETWEEN :from AND :to
    group by source, status, active
),
     count_by_status as (
         select source,
                sum(all_c) as created
         from source_prepare
         group by source
     ),
     open_and_active as (
         select source,
                sum(all_c) as opened_t
         from source_prepare
         where status = 'FULFILLED'
           and active = true
         group by source
     ),
     open_and_not_active as (
         select source,
                sum(all_c) as opened_f
         from source_prepare
         where status = 'FULFILLED'
           and active = false
         group by source
     ),
     opened as (
         select count_by_status.*,
                coalesce(open_and_active.opened_t, 0)     as opened_t,
                coalesce(open_and_not_active.opened_f, 0) as opened_f
         from count_by_status
                  left join open_and_active on open_and_active.source = count_by_status.source
                  left join open_and_not_active on open_and_not_active.source = count_by_status.source
     ),
     result as (
         select opened.source                                           as source,
                opened.created                                          as created_count,
                opened_t + opened_f                                     as opened_count,
                round(opened_t / (greatest(opened_t + opened_f, 1)), 3) as part_of_active
         from opened
         order by source
     )
select *
from result
