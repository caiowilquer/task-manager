#!/usr/bin/env sh
set -eu
./mvnw clean verify
cd frontend
npm install
npm run test
npm run build
