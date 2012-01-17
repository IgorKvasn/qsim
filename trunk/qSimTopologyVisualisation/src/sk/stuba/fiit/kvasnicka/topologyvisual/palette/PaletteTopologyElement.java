/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.palette;

/**
 * Node that will be shown in the Palette
 * @author Igor Kvasnicka
 */
@Deprecated
public class PaletteTopologyElement {

    private String name;
    private final PaletteActionEnum paletteAction;

    public PaletteTopologyElement(String name, PaletteActionEnum paletteAction) {
        this.name = name;
        this.paletteAction = paletteAction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PaletteActionEnum getPaletteAction() {
        return paletteAction;
    }
}
