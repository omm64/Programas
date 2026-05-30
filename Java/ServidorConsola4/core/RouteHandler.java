package Java.ServidorConsola4.core;

public interface RouteHandler {
    void handle(HttpRequest req, HttpResponse res) throws Exception;
}
