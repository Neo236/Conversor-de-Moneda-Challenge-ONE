#!/bin/sh
# Sirve el Conversor como una terminal web interactiva (ttyd + xterm.js), detrás de
# nginx bajo /terminal/. La paleta va afinada a la identidad teal del proyecto.
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
  -t 'theme={"background":"#0c1f18","foreground":"#d3e2ce","cursor":"#4fd1b0","cursorAccent":"#0c1f18","selectionBackground":"#1f3d30","black":"#0c1f18","red":"#d98a6a","green":"#3fbf85","yellow":"#c99a5a","blue":"#6fb0a0","magenta":"#b98fb0","cyan":"#5fd0a8","white":"#d3e2ce","brightBlack":"#4a6357","brightRed":"#e8a487","brightGreen":"#66d29e","brightYellow":"#dcb87a","brightBlue":"#8fc4b6","brightMagenta":"#cfabc7","brightCyan":"#83e0c0","brightWhite":"#eef4ea"}' \
  /opt/conversor/web-run.sh
