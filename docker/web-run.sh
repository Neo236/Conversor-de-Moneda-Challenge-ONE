#!/bin/sh
# Se ejecuta una vez por cada pestaña que se conecta a la terminal web.
#
# Cada visitante arranca en un espacio de trabajo efímero y propio: historial
# limpio y logs aislados, sin pisarse con las otras sesiones. Al cerrar la
# pestaña, ese directorio queda huérfano bajo /tmp y no molesta a nadie.
set -e

sesion="$(mktemp -d /tmp/conversor.XXXXXX)"
cd "$sesion"

# HistorialEnArchivo respeta esta variable; logback escribe logs/ relativo al cwd.
export HISTORY_FILE_PATH="$sesion/historial.json"

exec /opt/conversor/bin/conversor
