# WebWrapFindMyMobile (Android)

Pequeña app Android (Kotlin) que envuelve `https://samsungfind.samsung.com/` en un WebView con:
- JavaScript, DOM Storage, cookies de terceros activados
- Subida de archivos (file chooser)
- Geolocalización, cámara y micrófono bajo permisos de runtime
- Pull-to-refresh y zoom con gestos

## Cómo compilar
1. Abre este proyecto con **Android Studio Hedgehog o posterior**.
2. Deja que Gradle sincronice (se descargará el SDK si hace falta).
3. `Build > Make Project` y luego `Run` para instalar en el teléfono.
4. Para APK: `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

## Notas
- Puedes cambiar la URL en `MainActivity.kt` (`startUrl`).
- Si necesitas modo “Escritorio”, añade un switch que cambie el `userAgentString` para quitar `Mobile`.
- Si la página abre ventanas externas (tel:, mailto:, intent://), se lanzan como Intent fuera del WebView.
