/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.exceptions;

/**
 *
 * @author Igor Kvasnicka
 */
public class ExportException extends Exception {

    /**
     * Creates a new instance of
     * <code>ExportException</code> without detail message.
     */
    public ExportException() {
    }

    /**
     * Constructs an instance of
     * <code>ExportException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ExportException(String msg) {
        super(msg);
    }

    public ExportException(Throwable cause) {
        super(cause);
    }
}
