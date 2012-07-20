/*
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard;

import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.*;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationRuleIterator {

    private final static Logger logg = Logger.getLogger(SimulationRuleIterator.class);
    /**
     * defining source and destination of a simulation rule
     */
    public static final int SOURCE_DEST_PANEL = 0;
    /**
     * defining route
     */
    public static final int ROUTING_PANEL = 1;
    /**
     * defining simulation rule - number of packets, type of packets, ...
     */
    public static final int PACKET_RULES_PANEL = 2;
    private final int NUMBER_OF_PANELS = 3;
    private PanelInterface[] panels = new PanelInterface[NUMBER_OF_PANELS];
    private int actualPanel = -1;
    @Setter
    private ContainerPanel containerPanel;
    private int defaulPanel;
    private boolean allowPrevious;
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
    public void initDefaultPanel(Data data, int panelToShow, boolean allowPrevious) {
        storedData = data;
        this.allowPrevious = allowPrevious;
        defaulPanel = panelToShow;
        initPanels();
        actualPanel = panelToShow;
        panels[actualPanel].init(this);
        panels[actualPanel].initValues(data);
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
        panels[actualPanel].initValues(storedData);

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
        panels[actualPanel].initValues(storedData);

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

    /**
     * previous button is disabled, if allowPrevious is false and current panel
     * number is "defaulPanel"
     *
     * @return
     */
    public boolean isPreviousDisabled() {
        return !allowPrevious && actualPanel == defaulPanel;

    }
}
