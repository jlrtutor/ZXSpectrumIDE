# TODO.md - ZX Spectrum Z80 IDE

## Proyecto: IDE Completo para Desarrollo de Videojuegos en ZX Spectrum

---

## 1. EDITOR DE CÓDIGO Z80 ASSEMBLY

### 1.1 Editor de Texto Base
- [ ] Implementar área de texto con RichTextFX
- [ ] Sistema de pestañas para múltiples archivos
- [ ] Guardar/Abrir archivos .asm
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

### 1.11 Integración con Emulador (ZEsarUX)
- [ ] Ejecutar en emulador (F6)
- [ ] Configurar ruta de ZEsarUX
- [ ] Pasar parámetros al emulador
- [ ] Debug: breakpoints
- [ ] Debug: step over/into/out
- [ ] Debug: inspección de registros
- [ ] Debug: inspección de memoria
- [ ] Debug: pila de llamadas

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

## 2 EDITOR DE SPRITES ZX

### 2.1 Canvas de Dibujo
- [ ] Grilla de píxeles configurable (8x8, 16x16, 32x32, etc...)
- [ ] Zoom ajustable
- [ ] Herramienta lápiz
- [ ] Herramienta borrador
- [ ] Herramienta línea
- [ ] Herramienta rectángulo
- [ ] Herramienta círculo
- [ ] Herramienta relleno (flood fill)
- [ ] Selector de color/atributo ZX Spectrum
- [ ] Paleta de colores ZX Spectrum (BRIGHT/FLASH)

### 2.2 Gestión de Sprites
- [ ] Crear nuevo sprite
- [ ] Lista de sprites del proyecto
- [ ] Duplicar sprite
- [ ] Eliminar sprite
- [ ] Renombrar sprite
- [ ] Organizar en carpetas/grupos
- [ ] Importar imagen (conversión automática)
- [ ] Exportar sprite como PNG

### 2.3 Transformaciones
- [ ] Voltear horizontal
- [ ] Voltear vertical
- [ ] Rotar 90° (derecha/izquierda)
- [ ] Desplazar píxeles (shift)
- [ ] Invertir colores

### 2.4 Animación
- [ ] Crear secuencias de frames
- [ ] Reproducir animación (preview)
- [ ] Configurar FPS de preview
- [ ] Clonar frame
- [ ] Papel cebolla
- [ ] Insertar frame vacío
- [ ] Eliminar frame
- [ ] Reordenar frames
- [ ] Exportar animación completa

### 2.5 Atributos ZX Spectrum
- [ ] Editor de atributos INK/PAPER
- [ ] BRIGHT y FLASH
- [ ] Vista previa con atributos reales
- [ ] Modo monocromo / color
- [ ] Exportar con/sin atributos

### 2.6 Generación de Código
- [ ] Generar array de bytes en Z80 Assembly
- [ ] Generar DB (Define Byte)
- [ ] Incluir comentarios descriptivos
- [ ] Formato de salida configurable
- [ ] Copiar código al portapapeles
- [ ] Insertar código en editor activo
- [ ] Generar código para animaciones
- [ ] Incluir rutina de dibujado (opcional)

### 2.7 Capas y Overlays
- [ ] Sistema de capas múltiples
- [ ] Mostrar/ocultar capas
- [ ] Opacidad de capa
- [ ] Fusionar capas
- [ ] Fondo transparente

### 2.8 Herramientas Avanzadas
- [ ] Muestreo de color (eyedropper)
- [ ] Selección rectangular
- [ ] Copiar/pegar área seleccionada
- [ ] Historial de deshacer/rehacer
- [ ] Regla y guías
- [ ] Grilla configurable (activar/desactivar)

---

## 3. EDITOR DE MAPAS (TILEMAP)

### 3.1 Canvas de Mapa
- [ ] Grilla de tiles configurable
- [ ] Zoom y paneo (pan)
- [ ] Tamaño de mapa configurable (ancho x alto)
- [ ] Múltiples capas de tiles
- [ ] Vista previa en tiempo real

### 3.2 Paleta de Tiles
- [ ] Importar tiles desde editor de sprites
- [ ] Selector visual de tiles
- [ ] Organizar tiles en categorías
- [ ] Búsqueda de tiles
- [ ] Tiles recientes/favoritos

