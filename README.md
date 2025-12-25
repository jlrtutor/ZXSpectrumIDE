# 📝 ZX Spectrum IDE

<div align="center">

![Version](https://img.shields.io/badge/version-0.0.7--alpha-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-green)
![License](https://img.shields.io/badge/license-MIT-brightgreen)
![Status](https://img.shields.io/badge/status-active-brightgreen)

**IDE moderno y completo para desarrollo de juegos y aplicaciones ZX Spectrum en Z80 Assembly**

[Características](#Características) • [Instalación](#Instalación) • [Uso](#Uso) • [Documentación](#Documentación)

</div>

---

## Descripción

**ZX Spectrum IDE** es un entorno de desarrollo integrado que combina un potente editor de código Z80 con un **emulador integrado**, permitiendo un ciclo de desarrollo ultrarrápido: **Escribe, Pulsa F5 y Juega**.

### Novedades Versión 0.0.7
- **Emulador Integrado**: Ya no necesitas herramientas externas. El IDE incluye un emulador basado en JSSpeccy (WebView) totalmente conectado.
- **Compilar y Ejecutar (F5)**: Compilación automática con PASMO e inyección instantánea en el emulador.
- **Depuración en Tiempo Real**: Visualización de registros de CPU mientras juegas.

---

## Características Actuales

### 🛠️ Editor y Compilador
- **Resaltado de sintaxis** para Z80 Assembly.
- **Compilación Transparente**: Integración con **PASMO** para generar archivos `.tap`.
- **Gestión de Build**: Generación automática de binarios en carpeta local.

### 🕹️ Emulación y Depuración
- **Arquitectura Híbrida**: Emulador ligero integrado en la propia ventana del IDE.
- **Auto-Carga**: El emulador escribe `LOAD ""` automáticamente al compilar.
- **Control Total**: Pausar, continuar y ejecución paso a paso (Step).
- **Inspector de CPU**: Visualización en tiempo real de registros (AF, BC, DE, HL...) y registros sombra.

### 🎨 Interfaz
- **Temas**: Soporte para temas Claro y Oscuro (Deep Ocean).
- **Diseño Moderno**: Paneles redimensionables y pestañas.

---

## Estado del Proyecto

> **⚠️ En Desarrollo Activo** - Versión Alpha 0.0.7

### Completado
- [x] Editor básico funcional.
- [x] Integración completa Compilador -> Emulador (Ciclo F5).
- [x] Arquitectura de emulación robusta (sin fugas de memoria).
- [x] Depuración básica (Monitor de registros).

### En Progreso
- [ ] Desensamblador visual (Ver código máquina en tiempo real).
- [ ] Editor de Memoria (Hex View).
- [ ] Autocompletado inteligente de código.

---

## Uso Rápido

### Compilar y Ejecutar

1. Abre o crea un archivo `.asm`.
2. Escribe tu código.
3. Pulsa **F5** (o Menú Herramientas -> Compilar y Ejecutar).
4. El IDE compilará el código, abrirá el emulador y cargará el juego automáticamente.

---

## Tecnologías

- **Java 17** / **JavaFX 21**
- **RichTextFX** (Editor)
- **WebView + JSSpeccy** (Emulación)
- **PASMO** (Compilador Z80)

---

## Autor

**Lazy ZX Software**
- GitHub: [@jlrtutor](https://github.com/jlrtutor)