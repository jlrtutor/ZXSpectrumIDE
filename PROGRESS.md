# PROGRESS.md - Progreso del Proyecto ZX Spectrum IDE

## ✅ COMPLETADO

### Fase 0: Configuración del Proyecto
- [x] Proyecto creado en IntelliJ IDEA 2025
- [x] Maven configurado
- [x] Dependencias añadidas (JavaFX, RichTextFX, ControlsFX, Gson)
- [x] Estructura de paquetes creada:
    - config, theme, i18n, editor, sprites, maps, music, compiler, emulator, utils
- [x] Carpetas de recursos creadas:
    - themes/, i18n/, config/, icons/
- [x] Clase Main.java creada con menú básico
- [x] Archivo Launcher.java eliminado
- [x] AppConfig.java creada (configuración completa)
- [x] ConfigManager.java creada (carga/guarda JSON)

## 🔄 EN PROGRESO

### Fase 0: Sistema de Temas y Configuración
- [x] Crear ThemeManager.java
- [x] Crear archivos CSS para tema claro (light.css)
- [x] Crear archivos CSS para tema oscuro (deep-ocean.css)
- [x] Crear I18nManager.java
- [x] Crear archivo messages_es.properties
- [x] Integrar ThemeManager en Main.java
- [ ] **SIGUIENTE**: Crear ventana de Configuración/Preferencias

## 📝 PENDIENTE

- Fase 1: Editor de código con resaltado de sintaxis
- Fase 2: Integración con PASMO y ZEsarUX
- Fase 3: Editor de sprites
- Fase 4: Editor de mapas
- Fase 5: Editor de música
- Fase 6: Pulido final

## 💾 CONFIGURACIÓN GUARDADA

La configuración se guarda en: `~/.zxide/config.json`

## 🔗 INFORMACIÓN PARA RETOMAR

**Dominio del proyecto**: `com.lazyzxsoftware.zxspectrumide`  
**Versión actual**: 0.0.2
**JDK**: 17  
**JavaFX**: 21  
**Build System**: Maven