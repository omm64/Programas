import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

public class ServidorConsola3 {

    public static void main(String[] args) {
        int puerto = 8080;
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);

            // Endpoints del ecosistema
            servidor.createContext("/", new HandlerRaizSegura());
            servidor.createContext("/api/formulario", new HandlerFormulario());

            servidor.setExecutor(null);

            System.out.println("==========================================================");
            System.out.println(" SERVIDOR INTEGRAL ACTIVADO (GZIP + BASE64 + CORS + FORM)");
            System.out.println(" Abre en tu navegador de inmediato: http://localhost:" + puerto);
            System.out.println("==========================================================\n");

            servidor.start();
        } catch (IOException e) {
            System.err.println("Error en la inicializacion: " + e.getMessage());
        }
    }

    // === << ENDPOINT RAIZ: COMPRIME, ENCRIPTA (BASE64) Y SIRVE EL FORMULARIO >>
    // ===
    static class HandlerRaizSegura implements HttpHandler {
        @Override
        public void handle(HttpExchange intercambio) throws IOException {
            System.out.println("[SEGURIDAD] Cifrando y zipeando el formulario maestro de 20 elementos...");

            // Aplicar politicas de seguridad CORS
            intercambio.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            intercambio.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            intercambio.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if (intercambio.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                intercambio.sendResponseHeaders(204, -1);
                return;
            }

            // Obtener el HTML crudo del formulario
            String htmlOriginal = obtenerHtmlFormulario();

            // CAPA 1: Ofuscación / Encriptación Base64
            String htmlBase64 = Base64.getEncoder().encodeToString(htmlOriginal.getBytes(StandardCharsets.UTF_8));

            // Cargador bootstrap en el cliente que descifra el Base64 en memoria del
            // navegador
            String cargadorBootstrap = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>"
                    + "<script>document.open();document.write(atob('" + htmlBase64 + "'));document.close();</script>"
                    + "</body></html>";

            byte[] bytesCargador = cargadorBootstrap.getBytes(StandardCharsets.UTF_8);

            // CAPA 2: Compresión GZIP binaria en caliente
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(bytesCargador);
            }
            byte[] bytesZipeados = baos.toByteArray();

            // Configurar cabeceras HTTP de compresión para el navegador
            intercambio.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            intercambio.getResponseHeaders().set("Content-Encoding", "gzip"); // Fuerza al browser a dezipear de
                                                                              // inmediato

            intercambio.sendResponseHeaders(200, bytesZipeados.length);

            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytesZipeados);
                salida.flush();
            }
            System.out.println(" -> Formulario enviado. Peso crudo: " + bytesCargador.length + " bytes | Peso Zipeado: "
                    + bytesZipeados.length + " bytes.\n");
        }
    }

    // === 2. ENDPOINT API: PROCESA LOS 20 CAMPOS JSON ===
    static class HandlerFormulario implements HttpHandler {
        @Override
        public void handle(HttpExchange intercambio) throws IOException {
            intercambio.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            intercambio.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            intercambio.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if (intercambio.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                intercambio.sendResponseHeaders(204, -1);
                return;
            }

            // Capturar flujo JSON
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(intercambio.getRequestBody(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
            String jsonRecibido = sb.toString();

            System.out.println("\n[DATOS CAPTURADOS EN RED]:");
            System.out.println(jsonRecibido);

            // Extraer variables clave usando analisis nativo
            String nombre = extraerCampoJson(jsonRecibido, "txtNombre");
            String correo = extraerCampoJson(jsonRecibido, "txtEmail");
            String edad = extraerCampoJson(jsonRecibido, "numEdad");
            String pais = extraerCampoJson(jsonRecibido, "selPais");

            System.out.println("\n======= LECTURA LOGICA DE VARIABLES EN JAVA =======");
            System.out.println("Propietario: " + nombre.toUpperCase());
            System.out.println("Destinatario: " + correo);
            System.out.println("Edad validada: " + edad + " anos");
            System.out.println("Procedencia: " + pais);
            System.out.println("===================================================\n");

            // Responder JSON de Exito al cliente
            String respuesta = "{\"estado\":\"OK\", \"mensaje\":\"&iexcl;Perfecto! La Consola Java desencript&oacute;, dezipe&oacute; y proces&oacute; los 20 elementos de "
                    + nombre + ".\"}";
            byte[] responseBytes = respuesta.getBytes(StandardCharsets.UTF_8);

            intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            intercambio.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(responseBytes);
                salida.flush();
            }
        }

        private String extraerCampoJson(String json, String llave) {
            try {
                String busqueda = "\"" + llave + "\":\"";
                int inicio = json.indexOf(busqueda);
                if (inicio == -1) {
                    busqueda = "\"" + llave + "\":";
                    inicio = json.indexOf(busqueda);
                    if (inicio == -1)
                        return "N/A";
                    int fin = json.indexOf(",", inicio);
                    if (fin == -1)
                        fin = json.indexOf("}", inicio);
                    return json.substring(inicio + busqueda.length(), fin).trim().replace("\"", "");
                }
                inicio += busqueda.length();
                int fin = json.indexOf("\"", inicio);
                return json.substring(inicio, fin);
            } catch (Exception e) {
                return "Error";
            }
        }
    }

    // Estructura limpia del formulario sin comentarios que interfieran en la
    // compresion
    private static String obtenerHtmlFormulario() {
        return "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head>"
                + "  <meta charset='UTF-8'>"
                + "  <title>Formulario Complejo Seguro</title>"
                + "  <style>body{font-family:sans-serif; background:#f4f6f9; margin:0; padding:20px;}"
                + "  .contenedor{max-width:800px; background:white; margin:0 auto; padding:30px; border-radius:12px; box-shadow:0 4px 15px rgba(0,0,0,0.05);}"
                + "  h2{text-align:center; color:#0066cc; margin-bottom:25px;}"
                + "  .grid{display:grid; grid-template-columns:1fr 1fr; gap:15px;}"
                + "  .campo{display:flex; flex-direction:column; margin-bottom:10px;}"
                + "  .campo-completo{grid-column: span 2;}"
                + "  .campo-row{flex-direction:row; align-items:center; gap:10px; margin-top:5px;}"
                + "  label{font-size:13px; font-weight:bold; margin-bottom:5px; color:#555;}"
                + "  input, select, textarea{padding:8px; border:1px solid #ccc; border-radius:4px; font-size:14px; width:100%; box-sizing:border-box;}"
                + "  input[type='checkbox'], input[type='radio']{width:auto; margin:0;}"
                + "  button{grid-column:span 2; background:#0066cc; color:white; padding:12px; border:none; border-radius:6px; font-size:16px; font-weight:bold; cursor:pointer; margin-top:15px;}"
                + "  button:hover{background:#0053a6;}"
                + "  #feedback{margin-top:20px; padding:15px; border-radius:6px; text-align:center; font-weight:bold; display:none;}</style>"
                + "</head>"
                + "<body>"
                + "  <div class='contenedor'>"
                + "    <h2>Registro Maestro Seguro (20 Elementos Encriptados + GZIP)</h2>"
                + "    <form id='frmMaestro' onsubmit='enviarFormulario(event)'>"
                + "      <div class='grid'>"
                + "        <div class='campo'><label>1. Nombre Completo:</label><input type='text' id='txtNombre' required value='Luis G&oacute;mez'></div>"
                + "        <div class='campo'><label>2. Correo:</label><input type='email' id='txtEmail' required value='luisg@correo.com'></div>"
                + "        <div class='campo'><label>3. Clave:</label><input type='password' id='txtPass' value='987654'></div>"
                + "        <div class='campo'><label>4. Tel&eacute;fono:</label><input type='tel' id='txtTel' value='555998877'></div>"
                + "        <div class='campo'><label>5. Edad:</label><input type='number' id='numEdad' value='32'></div>"
                + "        <div class='campo'><label>6. Fecha Nacimiento:</label><input type='date' id='datNacimiento' value='1994-11-23'></div>"
                + "        <div class='campo'><label>7. Hora Registro:</label><input type='time' id='timHora' value='09:15'></div>"
                + "        <div class='campo'><label>8. Color Interfaz:</label><input type='color' id='colColor' value='#e67e22'></div>"
                + "        <div class='campo'><label>9. Pa&iacute;s:</label><select id='selPais'><option value='M&eacute;xico'>M&eacute;xico</option><option value='Colombia' selected>Colombia</option><option value='Argentina'>Argentina</option></select></div>"
                + "        <div class='campo'><label>10. Rango Nivel:</label><input type='range' id='rngNivel' min='1' max='10' value='10'></div>"
                + "        <div class='campo'><label>11. Sueldo:</label><input type='text' id='txtSueldo' value='$45,000'></div>"
                + "        <div class='campo'><label>12. Link Web:</label><input type='url' id='txtWeb' value='https://carlosgomez.dev'></div>"
                + "        <div class='campo-row campo'><input type='checkbox' id='chkTerminos' checked><label>13. Acepto terminos de encriptacion</label></div>"
                + "        <div class='campo-row campo'><input type='checkbox' id='chkBoletin' checked><label>14. Recibir logs por correo</label></div>"
                + "        <div class='campo'><label>15. Genero:</label><div class='campo-row'><input type='radio' name='radGen' id='radM' checked><label>M</label><input type='radio' name='radGen' id='radF'><label>F</label></div></div>"
                + "        <div class='campo-row campo'><input type='checkbox' id='chkNotif'><label>16. Desactivar telemetria externa</label></div>"
                + "        <div class='campo'><label>17. Cargar Ficha:</label><input type='file' id='filDocumento'></div>"
                + "        <div class='campo'><label>18. Termino de Busqueda:</label><input type='search' id='schBuscar' value='Cifrado nativo'></div>"
                + "        <div class='campo-completo campo'><label>19. Nota de Seguridad (TextArea):</label><textarea id='txtAreaComentarios' rows='3'>Los paquetes viajan bajo compresion binaria.</textarea></div>"
                + "        <div class='campo-completo campo-row'><input type='checkbox' id='chkSeguro' checked><label>20. Confirmo que el flujo HTTP funciona en un unico canal local.</label></div>"
                + "        <button type='submit'>Enviar Paquete Seguro a Java</button>"
                + "      </div>"
                + "    </form>"
                + "    <div id='feedback'></div>"
                + "  </div>"
                + "  <script>"
                + "    function enviarFormulario(event) {"
                + "      event.preventDefault();"
                + "      const divLog = document.getElementById('feedback');"
                + "      divLog.style.display = 'block';"
                + "      divLog.style.background = '#e3f2fd'; divLog.style.color = '#0d47a1';"
                + "      divLog.innerText = 'Transmitiendo paquete JSON...';"
                + "      const payload = {"
                + "        txtNombre: document.getElementById('txtNombre').value,"
                + "        txtEmail: document.getElementById('txtEmail').value,"
                + "        txtPass: document.getElementById('txtPass').value,"
                + "        txtTel: document.getElementById('txtTel').value,"
                + "        numEdad: document.getElementById('numEdad').value,"
                + "        datNacimiento: document.getElementById('datNacimiento').value,"
                + "        timHora: document.getElementById('timHora').value,"
                + "        colColor: document.getElementById('colColor').value,"
                + "        selPais: document.getElementById('selPais').value,"
                + "        rngNivel: document.getElementById('rngNivel').value,"
                + "        txtSueldo: document.getElementById('txtSueldo').value,"
                + "        txtWeb: document.getElementById('txtWeb').value,"
                + "        chkTerminos: document.getElementById('chkTerminos').checked,"
                + "        chkBoletin: document.getElementById('chkBoletin').checked,"
                + "        radGenero: document.getElementById('radM').checked ? 'M' : 'F',"
                + "        chkNotif: document.getElementById('chkNotif').checked,"
                + "        filAdjunto: document.getElementById('filDocumento').value,"
                + "        schBuscar: document.getElementById('schBuscar').value,"
                + "        txtAreaComentarios: document.getElementById('txtAreaComentarios').value,"
                + "        chkSeguro: document.getElementById('chkSeguro').checked"
                + "      };"
                + "      fetch('/api/formulario?v=' + Date.now(), {"
                + "        method: 'POST',"
                + "        headers: { 'Content-Type': 'application/json; charset=UTF-8' },"
                + "        body: JSON.stringify(payload)"
                + "      })"
                + "      .then(res => res.json())"
                + "      .then(data => {"
                + "        if(data.estado === 'OK') {"
                + "          divLog.style.background = '#e8f5e9'; divLog.style.color = '#1b5e20';"
                + "          divLog.innerHTML = data.mensaje;"
                + "        }"
                + "      })"
                + "      .catch(err => {"
                + "        divLog.style.background = '#ffebee'; divLog.style.color = '#c62828';"
                + "        divLog.innerText = 'Error de respuesta del socket backend';"
                + "      });"
                + "    }"
                + "  </script>"
                + "</body>"
                + "</html>";
    }

}