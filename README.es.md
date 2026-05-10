# We Rise DFW

> **Traducciones:** [English](README.md) · [Español](README.es.md) · [العربية](README.ar.md) · [中文](README.zh.md)

Una aplicación Android de código abierto para el área de Dallas / Fort Worth que muestra proveedores cercanos de **comida**, **ropa** y **alojamiento** según la ubicación actual del usuario. Creada por **We Rise DFW** (Shane W. Watson) y ofrecida gratuitamente bajo la licencia MIT — ver [LICENSE](LICENSE).

## Por qué
Muchas personas en DFW — incluidas las que enfrentan dificultades con vivienda, alimentación, recuperación o desplazamiento repentino — no tienen tiempo, ancho de banda ni un teléfono estable para revisar directorios de proveedores. We Rise DFW está pensada como una forma rápida y con poca fricción de ver qué hay cerca de ellas en este momento.

> **¿Buscas cómo usar la aplicación?** Consulta [USER_GUIDE.es.md](USER_GUIDE.es.md) — escrito para usuarios finales, sin necesidad de conocimientos técnicos. Este README está dirigido a desarrolladores.

## Privacidad ante todo
- La ubicación se lee **únicamente** cuando el usuario toca **Buscar** o **Buscar más en línea**.
- **Nunca se guarda en disco**, nunca se registra, nunca se comparte con ningún servicio de analítica.
- La base de datos local Room solo guarda la lista en caché de proveedores (para que la aplicación siga funcionando sin conexión). La ubicación del usuario no forma parte de esa caché.
- Las traducciones se ejecutan en el dispositivo mediante Google ML Kit — el texto nunca sale del teléfono para ser traducido.
- Las únicas llamadas de red salientes son: mosaicos de mapa de OpenStreetMap, una búsqueda explícita "Buscar más en línea" contra la API pública Overpass, y las descargas únicas de modelos de idioma de ML Kit.

## Características
- Tres pestañas: **Comida**, **Ropa**, **Alojamiento**
- Radio de búsqueda seleccionable por el usuario (1 a 25 millas)
- Proveedores religiosos marcados con un ícono de cruz, con un ajuste para incluirlos o excluirlos
- Estado abierto / cerrado: punto verde cuando está abierto ahora, rojo cuando está cerrado
- Tocar una dirección → abre tu aplicación de mapas predeterminada
- Tocar un número de teléfono → abre tu marcador predeterminado
- Tocar un sitio web → abre tu navegador predeterminado
- Mapa (mitad superior) con pines rojos y verdes coincidentes con la pestaña actual
- Esquema de colores negro y rojo
- Limitado al área metropolitana de DFW; fuera de ese rectángulo el mapa muestra un aviso de "fuera de rango"
- Botón **Buscar más en línea** que obtiene proveedores adicionales de OpenStreetMap y los fusiona con la caché local
- **Selector de idioma** con ~59 idiomas, traducción en el dispositivo mediante Google ML Kit (predeterminado: inglés)

## Stack tecnológico
- Kotlin · AndroidX · vistas XML clásicas (intencional — mantiene el binario pequeño y el código accesible)
- Min SDK 24 (Android 7.0) · Target SDK 34
- [OSMDroid](https://github.com/osmdroid/osmdroid) — renderizado del mapa, sin clave API
- [Room](https://developer.android.com/training/data-storage/room) — base de datos de caché local
- `LocationManager` de la plataforma — ubicación de un solo uso, sin dependencia de Google Play Services en este flujo
- [ML Kit Translation](https://developers.google.com/ml-kit/language/translation) — traducción en el dispositivo
- [API de Overpass](https://overpass-api.de/) — endpoint gratuito de consulta a OpenStreetMap, usado por el botón de búsqueda en línea

## Compilar y ejecutar
1. Abre la carpeta `WeRiseApp` en Android Studio (`File → Open`).
2. Cuando Android Studio te pregunte por el wrapper de Gradle, deja que use la versión declarada en `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.4). Si se queja porque falta `gradle-wrapper.jar`, ejecuta **File → Sync Project with Gradle Files**, o en una terminal en la raíz del proyecto ejecuta `gradle wrapper` una vez.
3. Deja que Gradle sincronice. La primera sincronización descarga las dependencias; tarda unos minutos.
4. Ejecuta en un dispositivo o emulador (API 24+).

## Agregar más proveedores a la lista incluida
Edita `app/src/main/java/com/werisetech/weriseapp/data/SeedData.kt`. Cada entrada toma un `id` estable, `name`, `category`, dirección, teléfono, horario, lat/lon, indicador religioso, descripción breve y sitio web. El formato de horario está documentado en `util/HoursParser.kt`.

## Traducción
La pantalla de Configuración tiene un selector de idioma con los ~59 idiomas que Google ML Kit admite en el dispositivo. Predeterminado: inglés (sin traducción).

Cuando el usuario elige un idioma distinto al inglés por primera vez, ML Kit descarga el modelo de idioma correspondiente (una sola vez, ~10–30 MB). Después de eso, la traducción se ejecuta completamente en el dispositivo. Las cadenas traducidas se almacenan en caché en la base de datos Room local, indexadas por `(idioma, texto-fuente)`, de modo que cada frase pasa por ML Kit una sola vez.

Si prefieres usar otro traductor (por ejemplo, para idiomas fuera del conjunto de ML Kit), implementa la interfaz `Translator` en `i18n/Translator.kt` e intercambia el argumento del constructor en `TranslatorFactory.get()`.

## Búsqueda en línea
El botón **Buscar más en línea** lanza una consulta contra la [API de Overpass](https://overpass-api.de/) de OpenStreetMap para nodos etiquetados con `social_facility=food_bank | soup_kitchen | clothing_bank | shelter` (y unas pocas etiquetas de servicios relacionadas) dentro de un rectángulo derivado de la ubicación del usuario y el radio seleccionado. Los resultados se mapean al mismo esquema `Service` y se fusionan con la caché local, así que aparecen en la búsqueda regular y en el mapa junto con la lista incluida.

La API de Overpass es un recurso gratuito de la comunidad. La consulta envía un rectángulo derivado de la ubicación instantánea del usuario a `overpass-api.de`. La ubicación misma no se persiste en el dispositivo.

## Documentación
- [USER_GUIDE.es.md](USER_GUIDE.es.md) — guía para el usuario final (pantalla principal, flujo de búsqueda, configuración, privacidad, resolución de problemas).
- [README.es.md](README.es.md) — este archivo. Para desarrolladores.
- [LICENSE](LICENSE) — texto de la licencia MIT.

## Licencia
Publicado bajo la [licencia MIT](LICENSE). Copyright © 2026 We Rise Technologies.

Eres libre de usar, copiar, modificar, fusionar, publicar, distribuir, sublicenciar y vender copias del software, siempre que mantengas intactos el aviso de copyright y la licencia.

## Estado
Este es un proyecto personal de código abierto que se ofrece **tal cual**, sin garantía y sin promesa de soporte, actualizaciones o correcciones de errores. Se aceptan contribuciones (pull requests), pero no se garantiza su incorporación.

## Reconocimientos
- Los proveedores de DFW en `SeedData.kt` son organizaciones reales que realizan un trabajo crítico. Por favor, considera apoyarlos directamente.
- Datos de mapa © contribuyentes de [OpenStreetMap](https://www.openstreetmap.org/copyright).
- Modelos de traducción © Google, distribuidos bajo los términos de ML Kit.
