/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events;

import java.util.EventListener;

/**
 *
 * @author Igor Kvasnicka
 */
public interface PaletteSelectionListener extends EventListener {

    public void paletteSelectedOccurred(PaletteSelectionEvent evt);

    public void paletteDeselectedOccurred(PaletteSelectionEvent evt);
}
