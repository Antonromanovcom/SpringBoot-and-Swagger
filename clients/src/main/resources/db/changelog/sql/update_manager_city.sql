update manager
set city_id = (select id from city where city.name = manager.region)
where region in (select name from city);
