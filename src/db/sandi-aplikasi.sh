#!/usr/bin/env bash
# Memberi kata sandi peran aplikasi sehati_app dari DB_PASSWORD pada src/.env.
# Dijalankan docker-entrypoint setelah schema.sql dan seed.sql, sehingga kata sandi tidak pernah tertulis di repo.

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    -v sandi="$SEHATI_APP_PASSWORD" <<'EOSQL'
ALTER ROLE sehati_app PASSWORD :'sandi';
EOSQL