### 3.3 Herramientas de Edición
- [ ] Herramienta pincel (colocar tile)
- [ ] Herramienta borrador
- [ ] Herramienta relleno
- [ ] Herramienta rectángulo (relleno de área)
- [ ] Herramienta selección
- [ ] Copiar/pegar área de mapa
- [ ] Deshacer/rehacer

### 3.4 Capas
- [ ] Crear múltiples capas
- [ ] Renombrar capas
- [ ] Mostrar/ocultar capas
- [ ] Bloquear capas
- [ ] Reordenar capas
- [ ] Opacidad de capa
- [ ] Capa de colisiones/propiedades

### 3.5 Propiedades de Tiles
- [ ] Definir propiedades personalizadas (walkable, solid, etc.)
- [ ] Asignar valores a tiles
- [ ] Editor de propiedades visual
- [ ] Overlay de visualización de propiedades

### 3.6 Generación de Código
- [ ] Exportar mapa como array 2D
- [ ] Formato de bytes comprimido
- [ ] Incluir información de dimensiones
- [ ] Exportar capas por separado
- [ ] Exportar propiedades de tiles
- [ ] Incluir rutinas de renderizado (opcional)
- [ ] Optimización RLE (Run-Length Encoding)

### 3.7 Gestión de Mapas
- [ ] Crear nuevo mapa
- [ ] Guardar/cargar mapa
- [ ] Múltiples mapas por proyecto
- [ ] Redimensionar mapa
- [ ] Configuración de scroll (wrap around)

### 3.8 Herramientas Avanzadas
- [ ] Auto-tiling (tiles conectados automáticamente)
- [ ] Generación procedural de terreno
- [ ] Importar/exportar TMX (Tiled Map Editor)
- [ ] Pathfinding visual para testing
- [ ] Rejilla hexagonal/isométrica (opcional)

---

## 4. EDITOR DE MÚSICA CHIPTUNE

### 4.1 Interfaz de Tracker
- [ ] Vista de patrones estilo tracker
- [ ] Múltiples canales (3 canales AY-3-8912)
- [ ] Scroll vertical de patrones
- [ ] Numeración de filas (steps)
- [ ] Indicador de posición de reproducción

### 4.2 Edición de Notas
- [ ] Entrada de notas por teclado QWERTY
- [ ] Entrada MIDI (opcional)
- [ ] Piano roll visual
- [ ] Selector de octava
- [ ] Duración de nota configurable
- [ ] Copiar/pegar patrones
- [ ] Deshacer/rehacer
- [ ] Transponer selección

### 4.3 Gestión de Instrumentos
- [ ] Editor de instrumentos/envolventes
- [ ] Volumen (0-15)
- [ ] Tono/frecuencia
- [ ] Ruido (noise)
- [ ] Envelope hardware del AY
- [ ] Biblioteca de instrumentos predefinidos
- [ ] Guardar/cargar instrumentos

### 4.4 Efectos de Sonido
- [ ] Vibrato
- [ ] Portamento (slide)
- [ ] Arpeggio
- [ ] Volume slide
- [ ] Retrigger
- [ ] Cut note
- [ ] Delay
- [ ] Tabla de efectos del canal

### 4.5 Gestión de Patrones
- [ ] Crear/eliminar patrón
- [ ] Duplicar patrón
- [ ] Longitud de patrón configurable
- [ ] Secuenciador de patrones (orden de reproducción)
- [ ] Loop de sección
- [ ] Marcadores de tiempo

### 4.6 Reproductor Integrado
- [ ] Play/Pause/Stop
- [ ] Reproducir desde posición actual
- [ ] Reproducir patrón actual
- [ ] Reproducir canción completa
- [ ] Velocidad/tempo ajustable (BPM)
- [ ] Silenciar canales individuales (solo/mute)
- [ ] Medidor de volumen por canal

### 4.7 Emulación de Sonido
- [ ] Motor de emulación AY-3-8912
- [ ] Salida de audio en tiempo real
- [ ] Calidad de audio ajustable
- [ ] Exportar a WAV/MP3
- [ ] Visualización de forma de onda

### 4.8 Generación de Código
- [ ] Exportar como datos Z80 Assembly
- [ ] Formato de player configurable
- [ ] Incluir rutina de reproducción
- [ ] Optimización de datos
- [ ] Incluir efectos de sonido
- [ ] Documentación de uso del código

### 4.9 Importación/Exportación
- [ ] Importar módulos PT3 (Vortex Tracker)
- [ ] Importar AY/YM archivos
- [ ] Exportar a formatos estándar
- [ ] Guardar proyecto nativo (.z80music)

