# PROGRESS.md - Progreso del Proyecto ZX Spectrum IDE

## ✅ COMPLETADO

### Fase 0: Configuración del Proyecto
- [x] Proyecto creado en IntelliJ IDEA 2025
- [x] Maven configurado
- [x] Dependencias añadidas (JavaFX, RichTextFX, ControlsFX, Gson)
- [x] Estructura de paquetes creada
- [x] AppConfig.java y ConfigManager.java (Persistencia)

### Fase 0.5: UI y Experiencia de Usuario
- [x] **UI Principal**: Implementado `SplitPane` y gestión de paneles.
- [x] **Barra de Estado** y **Splash Screen** asíncrono.
- [x] **Temas**: ThemeManager (Light / Deep Ocean) y soporte i18n.
- [x] **Corrección de Bugs**: Solucionado parpadeo, carga de iconos y bloqueos de UI.

### Fase 1: Editor y Gestión de Archivos
- [x] Editor con pestañas (TabPane) y RichTextFX.
- [x] Abrir/Guardar archivos `.asm`.
- [x] **Corrección Bug**: Solucionada pérdida de resaltado de sintaxis al recargar archivos.

### Fase 2: Compilación (PASMO)
- [x] **Integración PASMO**: Compilación automática con generación de `.tap`.
- [x] **Gestión de Build**: Generación de artefactos en carpeta `build` relativa al fuente.
- [x] **Compatibilidad**: Generación de nombres de archivo MSDOS (8.3) para soporte nativo del Spectrum.
- [x] **Corrección Bug**: Solucionado error con argumento `.publics` y gestión de flujos de error.

### Fase 3: Motor de Emulación Nativo (Reemplazo de WebView)
- [x] **Core Z80 Java Puro**:
    - Implementación completa de opcodes (incluyendo indocumentados y I/O).
    - Refactorización a `step()` unificado.
- [x] **Sistema de Video (ULA)**:
    - **Scanline Rendering**: Renderizado línea a línea para efectos "raster" precisos.
    - Buffer de video ampliado (320x240) para soporte real de borde.
    - Sincronización precisa a **50Hz** (Nano-timing).
- [x] **Carga Inteligente (Smart Loader)**:
    - **Tape Traps**: Intercepción de rutina ROM `0x0556` (LD-BYTES).
    - **Robot Mecanógrafo**: Auto-tecleo de `LOAD ""` tras reinicio.
    - Carga instantánea conservando mensajes nativos de ROM ("Bytes: ...").
- [x] **Gestión de Memoria**:
    - Inicialización correcta de atributos (Pantalla blanca al reset).
    - Soporte de lectura/escritura completa (RAM/ROM).

### Fase 3.5: Depurador Visual
- [x] **Monitorización**: Polling optimizado de registros CPU.
- [x] **Control**: Botones de ejecución conectados (Resume, Pause, Step, Step Over).
- [x] **Visualización**: Ventana de registros (AF, BC, DE, HL...) y Shadow Registers.
- [x] **Desensamblador en Tiempo Real**: Scroll infinito y seguimiento de PC.
- [x] **Visor de Memoria**: Hex View completo (64KB).

## 📝 PENDIENTE

- [ ] **Breakpoints**: Sistema visual para detener la ejecución (Backend implementado, falta UI).
- Fase 5: Editor de sprites (Gráficos y Atributos)
- Fase 6: Editor de mapas (Tiles)
- Fase 7: Editor de música (Integración Player)
- Fase 8: Pulido final y empaquetado

## 💾 CONFIGURACIÓN GUARDADA
La configuración se guarda en: `~/.zxide/config.json`

## 🔗 INFORMACIÓN GENERAL

**Dominio del proyecto**: `com.lazyzxsoftware.zxspectrumide`  
**Versión actual**: 0.0.8-alpha
**JDK**: 17  
**JavaFX**: 21  
**Build System**: Maven