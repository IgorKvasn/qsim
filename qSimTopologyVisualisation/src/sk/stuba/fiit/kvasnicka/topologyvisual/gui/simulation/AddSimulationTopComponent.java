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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation;

import java.awt.BorderLayout;
import org.apache.log4j.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.ContainerPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation//AddSimulation//EN",
autostore = false)
@TopComponent.Description(preferredID = "AddSimulationTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.AddSimulationTopComponent")
//@ActionReference(path = "Menu/Window" /*
// * , position = 333
// */)
//workaround for bug: http://netbeans.org/bugzilla/show_bug.cgi?id=208059
//@TopComponent.OpenActionRegistration(displayName = "#CTL_AddSimulationAction",
//preferredID = "AddSimulationTopComponent")
@Messages({
    "CTL_AddSimulationAction=Add new simulation rule",
    "CTL_AddSimulationTopComponent=Add new simulation rule",
    "HINT_AddSimulationTopComponent=This is a AddSimulation window"
})
public final class AddSimulationTopComponent extends TopComponent {

    private final static Logger logg = Logger.getLogger(AddSimulationTopComponent.class);
    private ContainerPanel containerPanel;
    private SimulationRuleIterator panelIterator;
    private Data modifyData = null; //simulation data to be modified; null if new data should be created
    private int panelToShow = 0;

    public AddSimulationTopComponent() {
        initComponents();
        setName(Bundle.CTL_AddSimulationTopComponent());
        setToolTipText(Bundle.HINT_AddSimulationTopComponent());
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.TRUE);
        panelIterator = new SimulationRuleIterator();
        containerPanel = new ContainerPanel(panelIterator);
        panelIterator.setContainerPanel(containerPanel);
        setLayout(new BorderLayout());
        add(containerPanel, BorderLayout.CENTER);
        validate();
    }

    /**
     * modify existing simulation rule data
     *
     * @param data
     * @param defaultPanel
     */
    public void modifySimulationRule(Data data, int defaultPanel) {
        modifyData = data;
        panelToShow = defaultPanel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (modifyData != null) {
            panelIterator.initDefaultPanel(modifyData, panelToShow, false);
        } else {
            panelIterator.initDefaultPanel(new Data(), panelToShow, true);
        }
        modifyData = null;
        panelToShow = 0;
    }

    @Override
    public void componentClosed() {
        panelIterator.cancelIterator();
        NetbeansWindowHelper.getInstance().getActiveTopology().setDefaultMode();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}