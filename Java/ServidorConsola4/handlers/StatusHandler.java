package Java.ServidorConsola4.handlers;

import Java.ServidorConsola4.core.*;

public class StatusHandler implements RouteHandler {

    @Override
    public void handle(
        HttpRequest req,
        HttpResponse res)
        throws Exception {

        res.json(
            "{\"status\":\"ok\"}"
        );
    }
}
