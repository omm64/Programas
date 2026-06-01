// package com.sistema.motor.core;

import com.sistema.motor.dto.ResponseContext;
import com.sistema.motor.reflection.UniversalInvoker;
import com.sistema.motor.util.TipoTraductorUniversal;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
public class ExecutionEngine {

    private final ApplicationContext springContext;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ExecutionEngine(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    public ResponseContext ejecutarPasoIndividual(Map<String, Object> datosPaso) {
        String tipoProceso = datosPaso.getOrDefault("tipo_proceso", "METODO").toString();
        Map<String, String> typeMetadata = Map.of(tipoProceso.toLowerCase(), datosPaso.getOrDefault("url_path", datosPaso.getOrDefault("metodo_nombre", "n/a")).toString());

        try {
            Object resultadoProceso;

            if ("RESTFUL".equalsIgnoreCase(tipoProceso) || "API".equalsIgnoreCase(tipoProceso)) {
                // LLAMADA WEB SERVICE CON HEADERS FORZADOS A JSON
                String url = (String) datosPaso.get("url_path");
                String token = (String) datosPaso.get("token_auth");
                
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String body = mapper.writeValueAsString(datosPaso.get("parametros_duplas"));

                var requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body));

                if (token != null) requestBuilder.header("Authorization", token);

                HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                resultadoProceso = mapper.readValue(response.body(), Object.class);

            } else {
                // LLAMADA LOCAL CON REFLEXIÓN Y ABSORCIÓN DE SPRING
                String clasePath = (String) datosPaso.get("clase_path");
                String metodoNombre = (String) datosPaso.get("metodo_nombre");

                Class<?> claseDestino = Class.forName(clasePath);
                Object instanciaConRecursos = springContext.getBean(claseDestino);

                List<Map<String, Object>> duplas = (List<Map<String, Object>>) datosPaso.get("parametros_duplas");
                Object[] parametrosFinales = new Object[duplas.size()];

                for (int i = 0; i < duplas.size(); i++) {
                    String tipo = (String) duplas.get(i).get("tipo");
                    Object valor = duplas.get(i).get("valor");
                    parametrosFinales[i] = TipoTraductorUniversal.convertType(tipo, valor);
                }

                resultadoProceso = UniversalInvoker.ejecutar(instanciaConRecursos, metodoNombre, parametrosFinales);
            }

            return new ResponseContext(typeMetadata, Map.of("exito", "true"), resultadoProceso);

        } catch (java.lang.reflect.InvocationTargetException e) {
            // Arrastra el error real lanzado por el proceso del programador
            Throwable errorReal = e.getTargetException();
            return new ResponseContext(typeMetadata, Map.of("error", errorReal.getMessage() != null ? errorReal.getMessage() : "Error de negocio"), 
                Map.of("clase_error", errorReal.getClass().getName(), "causa", "Término anticipado por error en el proceso"));
        } catch (Exception e) {
            // Arrastra errores de infraestructura (JSON mal formado, clase no encontrada, API caída)
            return new ResponseContext(typeMetadata, Map.of("error", e.getMessage() != null ? e.getMessage() : "Error de infraestructura"), 
                Map.of("clase_error", e.getClass().getName(), "causa", "Fallo en la carga o preparación del motor"));
        }
    }
}
