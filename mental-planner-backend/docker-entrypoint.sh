#!/bin/bash
set -e

# Heroku Postgres injects DATABASE_URL as postgres://user:pass@host:port/db.
# application.properties expects the JDBC_DATABASE_* variables, so convert here.
if [ -n "$DATABASE_URL" ]; then
  regex='^postgres(ql)?://([^:]+):([^@]+)@([^:/]+):([0-9]+)/(.+)$'
  if [[ $DATABASE_URL =~ $regex ]]; then
    export JDBC_DATABASE_USERNAME="${BASH_REMATCH[2]}"
    export JDBC_DATABASE_PASSWORD="${BASH_REMATCH[3]}"
    export JDBC_DATABASE_URL="jdbc:postgresql://${BASH_REMATCH[4]}:${BASH_REMATCH[5]}/${BASH_REMATCH[6]}"
  fi
fi

exec java $JAVA_OPTS -Dserver.port="${PORT:-8080}" -jar app.jar
