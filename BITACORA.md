# Bitácora de desarrollo · Horos+

Registro de lo que se hizo y de los errores encontrados, como base para el reporte final.

## Sesión 1 · 25 de noviembre de 2026 · Funcionalidad

### Punto de partida

El proyecto tenía la estructura de carpetas de la guía, pero casi todos los archivos estaban vacíos (solo la línea `package`). Los únicos con código eran las plantillas que genera Android Studio (Greeting en el teléfono y una lista de botones de ejemplo en el reloj).

### Errores encontrados en el proyecto y cómo se corrigieron

| # | Dónde | Problema | Corrección |
|---|-------|----------|------------|
| 1 | `wear/src/main/AndroidManifest.xml` | La Activity se llamaba `.presentation.Horos`, pero la guía pide `MainActivity`. Al renombrar la clase la app no habría arrancado (ClassNotFoundException). | Manifest apunta a `.presentation.MainActivity` y la clase se llama `MainActivity`. |
| 2 | `mobile/src/main/res/font/` | La fuente se llamaba `inte_regular.ttf` (faltaba la "r"). `R.font.inter_regular` no existía. | Renombrada a `inter_regular.ttf`. |
| 3 | `wear/build.gradle.kts` | `play-services-wearable` estaba declarada dos veces. | Se quitó la línea repetida. |
| 4 | `wear/build.gradle.kts` | No tenía `lifecycle-runtime-ktx` declarada explícitamente, que es la que da `lifecycleScope`. | Se agregó `libs.androidx.lifecycle.runtime.ktx`. |
| 5 | `mobile/.../data/Expense.kt` | `data class Expense()` sin parámetros no compila (una data class necesita al menos uno). | Implementada con sus 4 campos. |
| 6 | `mobile/.../ui/theme/Theme.kt` | La plantilla usaba colores dinámicos (Android 12+), que reemplazan los colores del Figma por los del fondo de pantalla. | Tema fijo con la paleta de Horos y sin color dinámico. |
| 7 | `wear/.../theme/Theme.kt` | El tema se llamaba `HorosTheme`, igual que el del teléfono; la guía pide `HorosWearTheme`. | Renombrado. |

Verificado sin cambios: el `applicationId` ya era `com.horos.app` en los dos módulos, y `settings.gradle.kts` ya incluía `:mobile` y `:wear`.

### Lo que se programó

**Módulo mobile**

- `WearableConstants.kt`: los 6 paths y claves de la guía.
- `data/Expense.kt`: data class con `amount`, `category`, `note`, `time`, más `toJson()` y `fromJson()`.
- `data/BudgetStorage.kt`: SharedPreferences (`horos_prefs`) con límite (por defecto $500), gastado de hoy, lista de gastos en JSON y reinicio automático al cambiar de día. Los `Double` se guardan como bits en un `Long` porque SharedPreferences no tiene `putDouble`.
- `MainActivity.kt`: estado en Compose, navegación entre Inicio y Registrar gasto, `syncBudgetToWatch()` (función 1, DataClient), `onMessageReceived()` (función 2, MessageClient), `saveExpense()` y `refreshState()`.
- `ui/screens/HomeScreen.kt`: encabezado, tarjeta "Te quedan hoy" con barra y etiqueta de estado, tarjetas Día/Semana/Mes, chips por categoría, botones, lista "Gastos de hoy" y barra inferior. Incluye `formatMoney()`.
- `ui/screens/RegisterExpenseScreen.kt`: monto con −/+ (paso $5) y atajos, 6 categorías en 2 columnas, método de pago y fecha (visuales), concepto con contador 0/50 y "Guardar gasto" desactivado si falta monto o categoría.
- `ui/theme/`: colores, tipografía Inter y `HorosTheme`.

**Módulo wear**

- `WearableConstants.kt`: copia exacta del de mobile.
- `presentation/MainActivity.kt`: `onDataChanged()` (función 1), `loadLastBudget()` en `onCreate` y `onResume`, `sendNewLimit()` (función 2) con Toast cuando no hay nodos.
- `presentation/GlanceScreen.kt`: anillo dibujado con Canvas, DISPONIBLE, monto, MXN, gastado hoy, etiqueta EN META / CERCA DEL LÍMITE / EXCEDIDO y el mensaje "Abre Horos+ en tu teléfono" si aún no hay datos. Tocar el anillo abre Ajustar límite.
- `presentation/AdjustLimitScreen.kt`: slider propio ($100–$1,000, paso $50) con toque y arrastre, Confirmar, `BackHandler` para volver con el gesto de deslizar y soporte opcional de la corona.
- `presentation/theme/Theme.kt`: colores del reloj, Inter con ExtraBold y `HorosWearTheme`.

