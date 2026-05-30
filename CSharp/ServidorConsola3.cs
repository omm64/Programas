using System;
using System.IO;
using System.IO.Compression;
using System.Net;
using System.Text;
using System.Text.Json;

class ServidorConsola3
{
    static void Main(string[] args)
    {
        //para que no choque con los servicios de java 8082
        int puerto = 8088;

        HttpListener servidor = new HttpListener();

        servidor.Prefixes.Add($"http://localhost:{puerto}/");

        try
        {
            servidor.Start();

            Console.WriteLine("==========================================================");
            Console.WriteLine(" Servidor Integral C# activado ");
            Console.WriteLine(" GZIP + BASE64 + CORS + JSON + Multiples ENDPOINT ");
            Console.WriteLine($" http://localhost:{puerto}");
            Console.WriteLine("==========================================================\n");

            while (true)
            {
                HttpListenerContext contexto = servidor.GetContext();

                ProcesarSolicitud(contexto);
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("ERROR:");
            Console.WriteLine(ex.Message);
        }
    }

    /* 
    ** ============================================================
    ** Ruteo central
    ** ============================================================ 
    */
    static void ProcesarSolicitud(HttpListenerContext contexto)
    {
        string ruta = contexto.Request.Url.AbsolutePath;

        Console.WriteLine($"[{DateTime.Now:HH:mm:ss}] {contexto.Request.HttpMethod} {ruta}");

        try
        {
            switch (ruta)
            {
                case "/":
                    EndpointRaiz(contexto);
                    break;

                case "/api/formulario":
                    EndpointFormulario(contexto);
                    break;

                case "/api/saludo":
                    EndpointSaludo(contexto);
                    break;

                case "/api/hora":
                    EndpointHora(contexto);
                    break;

                case "/api/status":
                    EndpointStatus(contexto);
                    break;

                default:
                    Respuesta404(contexto);
                    break;
            }
        }
        catch (Exception ex)
        {
            Respuesta500(contexto, ex.Message);
        }
    }

    /* 
    ** ============================================================
    ** Endpoint Raiz inicio
    ** ============================================================ 
    */
    static void EndpointRaiz(HttpListenerContext contexto)
    {
        Console.WriteLine("[SEGURIDAD] Comprimiendo HTML + Base64");

        ConfigurarCors(contexto);

        if (contexto.Request.HttpMethod == "OPTIONS")
        {
            contexto.Response.StatusCode = 204;
            contexto.Response.Close();
            return;
        }

        string htmlOriginal = ObtenerHtmlFormulario();

        /* 
        ** ============================================================
        ** Manejo con BASE64 esto hace que el codigo de la pagina ya no
        ** pueda visualizarse solo ver BASE64
        ** ============================================================ 
        */

        string htmlBase64 =
            Convert.ToBase64String(
                Encoding.UTF8.GetBytes(htmlOriginal)
            );

        string bootstrap =
            "<!DOCTYPE html>" +
            "<html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body>" +
            "<script>" +
            "document.open();" +
            "document.write(atob('" + htmlBase64 + "'));" +
            "document.close();" +
            "</script>" +
            "</body>" +
            "</html>";

        byte[] bytesBootstrap =
            Encoding.UTF8.GetBytes(bootstrap);

        /* 
        ** ============================================================
        ** zippear con GZIP todo el codigo se manda compactado
        ** y en Base64
        ** ============================================================ 
        */

        byte[] bytesComprimidos;

        using (MemoryStream ms = new MemoryStream())
        {
            using (GZipStream gzip =  new GZipStream(ms, CompressionMode.Compress))
            {
                gzip.Write(bytesBootstrap, 0, bytesBootstrap.Length);
            }

            bytesComprimidos = ms.ToArray();
        }

        contexto.Response.ContentType =
            "text/html; charset=UTF-8";

        contexto.Response.AddHeader(
            "Content-Encoding",
            "gzip"
        );

        contexto.Response.StatusCode = 200;

        contexto.Response.OutputStream.Write(
            bytesComprimidos,
            0,
            bytesComprimidos.Length
        );

        contexto.Response.Close();

        Console.WriteLine(
            $"HTML enviado | Original: {bytesBootstrap.Length} bytes | GZIP: {bytesComprimidos.Length} bytes"
        );
    }

    /* 
    ** ============================================================
    ** Endpoint Formulario (JSON)
    ** ============================================================ 
    */
    static void EndpointFormulario(HttpListenerContext contexto)
    {
        ConfigurarCors(contexto);

        if (contexto.Request.HttpMethod == "OPTIONS")
        {
            contexto.Response.StatusCode = 204;
            contexto.Response.Close();
            return;
        }

        using StreamReader reader =
            new StreamReader(
                contexto.Request.InputStream,
                Encoding.UTF8
            );

        string json = reader.ReadToEnd();

        Console.WriteLine("\n========== JSON RECIBIDO ==========");
        Console.WriteLine(json);
        Console.WriteLine("===================================\n");

        using JsonDocument doc =
            JsonDocument.Parse(json);

        JsonElement root = doc.RootElement;

        string nombre = root.GetProperty("txtNombre").GetString() ?? "";
        string correo = root.GetProperty("txtEmail").GetString() ?? "";
        string edad = root.GetProperty("numEdad").GetString() ?? "";
        string pais = root.GetProperty("selPais").GetString() ?? "";

        Console.WriteLine("========== VARIABLES ==========");
        Console.WriteLine("Nombre : " + nombre);
        Console.WriteLine("Correo : " + correo);
        Console.WriteLine("Edad   : " + edad);
        Console.WriteLine("Pais   : " + pais);
        Console.WriteLine("===============================\n");

        var respuesta = new
        {
            estado = "OK",
            mensaje =
                $"C# recibió y procesó correctamente el formulario (ver consola en terminal) de <b>{nombre}</b>"
        };

        RespuestaJson(contexto, respuesta);
    }

    /* 
    ** ============================================================
    ** Endpoint Saludo
    ** ============================================================ 
    */
    static void EndpointSaludo(HttpListenerContext contexto)
    {
        var respuesta = new
        {
            mensaje = "Hola desde endpoint saludo",
            tecnologia = "C# HttpListener",
            ok = true
        };

        RespuestaJson(contexto, respuesta);
    }

    /* 
    ** ============================================================
    ** Endpoint presentar Hora
    ** ============================================================ 
    */
    static void EndpointHora(HttpListenerContext contexto)
    {
        var respuesta = new
        {
            hora = DateTime.Now.ToString("HH:mm:ss"),
            fecha = DateTime.Now.ToString("yyyy-MM-dd"),
            servidor = Environment.MachineName
        };

        RespuestaJson(contexto, respuesta);
    }

    /* 
    ** ============================================================
    ** Endpoint Status
    ** ============================================================ 
    */
    static void EndpointStatus(HttpListenerContext contexto)
    {
        var respuesta = new
        {
            estado = "ONLINE",
            memoriaMB =
                GC.GetTotalMemory(false) / 1024 / 1024,

            sistema =
                Environment.OSVersion.ToString(),

            procesadores =
                Environment.ProcessorCount,

            versionDotnet =
                Environment.Version.ToString()
        };

        RespuestaJson(contexto, respuesta);
    }

    /* 
    ** ============================================================
    ** Respuesta JSON
    ** ============================================================ 
    */
    static void RespuestaJson(
        HttpListenerContext contexto,
        object objeto
    )
    {
        string json =
            JsonSerializer.Serialize(
                objeto,
                new JsonSerializerOptions
                {
                    WriteIndented = true
                }
            );

        byte[] bytes =
            Encoding.UTF8.GetBytes(json);

        contexto.Response.ContentType =
            "application/json; charset=UTF-8";

        contexto.Response.StatusCode = 200;

        contexto.Response.OutputStream.Write(
            bytes,
            0,
            bytes.Length
        );

        contexto.Response.Close();
    }

    /* 
    ** ============================================================
    ** CORS
    ** ============================================================ 
    */
    static void ConfigurarCors(HttpListenerContext contexto)
    {
        contexto.Response.AddHeader(
            "Access-Control-Allow-Origin",
            "*"
        );

        contexto.Response.AddHeader(
            "Access-Control-Allow-Methods",
            "GET, POST, OPTIONS"
        );

        contexto.Response.AddHeader(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );
    }

    /* 
    ** ============================================================
    ** Error 404
    ** ============================================================ 
    */
    static void Respuesta404(HttpListenerContext contexto)
    {
        byte[] bytes =
            Encoding.UTF8.GetBytes(
                "404 - Endpoint no encontrado"
            );

        contexto.Response.StatusCode = 404;

        contexto.Response.OutputStream.Write(
            bytes,
            0,
            bytes.Length
        );

        contexto.Response.Close();
    }

    /* 
    ** ============================================================
    ** Error 500
    ** ============================================================ 
    */
    static void Respuesta500(
        HttpListenerContext contexto,
        string error
    )
    {
        byte[] bytes =
            Encoding.UTF8.GetBytes(
                "500 - " + error
            );

        contexto.Response.StatusCode = 500;

        contexto.Response.OutputStream.Write(
            bytes,
            0,
            bytes.Length
        );

        contexto.Response.Close();
    }

    // ============================================================
    // HTML CLIENTE
    // ============================================================
    static string ObtenerHtmlFormulario()
    {
        return @"
<!DOCTYPE html>
<html lang='es'>
<head>
<meta charset='UTF-8'>
<title>Servidor Integral C#</title>
<style>

body{ font-family:Arial; background:#f4f6f9;  margin:0; padding:20px; }

.contenedor{ max-width:900px; background:white; margin:auto;
    padding:30px; border-radius:12px;box-shadow:0 4px 15px rgba(0,0,0,0.05);
}

.grid{
    display:grid;
    grid-template-columns:1fr 1fr;
    gap:15px;
}

.campo{
    display:flex;
    flex-direction:column;
}

input,select,textarea{
    padding:8px;
    border:1px solid #ccc;
    border-radius:4px;
}

button{
    background:#0066cc;
    color:white;
    border:none;
    padding:12px;
    border-radius:6px;
    cursor:pointer;
    margin-top:10px;
}

button:hover{
    background:#004999;
}

#feedback{
    margin-top:20px;
    padding:10px;
    background:#e8f5e9;
    border-radius:6px;
}

.api{
    margin-top:30px;
    border-top:1px solid #ddd;
    padding-top:20px;
}

pre{
    background:#222;
    color:#0f0;
    padding:10px;
    border-radius:6px;
    overflow:auto;
}

</style>

</head>

<body>

<div class='contenedor'>

<h2>Servidor Integral C#</h2>

<form onsubmit='enviarFormulario(event)'>

<div class='grid'>

<div class='campo'>
<label>Nombre</label>
<input type='text' id='txtNombre' value='Rodrigo Armenta'>
</div>

<div class='campo'>
<label>Email</label>
<input type='email' id='txtEmail' value='Rodrigo@mail.com'>
</div>

<div class='campo'>
<label>Edad</label>
<input type='number' id='numEdad' value='35'>
</div>

<div class='campo'>
<label>Pais</label>

<select id='selPais'>
<option>México</option>
<option>Colombia</option>
<option>Argentina</option>
</select>

</div>

<div class='campo'>
<label>Comentarios</label>
<textarea id='txtArea'>
Prueba servidor C#
</textarea>
</div>

</div>

<button type='submit'>
Enviar JSON
</button>

</form>

<div id='feedback'>
Esperando...
</div>

<div class='api'>

<h3>Endpoints de Ejemplo solo es un Mapeo de </h3>

<button onclick='llamarApi(""saludo"")'>
/api/saludo
</button>

<button onclick='llamarApi(""hora"")'>
/api/hora
</button>

<button onclick='llamarApi(""status"")'>
/api/status
</button>

<pre id='resultadoApi'>
Esperar llamada API...
</pre>

</div>

</div>

<script>

function enviarFormulario(e)
{
    e.preventDefault();

    const payload =
    {
        txtNombre:
            document.getElementById('txtNombre').value,

        txtEmail:
            document.getElementById('txtEmail').value,

        numEdad:
            document.getElementById('numEdad').value,

        selPais:
            document.getElementById('selPais').value,

        txtArea:
            document.getElementById('txtArea').value
    };

    fetch('/api/formulario',
    {
        method:'POST',

        headers:
        {
            'Content-Type':
            'application/json'
        },

        body: JSON.stringify(payload)
    })

    .then(r => r.json())

    .then(j =>
    {
        document.getElementById('feedback').innerHTML =
            j.mensaje;
    });
}

function llamarApi(nombre)
{
    fetch('/api/' + nombre)

    .then(r => r.json())

    .then(j =>
    {
        document.getElementById('resultadoApi').innerText =
            JSON.stringify(j, null, 4);
    });
}

</script>

</body>
</html>
";
    }
}