using System;
using System.IO;
using System.Net;
using System.Text;
using System.Text.Json;

class ServidorConsola2
{
    static void Main(string[] args)
    {
        int puerto = 8088;

        HttpListener servidor = new HttpListener();
        servidor.Prefixes.Add($"http://localhost:{puerto}/");

        try
        {
            servidor.Start();

            Console.WriteLine("==================================================");
            Console.WriteLine(" Servidor HTTP Embebido Iniciado exitosamente");
            Console.WriteLine($" Abre en browser nueva pestaña/tab: http://localhost:{puerto}");
            Console.WriteLine("==================================================\n");

            while (true)
            {
                HttpListenerContext contexto = servidor.GetContext();

                ProcesarSolicitud(contexto);
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error al iniciar servidor:");
            Console.WriteLine(ex.Message);
        }
    }

    static void ProcesarSolicitud(HttpListenerContext contexto)
    {
        string ruta = contexto.Request.Url.AbsolutePath;

        Console.WriteLine($"[PETICION] {contexto.Request.HttpMethod} {ruta}");

        try
        {
            switch (ruta)
            {
                case "/":
                    HandlerRaiz(contexto);
                    break;

                case "/api/datos":
                    HandlerApi(contexto);
                    break;

                case "/api/procesar":
                    HandlerProcesar(contexto);
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

    // ============================================================
    // ENDPOINT 1: PAGINA HTML
    // ============================================================
    static void HandlerRaiz(HttpListenerContext contexto)
    {
        string html = ObtenerHtmlCliente();

        byte[] bytes = Encoding.UTF8.GetBytes(html);

        contexto.Response.ContentType = "text/html; charset=UTF-8";
        contexto.Response.StatusCode = 200;
        contexto.Response.ContentLength64 = bytes.Length;

        contexto.Response.OutputStream.Write(bytes, 0, bytes.Length);
        contexto.Response.OutputStream.Close();
    }

    // ============================================================
    // ENDPOINT 2: API JSON
    // ============================================================
    static void HandlerApi(HttpListenerContext contexto)
    {
        Console.WriteLine("[API] Respuesta enviada.");

        var respuesta = new
        {
            mensaje = "Hola desde el endpoint de C#",
            estado = "Conectado con Exito",
            servidor = "HttpListener Nativo"
        };

        string json = JsonSerializer.Serialize(respuesta);

        byte[] bytes = Encoding.UTF8.GetBytes(json);

        contexto.Response.ContentType = "application/json; charset=UTF-8";
        contexto.Response.StatusCode = 200;
        contexto.Response.ContentLength64 = bytes.Length;

        contexto.Response.OutputStream.Write(bytes, 0, bytes.Length);
        contexto.Response.OutputStream.Close();
    }

    // ============================================================
    // ENDPOINT 3: PROCESAR TEXTO
    // ============================================================
    static void HandlerProcesar(HttpListenerContext contexto)
    {
        using StreamReader reader = new StreamReader(
            contexto.Request.InputStream,
            contexto.Request.ContentEncoding
        );

        string body = reader.ReadToEnd();

        string textoLimpio = "";

        if (!string.IsNullOrEmpty(body) && body.Contains("="))
        {
            string[] partes = body.Split('=');

            if (partes.Length > 1)
            {
                textoLimpio = WebUtility.UrlDecode(partes[1]);
            }
        }

        Console.WriteLine("[CONSOLA 2 C#] El usuario escribió: " + textoLimpio);

        var respuesta = new
        {
            procesado = $"C# recibió tu mensaje: <b>{textoLimpio.ToUpper()}</b>"
        };

        string json = JsonSerializer.Serialize(respuesta);

        byte[] bytes = Encoding.UTF8.GetBytes(json);

        contexto.Response.ContentType = "application/json; charset=UTF-8";
        contexto.Response.StatusCode = 200;
        contexto.Response.ContentLength64 = bytes.Length;

        contexto.Response.OutputStream.Write(bytes, 0, bytes.Length);
        contexto.Response.OutputStream.Close();
    }

    // ============================================================
    // RESPUESTA 404
    // ============================================================
    static void Respuesta404(HttpListenerContext contexto)
    {
        string texto = "404 - Ruta no encontrada";

        byte[] bytes = Encoding.UTF8.GetBytes(texto);

        contexto.Response.StatusCode = 404;
        contexto.Response.ContentType = "text/plain";

        contexto.Response.OutputStream.Write(bytes, 0, bytes.Length);
        contexto.Response.OutputStream.Close();
    }

    // ============================================================
    // RESPUESTA 500
    // ============================================================
    static void Respuesta500(HttpListenerContext contexto, string error)
    {
        string texto = "500 - Error interno\n\n" + error;

        byte[] bytes = Encoding.UTF8.GetBytes(texto);

        contexto.Response.StatusCode = 500;
        contexto.Response.ContentType = "text/plain";

        contexto.Response.OutputStream.Write(bytes, 0, bytes.Length);
        contexto.Response.OutputStream.Close();
    }

    // ============================================================
    // HTML CLIENTE
    // ============================================================
    static string ObtenerHtmlCliente()
    {
        return @"
<!DOCTYPE html>
<html lang='es'>

<head>
<meta charset='UTF-8'>
<title>Panel de Control Socket C#</title>

<style>

body{
    font-family:sans-serif;
    text-align:center;
    padding-top:30px;
    background:#f0f2f5;
}

.bloque{
    background:white;
    padding:20px;
    display:inline-block;
    border-radius:8px;
    width:400px;
    margin:10px;
    box-shadow:0 2px 4px rgba(0,0,0,0.1);
    text-align:left;
    vertical-align:top;
    height:220px;
}

input, button{
    padding:10px;
    width:95%;
    margin-top:10px;
    border-radius:4px;
    border:1px solid #ccc;
    font-size:14px;
    box-sizing:border-box;
}

button{
    background:#0066cc;
    color:white;
    border:none;
    cursor:pointer;
    font-weight:bold;
}

button:hover{
    background:#0053a6;
}

.resultado{
    margin-top:15px;
    padding:8px;
    background:#f8f9fa;
    border-left:4px solid #0066cc;
    font-size:14px;
    min-height:40px;
    word-wrap:break-word;
}

</style>

</head>

<body>

<h1>Consola de Control de Endpoints C#</h1>

<div class='bloque'>

    <h3>1. Enviar Mensaje a Consola C#</h3>

    <input type='text'
           id='wsMsg'
           placeholder='Escribe algo...'>

    <button onclick='procesarTextoLocal()'>
        Enviar a C#
    </button>

    <div class='resultado'
         id='wsLog'>
         Esperando acción...
    </div>

</div>

<div class='bloque'>

    <h3>2. Consultar Endpoint API Fijo</h3>

    <button style='margin-top:44px;'
            onclick='consultarAPI()'>
            Llamar a /api/datos
    </button>

    <div class='resultado'
         id='apiLog'
         style='color:blue; font-family:monospace;'>
         Esperando consulta...
    </div>

</div>

<script>

function procesarTextoLocal()
{
    const txt = document.getElementById('wsMsg').value;

    if(txt.trim() === '')
        return;

    document.getElementById('wsLog').innerText = 'Enviando...';

    fetch('/api/procesar?nocache=' + Date.now(),
    {
        method: 'POST',

        headers:
        {
            'Content-Type':
            'application/x-www-form-urlencoded'
        },

        body: 'texto=' + encodeURIComponent(txt)
    })

    .then(res => res.json())

    .then(json =>
    {
        document.getElementById('wsLog').innerHTML =
            json.procesado;
    })

    .catch(err =>
    {
        document.getElementById('wsLog').innerText =
            'Error al procesar en C#';
    });
}

function consultarAPI()
{
    document.getElementById('apiLog').innerText =
        'Consultando...';

    fetch('/api/datos?nocache=' + Date.now())

    .then(response => response.json())

    .then(objetoJson =>
    {
        document.getElementById('apiLog').innerHTML =

            '<b>Mensaje:</b> ' +
            objetoJson.mensaje +

            '<br>' +

            '<b>Estado:</b> ' +
            objetoJson.estado +

            '<br>' +

            '<b>Motor:</b> ' +
            objetoJson.servidor;
    })

    .catch(err =>
    {
        document.getElementById('apiLog').innerText =
            'Error al invocar API';
    });
}

</script>

</body>
</html>
";
    }
}