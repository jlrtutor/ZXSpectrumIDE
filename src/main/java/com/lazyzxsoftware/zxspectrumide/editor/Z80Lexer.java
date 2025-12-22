package com.lazyzxsoftware.zxspectrumide.editor;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analizador léxico para Z80 Assembly
 * Identifica tokens y aplica estilos de sintaxis con colores inline
 */
public class Z80Lexer {

    // Instrucciones Z80 (todas en mayúsculas)
    private static final String[] INSTRUCTIONS = {
            // 8-bit load
            "LD", "PUSH", "POP", "EX", "EXX",
            // 8-bit arithmetic
            "ADD", "ADC", "SUB", "SBC", "AND", "OR", "XOR", "CP", "INC", "DEC",
            "DAA", "CPL", "NEG", "CCF", "SCF",
            // Rotate and shift
            "RLCA", "RLA", "RRCA", "RRA", "RLC", "RL", "RRC", "RR", "SLA", "SRA", "SRL", "SLL",
            // Bit manipulation
            "BIT", "SET", "RES",
            // Jump, call, return
            "JP", "JR", "DJNZ", "CALL", "RET", "RETI", "RETN", "RST",
            // Input/output
            "IN", "INI", "INIR", "IND", "INDR", "OUT", "OUTI", "OTIR", "OUTD", "OTDR",
            // CPU control
            "NOP", "HALT", "DI", "EI", "IM",
            // Block transfer
            "LDI", "LDIR", "LDD", "LDDR", "CPI", "CPIR", "CPD", "CPDR",
            // Misc
            "RLD", "RRD"
    };

    // Registros Z80
    private static final String[] REGISTERS = {
            // 8-bit
            "A", "B", "C", "D", "E", "H", "L", "I", "R",
            // 16-bit
            "AF", "BC", "DE", "HL", "IX", "IY", "SP", "PC",
            // Flags
            "NZ", "Z", "NC", "C", "PO", "PE", "P", "M"
    };

    // Directivas del ensamblador
    private static final String[] DIRECTIVES = {
            "ORG", "END", "EQU", "DEFB", "DB", "DEFW", "DW", "DEFM", "DM", "DEFS", "DS",
            "INCLUDE", "INCBIN", "MACRO", "ENDM", "IF", "ENDIF", "ELSE",
            "DEFINE", "UNDEF", "ERROR", "WARNING"
    };

    // Construir patrones regex
    private static final String INSTRUCTION_PATTERN = "\\b(" + String.join("|", INSTRUCTIONS) + ")\\b";
    private static final String REGISTER_PATTERN = "\\b(" + String.join("|", REGISTERS) + ")\\b";
    private static final String DIRECTIVE_PATTERN = "\\b(" + String.join("|", DIRECTIVES) + ")\\b";

    // Otros patrones
    private static final String LABEL_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]*:";
    private static final String NUMBER_PATTERN = "(\\$[0-9A-Fa-f]+|#[0-9A-Fa-f]+|%[01]+|\\b\\d+\\b)";
    private static final String COMMENT_PATTERN = ";[^\n]*";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    // Patrón combinado
    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                    + "|(?<INSTRUCTION>" + INSTRUCTION_PATTERN + ")"
                    + "|(?<REGISTER>" + REGISTER_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<LABEL>" + LABEL_PATTERN + ")",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    /**
     * Analiza el texto y devuelve los estilos a aplicar con colores inline
     */
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = null;

            if (matcher.group("COMMENT") != null) {
                styleClass = "comment";
            } else if (matcher.group("STRING") != null) {
                styleClass = "string";
            } else if (matcher.group("DIRECTIVE") != null) {
                styleClass = "directive";
            } else if (matcher.group("INSTRUCTION") != null) {
                styleClass = "keyword"; // Usamos 'keyword' para instrucciones
            } else if (matcher.group("REGISTER") != null) {
                styleClass = "register";
            } else if (matcher.group("NUMBER") != null) {
                styleClass = "number";
            } else if (matcher.group("LABEL") != null) {
                styleClass = "label";
            }

            // 1. Añadir texto sin estilo antes del match
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            // 2. Añadir el estilo (Clase CSS) si existe
            if (styleClass != null) {
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            } else {
                spansBuilder.add(Collections.emptyList(), matcher.end() - matcher.start());
            }

            lastKwEnd = matcher.end();
        }

        // Añadir el resto del texto sin estilo
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

        return spansBuilder.create();
    }
}