### 4.10 Herramientas Avanzadas
- [ ] Análisis espectral
- [ ] Metrónomo
- [ ] Grabación en tiempo real
- [ ] Quantización de notas
- [ ] Plantillas de canciones (intro, loop, fin)

---

## 5. SISTEMA DE TEMAS (THEMES)

### 5.1 Gestión de Temas
- [ ] Sistema de temas modulares
- [ ] Tema Claro (estilo IntelliJ IDEA Default Light)
    - [ ] Colores de fondo/primer plano
    - [ ] Colores de controles (botones, menús, etc.)
    - [ ] Colores del editor de código
    - [ ] Colores de la barra de herramientas
    - [ ] Colores de la barra de estado
- [ ] Tema Oscuro (estilo Material Deep Ocean)
    - [ ] Paleta de colores Deep Ocean
    - [ ] Contraste optimizado
    - [ ] Colores para sintaxis del editor
    - [ ] Colores de UI oscuros
- [ ] Selector de tema en Configuración
- [ ] Aplicación en tiempo real (sin reiniciar)
- [ ] Persistencia de tema seleccionado

### 5.2 Sistema de Temas para Sintaxis
- [ ] Esquemas de color para resaltado de sintaxis
- [ ] Tema claro para Z80 Assembly
    - [ ] Instrucciones: azul oscuro
    - [ ] Registros: púrpura
    - [ ] Números: verde oscuro
    - [ ] Comentarios: gris
    - [ ] Etiquetas: negro/negrita
    - [ ] Strings: verde oliva
- [ ] Tema oscuro para Z80 Assembly
    - [ ] Instrucciones: azul claro
    - [ ] Registros: rosa/magenta
    - [ ] Números: verde claro
    - [ ] Comentarios: gris claro
    - [ ] Etiquetas: amarillo
    - [ ] Strings: naranja
- [ ] Editor visual de temas de sintaxis
- [ ] Importar/exportar temas
- [ ] Vista previa en vivo de cambios

### 5.3 Aplicación de Temas en Componentes
- [ ] Aplicar tema a editores de código
- [ ] Aplicar tema a editor de sprites
- [ ] Aplicar tema a editor de mapas
- [ ] Aplicar tema a editor de música
- [ ] Aplicar tema a diálogos y ventanas
- [ ] Aplicar tema a tooltips e hints
- [ ] Actualización dinámica de todos los componentes

---

## 6. SISTEMA DE INTERNACIONALIZACIÓN (i18n)

### 6.1 Gestión de Idiomas
- [ ] Sistema de recursos i18n modulares
- [ ] Archivos .properties por idioma
- [ ] Idioma Español (es_ES) - Predeterminado
- [ ] Estructura para añadir más idiomas fácilmente
- [ ] Selector de idioma en Configuración
- [ ] Cambio de idioma en tiempo real
- [ ] Persistencia de idioma seleccionado

### 6.2 Traducción de Componentes
- [ ] Menús principales traducidos
- [ ] Diálogos y mensajes traducidos
- [ ] Tooltips traducidos
- [ ] Mensajes de error traducidos
- [ ] Documentación de ayuda por idioma
- [ ] Atajos de teclado localizados

### 6.3 Gestión de Traducciones
- [ ] Archivo de recursos: messages_es.properties
- [ ] Archivo de recursos: messages_en.properties (futuro)
- [ ] Claves de traducción estandarizadas
- [ ] Fallback a idioma por defecto
- [ ] Herramienta para detectar traducciones faltantes
- [ ] Documentación para contribuir traducciones

---

## 7. CONFIGURACIÓN Y PREFERENCIAS

### 7.1 Ventana de Configuración
- [ ] Diálogo modal de Configuración/Settings
- [ ] Sistema de pestañas/categorías
- [ ] Búsqueda de opciones
- [ ] Botones: Aplicar, Aceptar, Cancelar, Restaurar
- [ ] Vista previa de cambios en vivo (cuando sea posible)

### 7.2 Categoría: Apariencia
- [ ] Selector de tema (Claro/Oscuro)
- [ ] Selector de idioma
- [ ] Tamaño de fuente de la UI
- [ ] Fuente de la interfaz
- [ ] Escala de la interfaz (DPI)
- [ ] Animaciones activadas/desactivadas
- [ ] Opacidad de ventanas (opcional)

