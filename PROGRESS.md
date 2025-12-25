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

### Fase 3: Integración de Emulador (Arquitectura Híbrida WebView)
- [x] **Core JSSpeccy**: Integración de emulador basado en JS mediante `WebView`.
- [x] **Puente Java-JS**: Comunicación bidireccional para carga de ROMs y control.
- [x] **Auto-Carga Inteligente**:
    - Inyección de binarios `.tap` compilados en tiempo real.
    - Script `autoloaders.js` para ejecución automática (`LOAD ""`).
- [x] **Estabilidad y Rendimiento**:
    - Sistema de "Cola de Espera" para evitar condiciones de carrera al arrancar.
    - Reutilización de instancia del emulador (evita fugas de memoria y zombies).
    - Auto-resume al mostrar la ventana (eliminación del estado "Pause").

### Fase 3.5: Depurador Visual (Enlace Emulador)
- [x] **Monitorización**: Polling optimizado de registros CPU.
- [x] **Control**: Botones de ejecución conectados (Resume, Pause, Step).
- [x] **Visualización**: Ventana de registros (AF, BC, DE, HL...) y Shadow Registers actualizados en tiempo real.

## 🔄 EN PROGRESO

### Fase 4: Herramientas de Depuración Avanzadas
- [ ] **Desensamblador en Tiempo Real**: Mostrar código ASM ejecutado en la ventana de debug.
- [ ] **Visor de Memoria**: Hex View con capacidad de edición (POKE).
- [ ] **Breakpoints**: Sistema visual para detener la ejecución en líneas concretas.

## 📝 PENDIENTE

- Fase 5: Editor de sprites (Gráficos y Atributos)
- Fase 6: Editor de mapas (Tiles)
- Fase 7: Editor de música (Integración Player)
- Fase 8: Pulido final y empaquetado

## 💾 CONFIGURACIÓN GUARDADA
La configuración se guarda en: `~/.zxide/config.json`

## 🔗 INFORMACIÓN GENERAL

**Dominio del proyecto**: `com.lazyzxsoftware.zxspectrumide`  
**Versión actual**: 0.0.7-alpha
**JDK**: 17  
**JavaFX**: 21  
**Build System**: Maven