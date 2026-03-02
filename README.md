# 💱 Conversor de Monedas - Challenge ONE

¡Bienvenido al **Conversor de Monedas V2**! Esta es una aplicación de consola en Java desarrollada como parte del desafío "Challenge ONE" de Alura Latam. Permite a los usuarios realizar conversiones de divisas en tiempo real consumiendo la [ExchangeRate-API](https://www.exchangerate-api.com/).

## ✨ Características Principales

Más allá de los requisitos básicos del desafío, esta "V2" incluye múltiples características avanzadas orientadas a la experiencia del usuario y las mejores prácticas de desarrollo:

* **Conversión en Tiempo Real:** Soporte para más de 150 monedas mundiales con tasas exactas de hasta 6 decimales.
* **Interfaz CLI Interactiva:** Sistema de menús ininterrumpidos tipo "Máquina de Estados". No necesitas reiniciar el programa para cambiar de opinión.
* **Atajos de Teclado (Comandos Globales):** Navega rápidamente escribiendo `l` (lista), `h` (historial), o `salir` en cualquier momento de la ejecución.
* **Paginación y Motor de Búsqueda:** ¿No quieres leer 160 monedas? La lista está paginada (15 ítems por vista) y cuenta con un comando `b` (buscar) para filtrar por nombre o código (ej. "Peso", "Euro").
* **Historial Persistente y Paginado:** Cada conversión se guarda automáticamente con su marca de tiempo (`java.time`) en un archivo `historial.json` local. El historial completo se puede visualizar paginado de a 10 registros para no saturar la consola. ¡No pierdas tus registros al cerrar la app!
* **Caché de Sesión Inteligente:** La lista de monedas se descarga de la API una sola vez y se guarda en memoria, ahorrando ancho de banda y cuota de peticiones.
* **Seguridad y Manejo de Errores:** * La API Key está protegida mediante Variables de Entorno.
    * Las entradas del usuario están sanitizadas contra letras o caracteres extraños en campos numéricos.
    * Adaptación automática de comas a puntos para evitar errores de sintaxis en los decimales.

## 🛠️ Tecnologías Utilizadas

* **Java 21 (LTS) / 25:** Lenguaje principal. Usando la clase nativa `HttpClient` para las peticiones web.
* **Gson (Google):** Para la serialización y deserialización de archivos JSON (Respuestas de la API y guardado del historial local).
* **IntelliJ IDEA & WSL:** Entorno de desarrollo aislado.

## ⚙️ Instalación y Configuración

### Prerrequisitos
1. Tener Java instalado en tu sistema (JDK 17 o superior).
2. Obtener una API Key gratuita en [ExchangeRate-API](https://www.exchangerate-api.com/).

### Pasos
1. Clona este repositorio ejecutando en tu terminal:
   `git clone https://github.com/Neo236/Conversor-de-Moneda-Challenge-ONE.git`

2. Asegúrate de que la biblioteca `Gson` (v2.10.1 o superior) esté agregada a tu proyecto (mediante Maven o descargando el archivo `.jar` en tu carpeta `lib`).

3. **¡Importante! Configura la Variable de Entorno:**
   Para proteger tus credenciales, el código no contiene la API Key en texto plano. Debes crear una variable de entorno en tu sistema o en la configuración de ejecución de tu IDE llamada `EXCHANGE_RATE_API_KEY` con tu clave personal.
   *Ejemplo en Linux/WSL:* `export EXCHANGE_RATE_API_KEY="tu_clave_aqui"`

4. Compila y ejecuta la clase `Main.java`.

## 🎮 Cómo usarlo

El programa te guiará paso a paso, pero siempre tendrás estos comandos a tu disposición:
* `l` o `lista`: Abre el submenú de la base de datos de monedas.
* `h` o `historial`: Abre tu historial completo de conversiones.
* `salir`: Termina el programa de forma segura.

**Dentro del submenú de Lista / Historial:**
* `s` o `siguiente`: Avanza a la página siguiente.
* `a` o `anterior`: Retrocede a la página anterior.
* `b` o `buscar`: Filtra la base de datos por término (Solo disponible en Lista).
* `v` o `volver`: Cierra el submenú y regresa exactamente a la operación donde estabas.