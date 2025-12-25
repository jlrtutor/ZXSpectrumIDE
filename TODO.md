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
- [ ] Buscar y reemplazar

### 1.10 Integración con Ensamblador (PASMO)
- [x] **Compilar y Ejecutar (F5)**
- [x] Panel de salida de compilación (Consola integrada)
- [x] Parsear errores de PASMO (Salida básica)
- [x] Generación de archivo `.tap` con cargador BASIC (`--tapbas`)
- [x] Gestión de nombres de archivo formato 8.3 (MSDOS)

### 1.11 Depurador y Emulación (Nueva Arquitectura WebView)
- [x] **Integración JSSpeccy**
    - [x] Carga de ROMs 48k
    - [x] Inyección de TAPs desde Java (Base64)
    - [x] Auto-arranque (`autoloaders.js`)
- [x] **Control de Ejecución**
    - [x] Step (Paso a paso)
    - [x] Resume / Pause
    - [x] Reset
- [x] **Visualización de Estado**
    - [x] Registros principales (AF, BC, DE, HL, PC, SP)
    - [x] Registros alternativos (Shadow Registers)
    - [x] Flags (F)
- [ ] **Desensamblador (Disassembly View)**
    - [ ] Decodificar bytes en memoria a mnemónicos Z80
    - [ ] Sincronización visual con el PC actual (Flecha/Resaltado)
- [ ] **Visor de Memoria**
    - [ ] Volcado Hexadecimal
    - [ ] Edición de memoria

### 1.12 Gestión de Proyectos
- [x] Estructura de compilación relativa (`/build` junto al archivo fuente)
- [ ] Crear nuevo proyecto (Wizard)
- [ ] Árbol de archivos del proyecto

---

## 2. A 9. (SECCIONES DE EDITORES SIN CAMBIOS)

---

## 10. Bugs Solucionados (Recientes)
- [x] **Critical**: Corrección de "Race Condition" al cargar cintas (implementada cola de espera).
- [x] **Performance**: Solucionado consumo excesivo de CPU/Memoria al reiniciar emulación (reutilización de instancia JS).
- [x] **UI**: Solucionado estado de "Pausa" no deseado al reabrir la ventana del emulador.
- [x] **Editor**: Solucionado bug que eliminaba el coloreado de sintaxis al recargar un archivo.

---

**Versión**: 0.0.7  
**Última actualización**: Diciembre 2025