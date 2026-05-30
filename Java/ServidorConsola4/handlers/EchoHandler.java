package Java.ServidorConsola4.handlers;

import Java.ServidorConsola4.core.*;

public class EchoHandler
implements RouteHandler {

    @Override
    public void handle(
        HttpRequest req,
        HttpResponse res)
        throws Exception {

        res.json(req.body);
    }
}