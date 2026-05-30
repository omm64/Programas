package Java.ServidorConsola4.core;

import java.util.*;

public class HttpRequest {

    public String method;
    public String path;
    public String version;

    public Map<String,String> headers = new HashMap<>();

    public String body = "";

} 
