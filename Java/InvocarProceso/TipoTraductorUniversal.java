// package com.sistema.motor.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TipoTraductorUniversal {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Object convertType(String tipoCadena, Object valor) {
        if (valor == null) return null;
        try {
            Class<?> claseDestino = resolverClasePorNombreCorto(tipoCadena);
            return mapper.convertValue(valor, claseDestino);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al generar tipo-valor para [" + tipoCadena + "] con: " + valor, e);
        }
    }

    private static Class<?> resolverClasePorNombreCorto(String tipo) throws ClassNotFoundException {
        return switch (tipo.toLowerCase()) {
            case "int", "integer" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "bigdecimal" -> java.math.BigDecimal.class;
            case "datetime" -> java.time.LocalDateTime.class;
            case "date" -> java.time.LocalDate.class;
            case "boolean" -> Boolean.class;
            case "string" -> String.class;
            case "map", "diccionario" -> java.util.Map.class;
            case "list", "array" -> java.util.List.class;
            default -> Class.forName(tipo); // Busca paths completos de sub-POJOs precompilados
        };
    }
}

