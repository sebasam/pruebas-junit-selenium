#!/usr/bin/env bash
# Compila las pruebas con javac y las ejecuta con JUnit. No necesita Maven ni conexión a internet.
set -e
AQUI="$(cd "$(dirname "$0")" && pwd)"
CP="$AQUI/lib/junit-4.13.2.jar:$AQUI/lib/hamcrest-core-1.3.jar"
rm -rf "$AQUI/build" && mkdir -p "$AQUI/build"
javac -encoding UTF-8 -d "$AQUI/build" -cp "$CP" "$AQUI"/src/test/java/sigr/pruebas/*.java
cd "$AQUI"
exec java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dsigr.raiz="$AQUI/../.." -cp "$AQUI/build:$CP" sigr.pruebas.EjecutarPruebas
