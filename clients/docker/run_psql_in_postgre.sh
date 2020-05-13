#!/usr/bin/env bash

container_name=pos2_postgres
database_name=pos
user_name=posuser
user_password=pospass

if [ -z "$@" ]
then
  echo "Empty command"
  echo "Postgre syntax| ./run_psql_in_postgre.sh <psql_command>"
else
  echo "---------- Result ----------"
  docker exec -it ${container_name} psql -U postgres -c $@;
fi

