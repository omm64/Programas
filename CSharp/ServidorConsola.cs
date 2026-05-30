using System;
using System.IO;
using System.Net;
using System.Text;
using System.Text.Json;

class ServidorConsola
{
    static void Main()
    {
        HttpListener servidor = new HttpListener();

        servidor.Prefixes.Add("http://localhost:8088/");
        servidor.Start();

        Console.WriteLine("Servidor iniciado:");
        Console.WriteLine("http://localhost:8080");

        while (true)
        {
            HttpListenerContext contexto = servidor.GetContext();

            string ruta = contexto.Request.Url.AbsolutePath;

            if (ruta == "/")
            {
                RespuestaHtml(contexto);
            }
            else if (ruta == "/api/datos")
            {
                RespuestaApi(contexto);
            }
            else if (ruta == "/api/procesar")
            {
                Procesar(contexto);
            }
            else
            {
                contexto.Response.StatusCode = 404;
                contexto.Response.Close();
            }
        }
    }

    static void RespuestaHtml(HttpListenerContext ctx)
    {
        string html = @"
<!DOCTYPE html>
<html>
<head>
<meta charset='UTF-8'>
<title>C# HTTP Server</title>
</head>
<body>

<h1>Servidor C#</h1>

<input type='text' id='txt'>
<button onclick='enviar()'>Enviar</button>

<div id='resultado'></div>

<script>
function enviar() {

    const texto = document.getElementById('txt').value;

    fetch('/api/procesar', {
        method: 'POST',
        headers: {
            'Content-Type':'application/x-www-form-urlencoded'
        },
        body:'texto=' + encodeURIComponent(texto)
    })
    .then(r => r.json())
    .then(j => {
        document.getElementById('resultado').innerHTML = j.procesado;
    });
}
</script>

</body>
</html>";

        byte[] buffer = Encoding.UTF8.GetBytes(html);

        ctx.Response.ContentType = "text/html";
        ctx.Response.OutputStream.Write(buffer, 0, buffer.Length);
        ctx.Response.Close();
    }

    static void RespuestaApi(HttpListenerContext ctx)
    {
        var obj = new
        {
            mensaje = "Hola desde C#",
            estado = "OK",
            servidor = "HttpListener"
        };

        string json = JsonSerializer.Serialize(obj);

        byte[] buffer = Encoding.UTF8.GetBytes(json);

        ctx.Response.ContentType = "application/json";
        ctx.Response.OutputStream.Write(buffer, 0, buffer.Length);
        ctx.Response.Close();
    }

    static void Procesar(HttpListenerContext ctx)
    {
        using StreamReader reader =
            new StreamReader(ctx.Request.InputStream);

        string body = reader.ReadToEnd();

        string texto = "";

        if (body.Contains("="))
        {
            texto = WebUtility.UrlDecode(body.Split('=')[1]);
        }

        Console.WriteLine("Texto recibido: " + texto);

        var respuesta = new
        {
            procesado = $"C# recibió: <b>{texto.ToUpper()}</b>"
        };

        string json = JsonSerializer.Serialize(respuesta);

        byte[] buffer = Encoding.UTF8.GetBytes(json);

        ctx.Response.ContentType = "application/json";
        ctx.Response.OutputStream.Write(buffer, 0, buffer.Length);
        ctx.Response.Close();
    }
}