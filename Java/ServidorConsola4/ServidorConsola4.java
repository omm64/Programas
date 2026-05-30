package Java.ServidorConsola4;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import Java.ServidorConsola4.core.*;
import Java.ServidorConsola4.handlers.*;

public class ServidorConsola4 {

    private static Router router =
        new Router();

    public static void main(String[] args)
    throws Exception {

        int port = 8088;
        int maxThreads = 20;

        router.add(
            "GET",
            "/status",
            new StatusHandler());

        router.add(
            "GET",
            "/hora",
            new HoraHandler());

        router.add(
            "POST",
            "/echo",
            new EchoHandler());

        ExecutorService pool =
            Executors.newFixedThreadPool(
                maxThreads);

        ServerSocket server =
            new ServerSocket(port);

        Logger.info(
            "Servidor iniciado puerto "
            + port);

        while(true) {

            Socket socket =
                server.accept();

            pool.submit(
                () -> process(socket)
            );
        }
    }

    private static void process(
        Socket socket) {

        try {

            BufferedReader br =
                new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()));

            String firstLine =
                br.readLine();

            if(firstLine == null)
                return;

            String[] parts =
                firstLine.split(" ");

            HttpRequest req =
                new HttpRequest();

            req.method = parts[0];
            req.path = parts[1];
            req.version = parts[2];

            String line;

            int contentLength = 0;

            while(!(line = br.readLine())
                    .isEmpty()) {

                int p = line.indexOf(':');

                if(p > 0) {

                    String key =
                        line.substring(0,p)
                            .trim();

                    String value =
                        line.substring(p+1)
                            .trim();

                    req.headers.put(
                        key,
                        value
                    );

                    if(key.equalsIgnoreCase(
                        "Content-Length")) {

                        contentLength =
                            Integer.parseInt(
                                value);
                    }
                }
            }

            if(contentLength > 0) {

                char[] body =
                    new char[contentLength];

                br.read(body);

                req.body =
                    new String(body);
            }

            Logger.info(
                req.method +
                " " +
                req.path);

            HttpResponse res =
                new HttpResponse(
                    socket.getOutputStream());

            RouteHandler handler =
                router.find(
                    req.method,
                    req.path);

            if(handler == null) {

                res.error(
                    404,
                    "Ruta no encontrada");
            }
            else {

                handler.handle(
                    req,
                    res);
            }

            socket.close();

        }
        catch(Exception ex) {

            Logger.error(
                ex.getMessage());

            try {
                socket.close();
            }
            catch(Exception e) {
            }
        }
    }
}
