#!/bin/bash
#
# Start script for file-validation-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" "file-validation-api.jar"