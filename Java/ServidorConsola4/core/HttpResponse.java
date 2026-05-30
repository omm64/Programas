package Java.ServidorConsola4.core;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {

    private OutputStream out;

    public HttpResponse(OutputStream out) {
        this.out = out;
    }

    public void json(String json) throws Exception {

        byte[] data =
            json.getBytes(StandardCharsets.UTF_8);

        String header =
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: application/json\r\n" +
            "Content-Length: " + data.length + "\r\n" +
            "Access-Control-Allow-Origin: *\r\n" +
            "\r\n";

        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(data);
        out.flush();
    }

    public void error(int code,String msg) throws Exception {

        String body =
            "{\"error\":\""+msg+"\"}";

        byte[] data =
            body.getBytes(StandardCharsets.UTF_8);

        String header =
            "HTTP/1.1 " + code + " ERROR\r\n" +
            "Content-Type: application/json\r\n" +
            "Content-Length: " + data.length + "\r\n" +
            "\r\n";

        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(data);
        out.flush();
    }
}