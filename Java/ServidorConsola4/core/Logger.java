package Java.ServidorConsola4.core;

import java.time.LocalDateTime;

public class Logger {

    public static void info(String msg) {

        System.out.println(
            LocalDateTime.now() +
            " INFO " +
            msg
        );
    }

    public static void error(String msg) {

        System.out.println(
            LocalDateTime.now() +
            " ERROR " +
            msg
        );
    }
}
