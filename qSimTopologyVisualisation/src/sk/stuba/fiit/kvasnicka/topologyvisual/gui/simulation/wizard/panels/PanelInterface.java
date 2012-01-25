/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels;

import javax.swing.JPanel;

/**
 * All wizard's panels must extends this interface
 *
 * @author Igor Kvasnicka
 */
public abstract class PanelInterface extends JPanel {

    /**
     * initializes default values
     *
     * @return false if there was a problem during initialisation
     */
    public abstract boolean init();

    /**
     * validation of user data
     *
     * @return false if there was a validation problem
     */
    public abstract boolean validateData();
}
