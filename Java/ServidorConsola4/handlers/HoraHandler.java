package Java.ServidorConsola4.handlers;

import Java.ServidorConsola4.core.*;
import java.time.LocalDateTime;

public class HoraHandler implements RouteHandler {

    @Override
    public void handle(
        HttpRequest req,
        HttpResponse res)
        throws Exception {

        res.json(
            "{\"hora\":\"" +
            LocalDateTime.now() +
            "\"}"
        );
    }
}