### 7.3 Categoría: Editor
- [ ] **Tabulación y espacios**
    - [ ] Espacios por tabulación (2, 4, 8)
    - [ ] Usar espacios en lugar de tabs
    - [ ] Detectar indentación automáticamente
- [ ] **Visualización**
    - [ ] Mostrar números de línea (Sí/No)
    - [ ] Mostrar líneas verticales de tabulación (Sí/No)
    - [ ] Mostrar espacios en blanco (Sí/No)
    - [ ] Mostrar caracteres de fin de línea (Sí/No)
    - [ ] Resaltar línea actual (Sí/No)
    - [ ] Mostrar minimap (Sí/No)
    - [ ] Ancho de línea guía (80, 120, desactivado)
- [ ] **Fuente del editor**
    - [ ] Familia de fuente (monoespaciada)
    - [ ] Tamaño de fuente (10-24px)
    - [ ] Interlineado
    - [ ] Ligaduras tipográficas (Sí/No)
- [ ] **Comportamiento**
    - [ ] Auto-guardado (Sí/No)
    - [ ] Intervalo de auto-guardado
    - [ ] Deshacer ilimitado (Sí/No)
    - [ ] Tamaño de historial de deshacer
    - [ ] Auto-indentación (Sí/No)
    - [ ] Cerrar paréntesis/comillas automáticamente
    - [ ] Autocompletado activado (Sí/No)
    - [ ] Sensibilidad de autocompletado (inmediato/con delay)

### 7.4 Categoría: Colores y Fuentes
- [ ] Editor de esquema de colores de sintaxis
- [ ] Lista de elementos sintácticos
- [ ] Selector de color por elemento
- [ ] Negrita/Cursiva/Subrayado por elemento
- [ ] Vista previa del código con el esquema
- [ ] Guardar esquemas personalizados
- [ ] Importar/exportar esquemas

### 7.5 Categoría: Compilador (PASMO)
- [ ] **Ruta del ejecutable**
    - [ ] Campo de texto con ruta
    - [ ] Botón "Examinar" para buscar
    - [ ] Validación de ruta (verificar que existe)
- [ ] **Opciones de compilación**
    - [ ] Argumentos adicionales
    - [ ] Directorio de salida
    - [ ] Formato de salida (.tap, .tzx, .bin)
    - [ ] Generar archivo de símbolos (Sí/No)
    - [ ] Generar listado (Sí/No)
- [ ] **Comportamiento**
    - [ ] Compilar al guardar (Sí/No)
    - [ ] Limpiar build anterior (Sí/No)
    - [ ] Mostrar tiempo de compilación

### 7.6 Categoría: Emulador (ZEsarUX)
- [ ] **Ruta del ejecutable**
    - [ ] Campo de texto con ruta
    - [ ] Botón "Examinar" para buscar
    - [ ] Validación de ruta
- [ ] **Opciones de ejecución**
    - [ ] Argumentos adicionales
    - [ ] Modelo de Spectrum (48K, 128K, +2, +3)
    - [ ] Velocidad de emulación
    - [ ] Pantalla completa al ejecutar (Sí/No)
- [ ] **Debug**
    - [ ] Activar modo debug (Sí/No)
    - [ ] Puerto de debug remoto
    - [ ] Pausar al iniciar (Sí/No)

### 7.7 Categoría: Atajos de Teclado
- [ ] Lista de acciones con atajos asignados
- [ ] Búsqueda de acciones
- [ ] Editor de atajos
- [ ] Detectar conflictos de atajos
- [ ] Restaurar atajos por defecto
- [ ] Importar/exportar configuración de atajos
- [ ] Esquemas de atajos (Visual Studio, Eclipse, etc.)

### 7.8 Categoría: Proyectos
- [ ] **Estructura por defecto**
    - [ ] Plantilla de carpetas
    - [ ] Nombres de carpetas (src, build, assets)
- [ ] **Comportamiento**
    - [ ] Abrir último proyecto al iniciar (Sí/No)
    - [ ] Recordar archivos abiertos (Sí/No)
    - [ ] Auto-detección de cambios externos
- [ ] **Backups**
    - [ ] Crear copias de seguridad (Sí/No)
    - [ ] Ubicación de backups
    - [ ] Número máximo de backups
    - [ ] Frecuencia de backup

### 7.9 Categoría: Plugins
- [ ] Lista de plugins instalados
- [ ] Activar/desactivar plugins
- [ ] Configuración por plugin
- [ ] Buscar nuevos plugins
- [ ] Instalar plugin desde archivo
- [ ] Actualizar plugins

