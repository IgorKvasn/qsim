/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard;

import java.util.List;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.*;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationRuleIterator {

    private final static Logger logg = Logger.getLogger(SimulationRuleIterator.class);
    private final int NUMBER_OF_PANELS = 3;
    private PanelInterface[] panels = new PanelInterface[NUMBER_OF_PANELS];
    private int actualPanel = -1;
    @Setter
    private ContainerPanel containerPanel;
    @Getter
    private Data storedData;

    public SimulationRuleIterator() {
    }

    /**
     * panel initialisation
     */
    private void initPanels() {
        panels[0] = new VerticesSelectionPanel();
        panels[1] = new RoutingPanel();
        panels[2] = new PacketSendingPanel();
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
        storedData = new Data();
        
        initPanels();
        actualPanel = 0;
        panels[actualPanel].init(this);
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
        boolean initResult = panels[actualPanel].init(this);
        if (!initResult) {//panel did not init properly            
            logg.error("Error during panel initialisation - panel number: " + actualPanel);
            actualPanel--;
            return;
        }

        containerPanel.setPanel(panels[actualPanel]);
    }

    /**
     * validates currently visible panel
     */
    public boolean isCurrentPanelValid() {
        if (actualPanel < 0 || panels.length <= actualPanel) {
            throw new IllegalStateException("illegal number of actual panel: " + actualPanel);
        }
        return panels[actualPanel].validateData();
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

        boolean initResult = panels[actualPanel].init(this);
        if (!initResult) {//panel did not init properly            
            logg.error("Error during panel initialisation - panel number: " + actualPanel);
            actualPanel++;
            return;
        }
        containerPanel.setPanel(panels[actualPanel]);
    }

    /**
     * deletes all information stored during wizard life-cycle
     */
    public void cancelIterator() {
        storedData = null;
    }

    public JPanel getCurrentPanel() {
        if (actualPanel < 0 || panels.length <= actualPanel) {
            throw new IllegalStateException("illegal number of actual panel: " + actualPanel);
        }
        return panels[actualPanel];
    }

    @Getter
    @Setter
    public static class Data {

        private TopologyVertex sourceVertex, destinationVertex;
        private List<TopologyVertex> route ;
        private Layer4TypeEnum layer4protocol;
        private int packetSize;
        private int packetCount;
        private PacketTypeEnum packetType;
        private int activationDelay;
        private boolean ping;
    }
}
