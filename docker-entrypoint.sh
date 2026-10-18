#!/bin/sh
# Reads Docker secrets from *_FILE env vars and exports them as plain env vars.
# Needed because Spring Boot reads DB_PASSWORD / JWT_SECRET, not *_FILE variants.
for var in DB_PASSWORD JWT_SECRET; do
  file_var="${var}_FILE"
  eval file_path=\$$file_var
  if [ -n "$file_path" ] && [ -f "$file_path" ]; then
    val=$(cat "$file_path")
    export "$var=$val"
  fi
done

exec "$@"