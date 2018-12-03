package com.hzq.initer.compiler;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Created by hezhiqiang on 2018/11/21.
 */

public class Logger {
    private Messager msg;

    public Logger(Messager messager) {
        msg = messager;
    }

    public void info(CharSequence info) {
        if(info != null && info.length() != 0) {
            msg.printMessage(Diagnostic.Kind.NOTE,Consts.PREFIX_OF_LOGGER + info);
        }
    }

    public void error(CharSequence error) {
        if(error != null && error.length() != 0) {
            msg.printMessage(Diagnostic.Kind.ERROR,Consts.PREFIX_OF_LOGGER + "An exception is encountered, [" + error + "]");
        }
    }

    public void error(Throwable error) {
        if (null != error) {
            msg.printMessage(Diagnostic.Kind.ERROR, Consts.PREFIX_OF_LOGGER + "An exception is encountered, [" + error.getMessage() + "]" + "\n" + formatStackTrace(error.getStackTrace()));
        }
    }

    public void warning(CharSequence warning) {
        if(warning != null && warning.length() != 0) {
            msg.printMessage(Diagnostic.Kind.WARNING,Consts.PREFIX_OF_LOGGER + warning);
        }
    }

    private String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
