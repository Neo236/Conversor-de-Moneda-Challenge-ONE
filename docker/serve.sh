#!/bin/sh
# Sirve el Conversor como una terminal web interactiva (ttyd + xterm.js), detrás de
# nginx bajo /terminal/. La paleta va afinada a la identidad "billete" (verde intaglio)
# de la portada, para que la terminal viva combine con el marco.
#
# ttyd corre 'web-run.sh' directamente —no un shell—, así que quien entra solo puede
# usar la app: no hay forma de escaparse a una consola del contenedor.
exec ttyd \
  --writable \
  --port 7681 \
  --base-path /terminal \
  --max-clients 10 \
  --terminal-type xterm-256color \
  -t rendererType=dom \
  -t fontSize=15 \
  -t lineHeight=1.15 \
  -t cursorBlink=true \
  -t disableLeaveAlert=true \
  -t 'titleFixed=Conversor de Moneda — en vivo' \
  -t 'theme={"background":"#0E2620","foreground":"#c9d1d9","cursor":"#66d9e8","cursorAccent":"#0E2620","selectionBackground":"#2C4A3B","black":"#0E2620","red":"#e5806b","green":"#8ce99a","yellow":"#ffd43b","blue":"#74c0fc","magenta":"#b98fb0","cyan":"#66d9e8","white":"#c9d1d9","brightBlack":"#8b949e","brightRed":"#f0a08c","brightGreen":"#a0f0ad","brightYellow":"#ffe08a","brightBlue":"#96d0ff","brightMagenta":"#cfabc7","brightCyan":"#83e0c0","brightWhite":"#f0f6fc"}' \
  /opt/conversor/web-run.sh
