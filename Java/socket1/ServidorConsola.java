import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ServidorConsola {

    public static void main(String[] args) {
        int puerto = 8088;
        try {
            // Corrección de la errata: 'pu puerto' cambiado a 'puerto'
            HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);

            // ASIGNACIÓN DE LOS 3 ENDPOINTS REALES
            servidor.createContext("/", new HandlerRaiz());
            servidor.createContext("/api/datos", new HandlerApi());
            servidor.createContext("/api/procesar", new HandlerProcesar());

            servidor.setExecutor(null);

            System.out.println("==================================================");
            System.out.println(" Servidor HTTP Embebido Iniciado Correctamente");
            System.out.println(" Abre en tu navegador: http://localhost:" + puerto);
            System.out.println("==================================================\n");

            servidor.start();

        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    // === ENDPOINT 1: PÁGINA WEB PRINCIPAL ===
    static class HandlerRaiz implements HttpHandler {
        @Override
        public void handle(HttpExchange intercambio) throws IOException {
            String html = obtenerHtmlCliente();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            intercambio.sendResponseHeaders(200, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
                salida.flush();
            }
        }
    }

    // === ENDPOINT 2: API DE DATOS ESTÁTICOS ===
    static class HandlerApi implements HttpHandler {
        @Override
        public void handle(HttpExchange intercambio) throws IOException {
            System.out.println("[API /api/datos] Respuesta enviada.");
            String json = "{\"mensaje\": \"Hola desde el endpoint de Java Puro!\", \"estado\": \"Conectado con Exito\", \"servidor\": \"HttpServer Nativo\"}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            intercambio.sendResponseHeaders(200, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
                salida.flush();
            }
        }
    }

    // === ENDPOINT 3: PROCESA TEXTO EN TIEMPO REAL (BLOQUE 1) ===
    static class HandlerProcesar implements HttpHandler {
        @Override
        public void handle(HttpExchange intercambio) throws IOException {
            // Leer el texto enviado por el método POST desde el navegador
            InputStreamReader isr = new InputStreamReader(intercambio.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String valorRecibido = br.readLine();

            // Decodificar el formato URL (Ejemplo: "PRUEBA+TR" se convierte en "PRUEBA TR")
            String textoLimpio = "";
            if (valorRecibido != null && valorRecibido.contains("=")) {
                textoLimpio = URLDecoder.decode(valorRecibido.split("=")[1], StandardCharsets.UTF_8);
            }

            // Mostrar en la consola de comandos de Java
            System.out.println("[CONSOLA JAVA] El usuario escribi&oacute;: " + textoLimpio);

            // Responder al navegador
            String jsonRespuesta = "{\"procesado\": \"Java recibi&oacute; tu mensaje: <b>" + textoLimpio.toUpperCase()
                    + "</b>\"}";
            byte[] bytes = jsonRespuesta.getBytes(StandardCharsets.UTF_8);

            intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            intercambio.sendResponseHeaders(200, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
                salida.flush();
            }
        }
    }

    private static String obtenerHtmlCliente() {
        return "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head>"
                + "  <meta charset='UTF-8'>"
                + "  <title>Panel de Control Socket</title>"
                + "  <style>body{font-family:sans-serif; text-align:center; padding-top:30px; background:#f0f2f5;}"
                + "  .bloque{background:white; padding:20px; display:inline-block; border-radius:8px; width:400px; margin:10px; box-shadow:0 2px 4px rgba(0,0,0,0.1); text-align:left; vertical-align:top; height:200px;}"
                + "  input, button{padding:10px; width:95%; margin-top:10px; border-radius:4px; border:1px solid #ccc; font-size:14px; box-sizing:border-box;}"
                + "  button{background:#0066cc; color:white; border:none; cursor:pointer; font-weight:bold;}"
                + "  button:hover{background:#0053a6;}"
                + "  .resultado{margin-top:15px; padding:8px; background:#f8f9fa; border-left:4px solid #0066cc; font-size:14px; min-height:40px; word-wrap:break-word;}</style>"
                + "</head>"
                + "<body>"
                + "  <h1>Consola de Control de Endpoints</h1>"
                + "  "
                + "  <div class='bloque'>"
                + "    <h3>1. Enviar Mensaje a Consola Java</h3>"
                + "    <input type='text' id='wsMsg' placeholder='Escribe algo...'>"
                + "    <button onclick='procesarTextoLocal()'>Enviar a Java</button>"
                + "    <div class='resultado' id='wsLog'>Esperando acci&oacute;n...</div>"
                + "  </div>"
                + "  "
                + "  <div class='bloque'>"
                + "    <h3>2. Consultar Endpoint API Fijo</h3>"
                + "    <button style='margin-top:44px;' onclick='consultarAPI()'>Llamar a /api/datos</button>"
                + "    <div class='resultado' id='apiLog' style='color:blue; font-family:monospace;'>Esperando consulta...</div>"
                + "  </div>"
                + "  "
                + "  <script>"
                + "    "
                + "    function procesarTextoLocal() {"
                + "      const txt = document.getElementById('wsMsg').value;"
                + "      if(txt.trim() === '') return;"
                + "      "
                + "      fetch('/api/procesar', {"
                + "        method: 'POST',"
                + "        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },"
                + "        body: 'texto=' + encodeURIComponent(txt)"
                + "      })"
                + "      .then(res => res.json())"
                + "      .then(json => {"
                + "        document.getElementById('wsLog').innerHTML = json.procesado;"
                + "      });"
                + "    }"
                + "    "
                + "    // CORREGIDO: Eliminada línea huérfana que rompía el JavaScript"
                + "    function consultarAPI() {"
                + "      fetch('/api/datos')"
                + "        .then(response => response.json())"
                + "        .then(objetoJson => {"
                + "           document.getElementById('apiLog').innerHTML = "
                + "             '<b>Mensaje:</b> ' + objetoJson.mensaje + '<br>' +"
                + "             '<b>Estado:</b> ' + objetoJson.estado + '<br>' +"
                + "             '<b>Motor:</b> ' + objetoJson.servidor;"
                + "        });"
                + "    }"
                + "  </script>"
                + "</body>"
                + "</html>";
    }
}
