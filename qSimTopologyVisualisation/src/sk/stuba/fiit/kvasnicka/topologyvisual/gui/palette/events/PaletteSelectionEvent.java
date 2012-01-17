/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events;

import java.util.EventObject;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;

/**
 *
 * @author Igor Kvasnicka
 */
public class PaletteSelectionEvent extends EventObject {

    @Getter
    private PaletteActionEnum selectedAction;

    public PaletteSelectionEvent(Object source, PaletteActionEnum selectedAction) {
        super(source);
        this.selectedAction = selectedAction;
    }

    public PaletteSelectionEvent(Object source) {
        super(source);
        selectedAction = null;
    }
}