### Decisiones

- Los íconos se dejaron para la fase de diseño. La guía pide `material-icons-extended`, pero esa librería ya no se actualiza y no está claro que el BOM 2026.02.01 le asigne versión; por ahora se usan letras o texto (por ejemplo "←" y la inicial de la categoría).
- Semana y Mes muestran "—" porque en esta entrega son solo visuales.
- El saludo dice "Hola, Alexis" igual que el Figma; está fijo en el código.
- El anillo del reloj se dejó en 60 % del ancho en vez de 55 % para que "$1,000.00" a 20 sp quepa dentro.
- El teléfono también responde al botón "atrás" en Registrar gasto (regresa a Inicio).

### Limitaciones conocidas

- No se pudo compilar desde aquí: el entorno no tiene acceso a los repositorios de Google/Maven. La primera compilación hay que hacerla en Android Studio.
- El listener de mensajes del teléfono vive en la Activity (como pide la guía). Si la app del teléfono está cerrada o en segundo plano, el ajuste de límite desde el reloj se pierde. Para el demo, la app del teléfono debe estar abierta.
- El gesto "deslizar para regresar" llega como `BackHandler` en Wear OS 6 (API 36) o superior. En imágenes de emulador más antiguas, el gesto cierra la Activity completa.
- Las dos apps deben firmarse con la misma llave para comunicarse. En debug ya ocurre si se instalan desde la misma computadora.

### Comentarios en el código (requisito de la actividad)

La actividad pide código bien comentado, sobre todo en las APIs de Wearable. Se agregaron comentarios en:

- Las dos `MainActivity`: para qué sirve cada listener y por qué se registra en `onResume` y se quita en `onPause`, qué hace `PutDataMapRequest`, `setUrgent()` y `KEY_UPDATED_AT`, por qué se lee el último DataItem al abrir, por qué se libera el `DataItemBuffer`, en qué se diferencian un DataItem y un mensaje, qué son los nodos y cómo viaja el payload.
- Corrutinas: por qué se usa `lifecycleScope.launch` con `await()` (no bloquea el hilo principal y se cancela con la Activity) y por qué el Toast y el estado se actualizan en el hilo principal.
- `WearableConstants` (en los dos módulos), `BudgetStorage`, `Expense` y una línea de descripción para cada pantalla.

### Error en pruebas: "Sin conexión con el teléfono" al confirmar el límite

- **Síntoma:** al tocar Confirmar en Ajustar límite, el reloj muestra "Sin conexión con el teléfono".
- **Problema en el código:** ese mismo Toast salía en dos casos distintos: cuando no había nodos conectados y cuando fallaba cualquier envío. Además, si fallaba el envío a un nodo se cancelaban los demás. Con eso no se podía saber la causa.
- **Corrección:** `sendNewLimit()` ahora separa los casos. "Sin conexión con el teléfono" sale solo si no hay nodos, "No se pudo enviar el límite" si hay nodos pero falló el envío, y "Error de Wearable: …" si falla la consulta de nodos. Cada nodo se intenta por separado y todo queda en Logcat con la etiqueta `HorosWear`.
- **Segunda prueba:** sigue saliendo "Sin conexión con el teléfono", y además un gasto de $30 registrado en el teléfono no aparece en el reloj. Ninguna de las dos direcciones funciona.
- **Diagnóstico:** como "Sin conexión" ahora solo sale cuando `connectedNodes` viene vacío, el reloj no ve ningún teléfono en el Data Layer. Que la función 1 tampoco llegue lo confirma. Es un problema de emparejamiento entre emuladores, no del código (es la fila "Nada llega entre emuladores" de la tabla de errores de la guía).
- **Causa y solución:** los emuladores no estaban bien emparejados. Después de revisar y rehacer el emparejamiento, las dos funciones funcionan en las dos direcciones. No hizo falta cambiar el código.

### Video

Se preparó el guion del video en inglés (máximo 3 minutos): explicación del código de las dos `MainActivity` y demo de las dos funciones con los valores de la guía ($80 en Comida: $500 → $420; límite a $600: el reloj pasa a $520).

### Pendiente

- Compilar en Android Studio y registrar aquí cualquier error.
- Probar el guion del video: $80 en Comida (reloj $500 → $420) y límite $600 desde el reloj (teléfono "de $600.00", reloj $520).
- Fase de diseño contra el Figma: íconos, logo en `res/drawable`, medidas exactas y color de los botones de Registrar gasto.