### 7.10 Persistencia de Configuración
- [ ] Guardar configuración en archivo local
    - [ ] Formato JSON o XML
    - [ ] Ubicación: ~/.zxide/config.json
- [ ] Cargar configuración al inicio
- [ ] Valores por defecto para primera ejecución
- [ ] Validación de configuración
- [ ] Migración entre versiones del IDE
- [ ] Exportar configuración completa
- [ ] Importar configuración

---

## 8. INTEGRACIÓN GENERAL DEL IDE

### 8.1 Interfaz Principal
- [ ] Sistema de ventanas acoplables (docking)
- [ ] Pestañas para cada herramienta
- [ ] Disposición de ventanas personalizable
- [ ] Barra de menús completa
- [ ] Barra de herramientas con iconos
- [ ] Barra de estado (info del proyecto)
- [ ] Panel de salida/consola
- [ ] Panel de búsqueda global

### 8.2 Gestión de Proyectos Unificada
- [ ] Proyecto único con todas las herramientas
- [ ] Explorador de archivos del proyecto
- [ ] Referencias cruzadas entre herramientas
- [ ] Assets manager (sprites, mapas, música)
- [ ] Build system integrado
- [ ] Control de versiones Git (opcional)

### 8.3 Interoperabilidad
- [ ] Insertar sprite desde editor al código
- [ ] Usar sprites en editor de mapas
- [ ] Vincular música a proyectos
- [ ] Actualización automática de referencias
- [ ] Drag & drop entre editores

### 8.4 Ayuda y Documentación
- [ ] Manual de usuario integrado
- [ ] Referencia de instrucciones Z80
- [ ] Tutoriales paso a paso
- [ ] Tips del día
- [ ] Búsqueda en documentación
- [ ] Ejemplos de código

### 8.5 Temas y Personalización Visual
- [ ] Aplicación global del tema seleccionado
- [ ] Transición suave entre temas
- [ ] Personalización de colores individuales
- [ ] Persistencia de tema entre sesiones

---

## 9. CARACTERÍSTICAS ADICIONALES

### 9.1 Rendimiento
- [ ] Carga rápida de proyectos grandes
- [ ] Autoguardado periódico
- [ ] Carga lazy de recursos
- [ ] Caché de compilación
- [ ] Indexación de símbolos

### 9.2 Extensibilidad
- [ ] Sistema de plugins/extensiones
- [ ] API para scripts externos
- [ ] Temas personalizados importables
- [ ] Snippets definidos por usuario

### 9.3 Colaboración
- [ ] Exportar proyecto completo
- [ ] Importar proyectos de otros usuarios
- [ ] Compartir configuraciones
- [ ] Formato de proyecto estándar

---

## NOTAS DE IMPLEMENTACIÓN

### Prioridades de Desarrollo
1. **Fase 0**: Sistema de configuración, temas e i18n
2. **Fase 1**: Editor de código básico + resaltado de sintaxis
3. **Fase 2**: Integración PASMO + ZEsarUX
4. **Fase 3**: Editor de sprites
5. **Fase 4**: Editor de mapas
6. **Fase 5**: Editor de música
7. **Fase 6**: Integración y pulido

### Tecnologías a usar
- **JavaFX**: Interfaz gráfica
- **RichTextFX**: Editor de código avanzado
- **CSS**: Sistema de temas y estilos
- **ResourceBundle**: Internacionalización (i18n)
- **JSON/Properties**: Persistencia de configuración
- **FXGL** (opcional): Para canvas de sprites/mapas
- **JLayer/JavaSound**: Reproducción de audio
- **PASMO**: Ensamblador externo
- **ZEsarUX**: Emulador externo

### Dependencias Externas
- RichTextFX (editor de código)
- ControlsFX (controles adicionales)
- FXGraphics2D (renderizado avanzado)
- Gson/Jackson (parsing JSON para configuración)
- SLF4J + Logback (logging del sistema)

---

## OBJETIVOS DEL PROYECTO

- IDE moderno y completo para desarrollo ZX Spectrum
- Productividad maximizada para desarrolladores retro (en un futuro añadir otras máquinas)
- Herramientas integradas sin salir del IDE
- Curva de aprendizaje amigable
- Open source y extensible

---

**Versión**: 0.0.1  
**Última actualización**: Diciembre 2025