# 📝 ZX Spectrum IDE

<div align="center">

![Version](https://img.shields.io/badge/version-0.0.6--alpha-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-green)
![License](https://img.shields.io/badge/license-MIT-brightgreen)
![Status](https://img.shields.io/badge/status-in%20development-yellow)

**IDE moderno y completo para desarrollo de juegos y aplicaciones ZX Spectrum en Z80 Assembly**

[Características](#Características) • [Instalación](#Instalación) • [Uso](#Uso) • [Documentación](#Documentación) • [Contribuir](#Contribuir)

</div>

---

## Descripción

**ZX Spectrum IDE** es un entorno de desarrollo integrado diseñado específicamente para crear software retro para el legendario ordenador ZX Spectrum. Combina un potente editor de código Z80 Assembly con herramientas visuales para sprites, mapas y música chiptune, todo en una interfaz moderna y eficiente.

### Objetivo del Proyecto

Proporcionar a los desarrolladores retro y entusiastas del ZX Spectrum un IDE moderno con características avanzadas similares a IDEs contemporáneos, pero optimizado para el desarrollo en ensamblador Z80 y la creación de contenido multimedia para ZX Spectrum.

---

## Características

### Editor de Código Z80 Assembly

- **Resaltado de sintaxis** avanzado para Z80 Assembly
- **Autocompletado** inteligente de instrucciones y etiquetas
- **Code folding** (colapsado de código)
- **Etiquetas fantasma** (code lens) con conteo de referencias
- **Numeración de líneas** y guías de indentación
- **Análisis en tiempo real** de errores sintácticos
- **Navegación rápida** (ir a definición, buscar referencias)
- **Snippets** predefinidos y personalizables

### Herramientas Integradas

| Herramienta           | Descripción |
|-----------------------|-------------|
| **Depurador Visual** | Conexión con ZEsarUX, visualización de registros, stack y desensamblado con resaltado de ejecución. |
| **Editor de Sprites** | Creación y edición de gráficos con soporte completo de atributos ZX Spectrum |
| **Editor de Mapas** | Diseño de niveles con sistema de capas y propiedades de tiles |
| **Editor de Música** | Compositor chiptune con emulación AY-3-8912 |
| **Compilador PASMO** | Integración completa con ensamblador |

### 🎨 Temas y Personalización

- **Tema Claro**: Inspirado en IntelliJ IDEA Default
- **Tema Oscuro**: Material Deep Ocean
- **Esquemas de color** personalizables para sintaxis
- **Multiidioma**: Español e Inglés (extensible)
- **Configuración completa**: Espacios, tabulaciones, fuentes, rutas de herramientas

---

## Estado del Proyecto

> **⚠️ En Desarrollo Activo** - Versión Alpha 0.0.6

### Completado

- [x] Estructura base del proyecto y persistencia (JSON).
- [x] Gestión de temas (ThemeManager) con cambio en tiempo real.
- [x] Sistema de internacionalización (i18n).
- [x] **Interfaz Refactorizada**: Paneles redimensionables y barras ancladas.
- [x] **Splash Screen**: Carga asíncrona optimizada.
- [x] **Depuración**: Conexión robusta ZRCP, control de flujo (Paso/Run/Pause), y visualización de estado de CPU en tiempo real.

### En Progreso

- [x] Resaltado de sintaxis Z80.
- [x] Pestañas múltiples para archivos.
- [x] Consola de salida integrada.
- [ ] Autocompletado inteligente.
- [x] Compilación directa con PASMO.
- [ ] Integración de emulador nativo (JSpeccy) para eliminar dependencias externas.

### Planificado

Ver [`TODO.md`](TODO.md) para la lista completa de características planificadas.

Ver [`PROGRESS.md`](PROGRESS.md) para el estado detallado del desarrollo.

---

## Tecnologías

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **Java** | 17+ | Lenguaje principal |
| **JavaFX** | 21 | Framework de interfaz gráfica |
| **Maven** | 3.x | Gestión de dependencias |
| **RichTextFX** | 0.11.2 | Editor de código avanzado |
| **ControlsFX** | 11.2.1 | Controles UI adicionales |
| **Gson** | 2.10.1 | Serialización JSON |

### Herramientas Externas

- **[PASMO](http://pasmo.speccy.org/)** - Ensamblador Z80 multiplataforma
- **[ZEsarUX](https://github.com/chernandezba/zesarux)** - Emulador de ZX Spectrum con capacidades de depuración
---

## Instalación

### Requisitos Previos

- **JDK 17 o superior** ([descargar](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.x** ([descargar](https://maven.apache.org/download.cgi))
- **PASMO** (opcional, para compilar código)
- **ZEsarUX** (opcional, para ejecutar programas)

### Clonar el Repositorio

```bash
git clone [https://github.com/jlrtutor/ZXSpectrumIDE.git](https://github.com/jlrtutor/ZXSpectrumIDE.git)
cd ZXSpectrumIDE
```

### Compilar el Proyecto

```bash
mvn clean install
```

### Ejecutar el IDE

```bash
mvn javafx:run
```

O desde IntelliJ IDEA:
1. Abrir el proyecto
2. Ejecutar la clase `Main.java`

---

## Uso

### Configuración Inicial

1. **Primera ejecución**: El IDE creará automáticamente el archivo de configuración en `~/.zxide/config.json`

2. **Configurar PASMO** (opcional):
    - Menú → Configuración → Preferencias
    - Pestaña "Compilador"
    - Especificar ruta del ejecutable de PASMO

3. **Configurar ZEsarUX** (opcional):
    - Menú → Configuración → Preferencias
    - Pestaña "Emulador"
    - Especificar ruta del ejecutable de ZEsarUX

### Crear un Proyecto

1. Menú → Archivo → Nuevo Proyecto
2. Seleccionar plantilla (Juego / Demo / Utilidad)
3. Especificar nombre y ubicación
4. El IDE creará la estructura de carpetas automáticamente

### Escribir Código Z80

```asm
; Ejemplo: Hola Mundo en ZX Spectrum
        ORG 32768

inicio:
        LD A, 2         ; Canal de pantalla
        CALL 5633       ; ROM: Abrir canal
        
        LD HL, mensaje
bucle:
        LD A, (HL)
        OR A
        RET Z
        RST 16          ; Imprimir carácter
        INC HL
        JR bucle

mensaje:
        DEFM "HOLA MUNDO"
        DEFB 13, 0

        END inicio
```

### Compilar y Ejecutar

- **Compilar**: `F5` o Menú → Herramientas → Compilar
- **Ejecutar**: `F6` o Menú → Herramientas → Ejecutar

---

## Capturas de Pantalla

> *Próximamente - El IDE está en desarrollo*

---

## Roadmap

### Fase 0: Fundamentos (En progreso)
- [x] Configuración del proyecto
- [x] Sistema de configuración
- [x] Sistema de temas
- [ ] Sistema de internacionalización

### Fase 1: Editor de Código
- [x] Editor básico con RichTextFX
- [x] Resaltado de sintaxis Z80
- [ ] Autocompletado
- [ ] Navegación de código

### Fase 2: Compilación y Ejecución
- [x] Integración con PASMO
- [x] Integración con ZEsarUX
- [x] Sistema de depuración básico
- [ ] Sistema de depuración avanzado

### Fase 3-5: Herramientas Visuales
- [ ] Editor de sprites
- [ ] Editor de mapas
- [ ] Editor de música chiptune

### Fase 6: Pulido Final
- [ ] Optimización
- [ ] Testing
- [ ] Documentación completa
- [ ] Versión 1.0 estable

---

## Contribuir

¡Las contribuciones son bienvenidas! Este proyecto está en desarrollo activo.

### Áreas donde Ayudar

- **Reportar bugs**
- **Sugerir características**
- **Mejorar documentación**
- **Añadir traducciones**
- **Diseñar temas**

---

## Documentación

- [TODO.md](TODO.md) - Lista completa de características planificadas
- [PROGRESS.md](PROGRESS.md) - Estado actual del desarrollo
- **Wiki** (próximamente) - Guías de uso y desarrollo
- **JavaDoc** (próximamente) - Documentación del API

### Recursos sobre ZX Spectrum

- [World of Spectrum](https://worldofspectrum.org/)
- [Z80 Instruction Set](http://z80.info/z80syntx.htm)
- [ZX Spectrum Technical Information](https://worldofspectrum.org/faq/reference/48kreference.htm)

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Ver el archivo [LICENSE](LICENSE) para más detalles.

```
MIT License

Copyright (c) 2025 Lazy ZX Software

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Autor

**Lazy ZX Software**
- GitHub: [@jlrtutor](https://github.com/jlrtutor)

---

## Agradecimientos

- **Sinclair Research** por crear el ZX Spectrum
- **Julián Albo** por PASMO
- **César Hernández** por ZEsarUX
- La comunidad de desarrolladores retro del ZX Spectrum

---

## 💬 Contacto y Soporte

- **Issues**: [GitHub Issues](https://github.com/jlrtutor/ZXSpectrumIDE/issues)
- **Discusiones**: [GitHub Discussions](https://github.com/jlrtutor/ZXSpectrumIDE/discussions)

---

<div align="center">

**⭐ Si te gusta este proyecto, dale una estrella en GitHub ⭐**

Made with ❤️ for the ZX Spectrum community

![ZX Spectrum](https://img.shields.io/badge/ZX%20Spectrum-1982-red?style=for-the-badge)

</div>