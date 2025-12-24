# PROGRESS.md - Progreso del Proyecto ZX Spectrum IDE

## ✅ COMPLETADO

### Fase 0: Configuración del Proyecto
- [x] Proyecto creado en IntelliJ IDEA 2025
- [x] Maven configurado
- [x] Dependencias añadidas (JavaFX, RichTextFX, ControlsFX, Gson)
- [x] Estructura de paquetes creada
- [x] AppConfig.java y ConfigManager.java (Persistencia)

### Fase 0.5: UI y Experiencia de Usuario (Refactorización)
- [x] **UI Principal**: Implementado `SplitPane` (Consola redimensionable independiente).
- [x] **Barra de Estado**: Fijada en la parte inferior (Docked bottom).
- [x] **Splash Screen**: Carga asíncrona en hilo separado con barra de progreso real.
- [x] **Corrección de Bugs Críticos**:
    - Solucionado `ClassCastException` al cambiar tema.
    - Solucionado problema de ventana inicial minúscula.
    - Solucionado bloqueo de UI durante la carga.

### Fase 1: Temas y Configuración
- [x] ThemeManager implementado (Cambio dinámico sin reinicio).
- [x] Temas CSS (Light / Deep Ocean).
- [x] Internacionalización (I18nManager) y Español.
- [x] Ventana de Configuración (`SettingsDialog`) básica.

## 🔄 EN PROGRESO

### Fase 2: Editor y Herramientas
- [x] Editor básico con pestañas (TabPane).
- [x] Abrir/Guardar archivos.
- [ ] Integración completa compilador PASMO.
- [x] Integración emulador ZEsarUX.

### Fase 2.5: Depurador Visual (feature/zesarux-advanced)
- [ ] Arquitectura de conexión ZRCP (`ZesaruxBridge`)
- [ ] Diseño de UI del Debugger (SplitPanes y Tablas)
- [ ] Implementación de comandos de control (Step, Run, Break)

## 📝 PENDIENTE

- Fase 3: Editor de sprites
- Fase 4: Editor de mapas
- Fase 5: Editor de música
- Fase 6: Pulido final

## 💾 CONFIGURACIÓN GUARDADA
La configuración se guarda en: `~/.zxide/config.json`

## 🔗 INFORMACIÓN GENERAL

**Dominio del proyecto**: `com.lazyzxsoftware.zxspectrumide`  
**Versión actual**: 0.0.5
**JDK**: 17  
**JavaFX**: 21  
**Build System**: Maven