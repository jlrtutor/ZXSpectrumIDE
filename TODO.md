# TODO.md - ZX Spectrum Z80 IDE

## Proyecto: IDE Completo para Desarrollo de Videojuegos en ZX Spectrum

---

## 1. EDITOR DE CÓDIGO Z80 ASSEMBLY

### 1.1 Editor de Texto Base
- [x] Implementar área de texto con RichTextFX
- [x] Sistema de pestañas para múltiples archivos
- [x] Guardar/Abrir archivos .asm
- [ ] Soporte para codificación de texto (UTF-8)
- [ ] Deshacer/Rehacer ilimitado
- [ ] Portapapeles (Copiar/Cortar/Pegar)
- [ ] Selección múltiple y edición en columnas
- [ ] Buscar y reemplazar (con expresiones regulares)
- [ ] Ir a línea específica

### 1.2 Resaltado de Sintaxis
- [ ] Tokenizador para Z80 Assembly
- [ ] Resaltar instrucciones (LD, ADD, JP, CALL, etc.)
- [ ] Resaltar registros (A, B, C, D, E, H, L, IX, IY, etc.)
- [ ] Resaltar números (hexadecimal, decimal, binario)
- [ ] Resaltar etiquetas y símbolos
- [ ] Resaltar comentarios (; y multilínea)
- [ ] Resaltar directivas del ensamblador
- [ ] Resaltar strings
- [ ] Temas de color personalizables (claro/oscuro)

### 1.3 Numeración y Visualización
- [ ] Columna de números de línea
- [ ] Resaltar línea actual
- [ ] Mostrar espacios y tabulaciones (opcional)
- [ ] Guías de indentación verticales
- [ ] Marcadores de cambios (modificado/guardado)
- [ ] Minimap del código (vista previa lateral)
- [ ] Ruler/regla de columnas (80 caracteres)

### 1.4 Indentación y Formato
- [ ] Auto-indentación inteligente
- [ ] Indentación al presionar Tab
- [ ] Des-indentación con Shift+Tab
- [ ] Formateo automático de código
- [ ] Alineación de comentarios
- [ ] Configuración de tamaño de tabulación

### 1.5 Code Folding (Colapsado de Código)
- [ ] Detectar bloques colapsables (procedimientos, macros)
- [ ] Iconos de [+]/[-] para colapsar/expandir
- [ ] Colapsar/expandir todo
- [ ] Mantener estado de colapsado al guardar
- [ ] Indicador visual de código colapsado

### 1.6 Etiquetas Fantasma (Code Lens)
- [ ] Mostrar número de referencias de cada etiqueta
- [ ] Click para navegar a las referencias
- [ ] Mostrar tamaño en bytes de procedimientos
- [ ] Mostrar ciclos de reloj estimados (T-states)
- [ ] Información de uso de registros

### 1.7 Autocompletado e IntelliSense
- [ ] Autocompletado de instrucciones Z80
- [ ] Autocompletado de registros
- [ ] Autocompletado de etiquetas definidas
- [ ] Autocompletado de símbolos del proyecto
- [ ] Snippets predefinidos (bucles, llamadas, etc.)
- [ ] Documentación inline de instrucciones
- [ ] Firma de parámetros para macros

### 1.8 Navegación de Código
- [ ] Ir a definición de etiqueta (Ctrl+Click)
- [ ] Buscar todas las referencias
- [ ] Breadcrumbs (ruta de navegación)
- [ ] Lista de símbolos/etiquetas del archivo
- [ ] Vista de estructura del proyecto
- [ ] Marcadores/bookmarks personalizados
- [ ] Navegación entre errores

### 1.9 Análisis en Tiempo Real
- [ ] Detección de errores sintácticos
- [ ] Advertencias (registros no inicializados, saltos imposibles)
- [ ] Subrayado de errores en rojo
- [ ] Panel de problemas/errores
- [ ] Sugerencias de optimización
- [ ] Validación de rangos de direcciones

### 1.10 Integración con Ensamblador (PASMO)
- [ ] Compilar archivo actual (F5)
- [ ] Compilar proyecto completo
- [ ] Panel de salida de compilación
- [ ] Parsear errores de PASMO
- [ ] Click en error para ir a la línea
- [ ] Generación de archivo .tap/.tzx
- [ ] Generación de listado con direcciones
- [ ] Símbolos y mapa de memoria

### 1.11 Integración Avanzada ZEsarUX (Debugger Visual)
- [x] **Protocolo ZRCP (Backend)**
    - [x] Cliente TCP/Socket para conectar a localhost:10000
    - [x] Parser de respuestas ZRCP (texto a objetos Java)
    - [x] Sistema de eventos reactivos (reemplazando polling)
- [x] **Panel de Registros (Registers View)**
    - [x] Grid con valores Hex (AF, BC, DE, HL, IX, IY, SP, PC)
    - [x] Visualización de Flags (SZ5H3PNC)
    - [ ] Edición de valores de registro "al vuelo"
- [x] **Panel de Desensamblado (Disassembly View)**
    - [x] Tabla/Area de texto con desensamblado
    - [x] Sincronización visual con el PC actual (Highlight azul)
    - [ ] Toggle Breakpoints con click en margen (Pendiente integración JSpeccy)
- [x] **Panel de Memoria y Stack**
    - [ ] Vista de volcado de memoria completa (Hex Editor)
    - [x] Vista básica de Stack (Pila)
- [x] **Controles de Depuración**
    - [x] Botones: Step, Run, Pause
    - [x] Gestión de estado (Activar/Desactivar botones según contexto)
    - [x] Atajos de teclado (F5, F8, F10)

### 1.12 Gestión de Proyectos
- [ ] Crear nuevo proyecto
- [ ] Abrir proyecto existente
- [ ] Estructura de carpetas (src, build, assets)
- [ ] Archivos de configuración (.z80project)
- [ ] Múltiples archivos .asm en proyecto
- [ ] Orden de compilación/ensamblado
- [ ] Variables de proyecto (ORG, defines)
- [ ] Plantillas de proyecto (juego, demo, utilidad)

### 1.13 Configuración y Personalización
- [ ] Preferencias del editor (fuente, tamaño)
- [ ] Atajos de teclado personalizables
- [ ] Temas de color
- [ ] Configuración de compilador
- [ ] Configuración de emulador
- [ ] Exportar/importar configuración

---

## 2. A 9. (SECCIONES DE EDITORES SPRITES, MAPAS, ETC... SIN CAMBIOS)

---

## 10. Bugs
- [x] Parpadeo/Blanco en Splash Screen (Solucionado con Thread separado).
- [x] Crash al cambiar tema (ClassCastException Solucionado).
- [x] UI mal dimensionada al inicio (Solucionado con tamaño explícito).
- [x] Icono de la aplicación no visible en Dock de macOS (Solucionado con AWT Taskbar).
- [x] Congelación de UI al desconectar debugger (Solucionado con "Fire & Forget").
- [x] Spam de logs en consola al pausar (Solucionado con arquitectura reactiva).
- [ ] BUG - Parpadeo en macOS al iniciar (Baja prioridad).

---

**Versión**: 0.0.6  
**Última actualización**: Diciembre 2025