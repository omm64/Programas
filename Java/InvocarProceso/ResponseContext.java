// package com.sistema.motor.dto;

import java.util.Map;

public record ResponseContext(
    Map<String, String> type, // a) tipo de proceso
    Map<String, String> msg,  // b) msg (error o exito)
    Object data               // c) respuesta física o tracking
) 
{
    public ResponseContext 
    {
        if (type == null || type.isEmpty()) type = Map.of("ejecucion", "desconocido");
        if (msg == null || msg.isEmpty()) msg = Map.of("error", "Sin mensaje de estado");
        if (data == null) data = Map.of("status", "No data returned");
    }
}

