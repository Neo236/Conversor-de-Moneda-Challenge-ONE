# Conversor de Monedas

[![CI](https://github.com/Neo236/Conversor-de-Moneda-Challenge-ONE/actions/workflows/ci.yml/badge.svg)](https://github.com/Neo236/Conversor-de-Moneda-Challenge-ONE/actions/workflows/ci.yml)

Aplicación de consola en Java que convierte divisas con la cotización del momento, contra la
[ExchangeRate-API](https://www.exchangerate-api.com/). Elegís las monedas y la cantidad, y te
da el resultado y la tasa; cada conversión queda en un historial que persiste entre sesiones,
y los comandos (lista de monedas, historial, salir) responden en cualquier momento sin perder
la operación en curso.

Salió del challenge de **Alura Latam** (programa Oracle Next Education).

<img src="docs/demo.svg" alt="Sesión del Conversor: dos conversiones en vivo (USD a BRL y EUR a ARS) con la tasa del momento, el historial paginado y los comandos" width="70%">

**[Ver la demo en vivo →](https://neo236.github.io/Conversor-de-Moneda-Challenge-ONE/)**

Es una app de consola, así que sus colores no se ven en la terminal de Windows. La demo web
reproduce una sesión real —tecleo incluido— para que se aprecien sin instalar nada.

## Qué hace

- Convierte entre más de 150 monedas con la cotización del momento.
- Comandos globales: `l` (lista), `h` (historial) o `salir` en cualquier momento, sin perder
  la operación en curso.
- Lista de monedas paginada y con búsqueda por código o nombre (`b`).
- Historial persistente: cada conversión se guarda en `historial.json` con su fecha y se
  navega paginado, de la más reciente a la más vieja.
- La lista de monedas se pide a la API una sola vez por sesión (caché).

## Cómo ejecutarlo

Hace falta una API Key de ExchangeRate-API (gratis en
[exchangerate-api.com](https://www.exchangerate-api.com/)). En todos los casos, si no definís
la variable de entorno, la app te pide la clave por teclado y la usa solo en memoria: nunca se
escribe a disco ni aparece en los logs.

**Terminal web, sin instalar nada.** La app se auto-hostea como una terminal de navegador con
[`ttyd`](https://github.com/tsl0922/ttyd): cada pestaña que se conecta corre su propia
instancia real, con sus colores y su teclado, contra la API en vivo. La clave vive solo del
lado del servidor y nunca viaja al navegador.

```bash
export EXCHANGE_RATE_API_KEY="tu_clave"
docker compose -f docker-compose.web.yml up -d --build
```

Después se abre en `http://IP-DEL-HOST:8091` desde cualquier dispositivo de la red.

**Con Docker, sin instalar Java.**

```bash
export EXCHANGE_RATE_API_KEY="tu_clave"
docker compose run --rm conversor
```

Se usa `run` y no `up` porque el menú se maneja por teclado.

**Con un JDK 21 local.** Gradle no hace falta: lo baja el wrapper.

```bash
export EXCHANGE_RATE_API_KEY="tu_clave"
./gradlew run
```

Para armar una distribución con su script de arranque: `./gradlew installDist` y después
`./build/install/conversor/bin/conversor`.

## Comandos

| En cualquier momento | |
| --- | --- |
| `l` / `lista` | Abre la lista de monedas |
| `h` / `historial` | Abre el historial de conversiones |
| `salir` | Termina el programa |

| Dentro de una lista | |
| --- | --- |
| `s` / `siguiente` | Página siguiente |
| `a` / `anterior` | Página anterior |
| `b` / `buscar` | Filtra por término (solo en la lista de monedas) |
| `v` / `volver` | Vuelve a donde estabas |

## Tests

```bash
./gradlew test
```

50 tests, sin tocar la red: el `HttpClient` entra por constructor, así que las respuestas de
la API están simuladas, y la interfaz se ejercita con un teclado y una salida de mentira.

## Estructura

```
com.alura.conversor
├── Main.java        Punto de entrada: arma las piezas
├── api/             Cliente de ExchangeRate-API y sus respuestas
├── historial/       Persistencia del historial en JSON
└── ui/              Consola, paginador y flujo de la aplicación
```

## Decisiones de diseño

**La plata es `BigDecimal`, no `double`.** No es purismo: con `double`, convertir 10.000.000
armaba la URL `/pair/USD/ARS/1.0E7` —porque así serializa Java un double grande— y la API
devolvía 404. El mismo cambio que arregla la precisión arregla el bug: `toPlainString()`.

**Los errores se leen del cuerpo, no del código HTTP.** ExchangeRate-API responde 200 con
`result: "error"` cuando el código de moneda no existe, y 403 con `error-type: invalid-key`
cuando la clave está mal. Mirar solo el status daría "error 403" cuando se puede decir "la API
Key no es válida".

**Los logs no van a la consola.** La pantalla es de la interfaz ANSI; el log va a
`logs/conversor.log`. Antes cada conversión imprimía sus líneas de log en medio de la interfaz.

**Un historial corrupto no impide arrancar.** Se carga desde el constructor, así que un JSON
inválido tiraba la aplicación abajo antes de mostrar el menú. Ahora se avisa por el log y se
empieza uno nuevo.

**Hay un solo paginador.** La lista de monedas y el historial tenían cada uno su bucle:
cuarenta líneas casi calcadas con los mismos comandos. `Paginador<T>` es ese bucle, una sola
vez.

**La entrada estándar se puede cerrar.** Con Ctrl+D o una tubería que termina,
`Scanner.nextLine()` lanza `NoSuchElementException`; antes eso escapaba de `main` y le mostraba
un stack trace al usuario. Ahora la aplicación cierra ordenadamente.

## Tecnologías

Java 21 · Gradle · `java.net.http.HttpClient` · Gson · SLF4J + Logback · JUnit 5 · Mockito · Docker · ttyd

---

Hecho por Lautaro Sebastian Mambrin (Neo236).
