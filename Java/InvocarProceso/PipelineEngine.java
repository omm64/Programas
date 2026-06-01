// package com.sistema.motor.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.motor.dto.ResponseContext;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PipelineEngine {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ExecutionEngine executionEngine;

    public PipelineEngine(ExecutionEngine executionEngine) {
        this.executionEngine = executionEngine;
    }

    public ResponseContext ejecutarPipeline(String jsonPipeline) {
        Map<String, ResponseContext> historialPasos = new HashMap<>();
        ResponseContext ultimaRespuesta = null;

        try {
            Map<String, Object> pipeline = mapper.readValue(jsonPipeline, Map.class);
            List<Map<String, Object>> secuencia = (List<Map<String, Object>>) pipeline.get("secuencia");

            for (Map<String, Object> datosPaso : secuencia) {
                String numeroPaso = datosPaso.get("paso").toString();

                // RESOLVER ENCADENAMIENTO DE TOKENS DINÁMICOS ($PASO_1.data.access_token)
                if (datosPaso.containsKey("token_auth")) {
                    String tokenDeclarado = (String) datosPaso.get("token_auth");
                    if (tokenDeclarado.startsWith("$")) {
                        String tokenReal = resolverVariableEnRuntime(tokenDeclarado, historialPasos);
                        datosPaso.put("token_auth", "Bearer " + tokenReal);
                    }
                }

                // EJECUTAR PASO INDIVIDUAL
                ultimaRespuesta = executionEngine.ejecutarPasoIndividual(datosPaso);

                // CONTROL DE ERRORES: Si un eslabón falla, detiene la cadena y arrastra el error
                if (ultimaRespuesta.msg().containsKey("error")) {
                    return ultimaRespuesta;
                }

                historialPasos.put("PASO_" + numeroPaso, ultimaRespuesta);
            }

            return ultimaRespuesta;

        } catch (Exception e) {
            return new ResponseContext(
                Map.of("pipeline", "error_secuencia"),
                Map.of("error", "Fallo crítico en la orquestación: " + e.getMessage()),
                null
            );
        }
    }

    private static String resolverVariableEnRuntime(String declaracion, Map<String, ResponseContext> historial) throws Exception {
        String[] partes = declaracion.replace("$", "").split("\\.data\\.");
        String llavePaso = partes[0];  // PASO_1
        String campoData = partes[1];  // token_acceso

        ResponseContext respuestaPasoAnterior = historial.get(llavePaso);
        JsonNode nodoData = mapper.valueToTree(respuestaPasoAnterior.data());
        
        if (nodoData.has(campoData)) {
            return nodoData.get(campoData).asText();
        }
        throw new IllegalArgumentException("No se encontró la variable especificada: " + declaracion);
    }
}

