#!/usr/bin/env sh
set -eu
docker compose up -d postgres
printf '\nBanco disponível. Em terminais separados execute:\n'
printf '  ./mvnw spring-boot:run\n'
printf '  cd frontend && npm install && npm run dev\n'
