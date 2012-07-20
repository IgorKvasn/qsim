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
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui;

import java.io.Serializable;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;

/**
 * this MultiViewElement is very (!) important to make the whole Topcomponent
 * serialisable (because of window persistence). it is also very important to
 * make this MultiViewElement default MultiViewElement that is shown
 *
 * @author Igor Kvasnicka
 */

public class TopologyInformation extends javax.swing.JPanel implements MultiViewElement, Serializable {

    private JToolBar toolBar = new JToolBar();
    private transient MultiViewElementCallback callback;
    private TopologyFileTypeDataObject obj;

    public TopologyInformation(TopologyFileTypeDataObject obj) {
        this.obj = obj;
        initComponents();
        jTextField1.setText(obj.getLoadSettings().getName());
        jTextArea1.setText(obj.getLoadSettings().getDescription());
        if (obj.getLoadSettings().getG() != null) {//topology was loaded
            jLabel1.setText(NbBundle.getMessage(TopologyInformation.class, "number_of_nodes") + ": " + obj.getLoadSettings().getVFactory().getAllVertices().size());
        }
        initDocumentListeners();
    }

    /**
     * inits document listeners to listen for changes in name/description
     * textfield/area the problem is that when keyTyped() method is used, I
     * encountered Swing bug #4140413
     */
    private void initDocumentListeners() {
        DocumentListenerImpl documentListenerImpl = new DocumentListenerImpl();
        jTextArea1.getDocument().addDocumentListener(documentListenerImpl);
        jTextField1.getDocument().addDocumentListener(documentListenerImpl);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel2.setText(org.openide.util.NbBundle.getMessage(TopologyInformation.class, "TopologyInformation.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        jTextField1.setText(org.openide.util.NbBundle.getMessage(TopologyInformation.class, "TopologyInformation.jTextField1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 311;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextField1, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(TopologyInformation.class, "TopologyInformation.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(jLabel3, gridBagConstraints);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 389;
        gridBagConstraints.ipady = 156;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 68);
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TopologyInformation.class, "TopologyInformation.jPanel1.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TopologyInformation.class, "TopologyInformation.jLabel1.text")); // NOI18N
        jPanel1.add(jLabel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.ipadx = 202;
        gridBagConstraints.ipady = 77;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 12, 12, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolBar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
        checkDocumentListeners((PlainDocument) jTextArea1.getDocument());
        checkDocumentListeners((PlainDocument) jTextField1.getDocument());
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    /**
     * There is a problem when this TopComponent is opened and user closes qSim.
     * That means that all opened TopComponents are persisted so that when qSim
     * is later re-started, these TopComponent are opened again. However I've
     * noticed that my DocumentListeners associated with JTextArea and
     * JTextField are not persisted. So in this method I check if
     * DocumentListeners are registered (if not that means qSim re-opened this
     * TopComponent). I would say it is Netbeans RCP's bug, but how can it be,
     * right? It is a fundamental feature that is used all across Netbeans IDE.
     *
     * @param plainDocument
     */
    private void checkDocumentListeners(PlainDocument plainDocument) {
        DocumentListenerImpl[] listeners = plainDocument.getListeners(DocumentListenerImpl.class);
        if (listeners.length == 0) {//no document listeners of my type are yet registered
            initDocumentListeners();
        }
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        if (obj == null) {
            return;
        }
        if (obj.isDirty()) {
            callback.updateTitle(obj.getPrimaryFile().getNameExt() + "*");
        } else {
            callback.updateTitle(obj.getPrimaryFile().getNameExt());
        }
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private class DocumentListenerImpl implements DocumentListener {

        public DocumentListenerImpl() {
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            obj.modifiedInformation(callback.getTopComponent(), jTextField1.getText(), jTextArea1.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            obj.modifiedInformation(callback.getTopComponent(), jTextField1.getText(), jTextArea1.getText());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            obj.modifiedInformation(callback.getTopComponent(), jTextField1.getText(), jTextArea1.getText());
        }
    }
}
