/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes;

/**
 *
 * @author Igor Kvasnicka
 */
public enum NetworkNodePropertyEnum {

    RX("RX"), TX("TX"), OUTPUT_BUFFER("Output buffer"), INPUT_BUFFER("Input buffer"), PROCESSING("Processing");
    private final String visibleName;

    private NetworkNodePropertyEnum(String visibleName) {
        this.visibleName = visibleName;
    }

    @Override
    public String toString() {
        return visibleName;
    }
}