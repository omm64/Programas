package Java.ServidorConsola4.core;

import java.util.*;

public class Router {

    private Map<String, RouteHandler> routes =
        new HashMap<>();

    public void add(
        String method,
        String path,
        RouteHandler handler) {

        routes.put(
            method + ":" + path,
            handler
        );
    }

    public RouteHandler find(
        String method,
        String path) {

        return routes.get(
            method + ":" + path
        );
    }
}
