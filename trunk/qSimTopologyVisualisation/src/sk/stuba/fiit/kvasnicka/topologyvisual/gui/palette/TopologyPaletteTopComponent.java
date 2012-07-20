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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette//TopologyPalette//EN",
autostore = false)
@TopComponent.Description(preferredID = "TopologyPaletteTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.TopologyPaletteTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TopologyPaletteAction",
preferredID = "TopologyPaletteTopComponent")
@Messages({
    "CTL_TopologyPaletteAction=TopologyPalette",
    "CTL_TopologyPaletteTopComponent=TopologyPalette Window",
    "HINT_TopologyPaletteTopComponent=This is a TopologyPalette window",
    "nodes=Nodes",
    "edges=Edges",
    "custom_nodes=Custom nodes",
    "custom_edges=Custom edges"
})
public final class TopologyPaletteTopComponent extends TopComponent {

    public TopologyPaletteTopComponent() {
        initComponents();
        setName(Bundle.CTL_TopologyPaletteTopComponent());
        setToolTipText(Bundle.HINT_TopologyPaletteTopComponent());
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        initPalette();
    }

    private void initPalette() {
        String category = NbBundle.getMessage(TopologyVisualisation.class, "nodesCategory");
        topologyPalette1.addCategory(category);
        topologyPalette1.addChild(category, NbBundle.getMessage(TopologyVisualisation.class, "routerVertex"), PaletteActionEnum.NEW_VERTEX_ROUTER);
        topologyPalette1.addChild(category, NbBundle.getMessage(TopologyVisualisation.class, "switchVertex"), PaletteActionEnum.NEW_VERTEX_SWITCH);
        topologyPalette1.addChild(category, NbBundle.getMessage(TopologyVisualisation.class, "computerVertex"), PaletteActionEnum.NEW_VERTEX_PC);

        category = NbBundle.getMessage(TopologyVisualisation.class, "linksCategory");
        topologyPalette1.addCategory(category);
        topologyPalette1.addChild(category, NbBundle.getMessage(TopologyVisualisation.class, "gigaEthernetLink"), PaletteActionEnum.NEW_EDGE_GIGA_ETHERNET);
        topologyPalette1.addChild(category, NbBundle.getMessage(TopologyVisualisation.class, "fastEthernetLink"), PaletteActionEnum.NEW_EDGE_FAST_ETHERNET);
        topologyPalette1.addChild(category, NbBundle.getMessage(TopologyVisualisation.class, "ethernetLink"), PaletteActionEnum.NEW_EDGE_ETHERNET);
        topologyPalette1.addChild(category, NbBundle.getMessage(TopologyVisualisation.class, "customLink"), PaletteActionEnum.NEW_EDGE_CUSTOM);

        topologyPalette1.addCategory(NbBundle.getMessage(TopologyPaletteTopComponent.class, "custom_nodes"));
        topologyPalette1.addCategory(NbBundle.getMessage(TopologyPaletteTopComponent.class, "custom_edges"));
    }

    private void resetPalette() {
        topologyPalette1.clearCategory(NbBundle.getMessage(TopologyPaletteTopComponent.class, "custom_nodes"));
        topologyPalette1.clearCategory(NbBundle.getMessage(TopologyPaletteTopComponent.class, "custom_edges"));
    }

    public void clearSelection() {
        topologyPalette1.clearSelection();
    }

    public void initListener(PaletteSelectionListener l) {
        topologyPalette1.addPaletteSelectionListener(l);
    }

    /**
     * enables/disables all buttons in palette
     */
    public void setEnabledPalette(boolean enable) {
        topologyPalette1.setEnabledButtons(enable);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topologyPalette1 = new sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.TopologyPalette();
        jButton1 = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(TopologyPaletteTopComponent.class, "TopologyPaletteTopComponent.jButton1.text_1")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(topologyPalette1, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 31, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topologyPalette1, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.TopologyPalette topologyPalette1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
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
