/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.ExportException;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.Exportable;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.impl.JsonExport;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.impl.PdfExport;
import sk.stuba.fiit.kvasnicka.topologyvisual.export.impl.XmlExport;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.SimulationRulesExportBean;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulRuleExportDialog extends javax.swing.JDialog {

    private InputStream chartImage;
    private List<SimulationRulesExportBean> exportBreans;
    private JFileChooser fileChooser = new JFileChooser();

    /**
     * Creates new form SimulRuleExportDialog
     */
    public SimulRuleExportDialog() {
        super(WindowManager.getDefault().getMainWindow());
        initComponents();
        setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        jXTextField1.setPrompt("Path");
    }

    public void showDialog(InputStream chartImage, List<SimulationRulesExportBean> exportBreans) {
        this.chartImage = chartImage;
        this.exportBreans = exportBreans;
        lblExportCount.setText(String.valueOf(exportBreans.size()));
        jXTextField1.setText("");
        setVisible(true);
    }

    private void doExport() {
        if (StringUtils.isEmpty(jXTextField1.getText())) {
            JOptionPane.showMessageDialog(this, "Path is not specified.", "Export error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Exportable exportable;
        switch (getExportType()) {
            case PDF:
                exportable = new PdfExport();
                break;
            case XML:
                exportable = new XmlExport();
                break;
            case JSON:
                exportable = new JsonExport();
                break;
            default:
                throw new IllegalStateException("unknown export type");
        }
        OutputStream out = null;
        try {
            File file = new File(jXTextField1.getText());
            if (file.exists()) {
                int n = JOptionPane.showConfirmDialog(
                        this,
                        "This file already exists. Do you want to continue?",
                        "File already exists",
                        JOptionPane.YES_NO_OPTION);
                if (n != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            out = new FileOutputStream(file);
            exportable.serialize(exportBreans, chartImage, out);


            JOptionPane.showMessageDialog(this, "Export sucessfull.");
        } catch (ExportException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to save file.\nCause: " + e.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    //nothing to do anyway
                }
            }
        }

    }

    private ExportType getExportType() {
        switch (jComboBox1.getSelectedIndex()) {
            case 0:
                return ExportType.PDF;
            case 1:
                return ExportType.XML;
            case 2:
                return ExportType.JSON;
            default:
                throw new IllegalStateException("unknown export type in combobox");
        }

    }

    private void openFileDialog() {
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            jXTextField1.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
        
        //add file extension
        switch (getExportType()) {
            case PDF:
                jXTextField1.setText(jXTextField1.getText() + ".pdf");
                break;
            case XML:
                jXTextField1.setText(jXTextField1.getText() + ".xml");
                break;
            case JSON:
                jXTextField1.setText(jXTextField1.getText() + ".json");
                break;
            default:
                throw new IllegalStateException("unknown export type");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jXTextField1 = new org.jdesktop.swingx.JXTextField();
        jLabel3 = new javax.swing.JLabel();
        lblExportCount = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jLabel1.text")); // NOI18N

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PDF", "XML", "JSON" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jButton1.text")); // NOI18N
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jXTextField1.setText(org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jXTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblExportCount, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.lblExportCount.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SimulRuleExportDialog.class, "SimulRuleExportDialog.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblExportCount))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jXTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(45, 45, 45))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblExportCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jXTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        doExport();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        openFileDialog();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private org.jdesktop.swingx.JXTextField jXTextField1;
    private javax.swing.JLabel lblExportCount;
    // End of variables declaration//GEN-END:variables

    private enum ExportType {

        JSON(2), XML(1), PDF(0);
        private int comboboxIndex;

        private ExportType(int comboboxIndex) {
            this.comboboxIndex = comboboxIndex;
        }
    }
}
