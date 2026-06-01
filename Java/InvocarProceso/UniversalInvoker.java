// package com.sistema.motor.reflection;

import java.lang.reflect.Method;
import java.util.Arrays;

public class UniversalInvoker {

    public static Object ejecutar(Object instancia, String nombreMetodo, Object[] parametros) throws Exception {
        Class<?>[] tiposParametros = new Class<?>[parametros.length];
        for (int i = 0; i < parametros.length; i++) {
            tiposParametros[i] = (parametros[i] != null) ? parametros[i].getClass() : Object.class;
        }

        Method metodoCorrecto = buscarMetodo(instancia.getClass(), nombreMetodo, tiposParametros);
        return metodoCorrecto.invoke(instancia, parametros);
    }

    private static Method buscarMetodo(Class<?> clase, String nombre, Class<?>[] tipos) throws NoSuchMethodException {
        for (Method m : clase.getMethods()) {
            if (m.getName().equals(nombre) && m.getParameterCount() == tipos.length) {
                Class<?>[] mTipos = m.getParameterTypes();
                boolean esCompatible = true;
                for (int i = 0; i < tipos.length; i++) {
                    if (!mTipos[i].isAssignableFrom(tipos[i])) {
                        esCompatible = false;
                        break;
                    }
                }
                if (esCompatible) return m;
            }
        }
        throw new NoSuchMethodException("Método no compatible: " + nombre + " con parámetros " + Arrays.toString(tipos));
    }
}

