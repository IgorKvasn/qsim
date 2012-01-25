/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard;

import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.ContainerPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.PacketSendingPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.PanelInterface;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.VerticesSelectionPanel;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationRuleIterator {

    private final static Logger logg = Logger.getLogger(SimulationRuleIterator.class);
    private final int NUMBER_OF_PANELS = 2;
    private PanelInterface[] panels = new PanelInterface[NUMBER_OF_PANELS];
    private int actualPanel = -1;
    @Setter
    private ContainerPanel containerPanel;

    public SimulationRuleIterator() {
    }

    /**
     * panel initialisation
     */
    private void initPanels() {
        panels[0] = new VerticesSelectionPanel();
        panels[1] = new PacketSendingPanel();
    }

    /**
     * is the actual panel last one?
     *
     * @return
     */
    public boolean isPanelLast() {
        return actualPanel == (NUMBER_OF_PANELS - 1);
    }

    /**
     * is the actual panel first one?
     *
     * @return
     */
    public boolean isPanelFirst() {
        return actualPanel == 0;
    }

    /**
     * shows the first panel
     */
    public void initDefaultPanel() {
        initPanels();
        actualPanel = 0;
        panels[actualPanel].init();
        containerPanel.setPanel(panels[actualPanel]);
    }

    /**
     * moves panel to he next one and shows this panel
     */
    public void nextPanel() {
        if (containerPanel == null) {
            throw new IllegalStateException("containerPanel is NULL");
        }
        if (actualPanel == NUMBER_OF_PANELS - 1) { //this is the last available panel
            return;
        }

        if (!panels[actualPanel].validateData()) {
            return;
        }
        actualPanel++;
        boolean initResult = panels[actualPanel].init();
        if (!initResult) {//panel did not init properly            
            logg.error("Error during panel initialisation - panel number: " + actualPanel);
            actualPanel--;
            return;
        }

        containerPanel.setPanel(panels[actualPanel]);
    }

    /**
     * moves panel to he previous one and shows this panel
     */
    public void previousPanel() {
        if (containerPanel == null) {
            throw new IllegalStateException("containerPanel is NULL");
        }
        if (actualPanel == 0) { //this is the last available panel
            return;
        }
        actualPanel--;

        boolean initResult = panels[actualPanel].init();
        if (!initResult) {//panel did not init properly            
            logg.error("Error during panel initialisation - panel number: " + actualPanel);
            actualPanel++;
            return;
        }
        containerPanel.setPanel(panels[actualPanel]);
    }
}
