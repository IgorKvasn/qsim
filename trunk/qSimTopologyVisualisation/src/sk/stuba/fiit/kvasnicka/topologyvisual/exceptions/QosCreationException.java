/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.exceptions;

/**
 * there was a problem creating QosMechanismDefinition object
 *
 * @author Igor Kvasnicka
 */
public class QosCreationException extends Exception {

    public QosCreationException(String message) {
        super(message);
    }
}
