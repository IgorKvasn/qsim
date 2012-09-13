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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology;

import org.jdesktop.swingx.JXLabel;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.text.NumberFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.BlockingDialog;

/**
 * provides dialog for initial configuration of edge
 *
 * @author Igor Kvasnicka
 */
public class EdgeConfigurationDialog extends BlockingDialog<EdgeConfigurationDialog.ResultObject> {

    private JFormattedTextField txtSpeed;
    private JFormattedTextField txtLength;

    /**
     * creates new instance of dialog
     *
     * @param edge Edge object that is being configured; this object is
     * read-only
     */
    public EdgeConfigurationDialog(NetworkNode src, NetworkNode dest, long defaultSpeed) {
        super(null);
        setTitle(NbBundle.getMessage(EdgeConfigurationDialog.class, "create.new.edge"));

        initGui();
        txtSpeed.setValue(defaultSpeed);
    }

    private void initGui() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));
        add(centerPanel, BorderLayout.CENTER);

        JPanel speedPanel = new JPanel();

        DecimalFormat decimalFormat = new DecimalFormat("#");
        NumberFormatter numberFormatter = new NumberFormatter(decimalFormat);
        txtSpeed = new JFormattedTextField(numberFormatter);
        txtSpeed.setColumns(10);
        txtSpeed.setToolTipText(NbBundle.getMessage(EdgeConfigurationDialog.class, "bitrate.must.be.decimal.number"));

        JXLabel lblSpeed = new JXLabel(NbBundle.getMessage(EdgeConfigurationDialog.class, "bitrate.bit.s"));
        lblSpeed.setLabelFor(txtSpeed);
        speedPanel.add(lblSpeed);
        speedPanel.add(txtSpeed);
        centerPanel.add(speedPanel);


        JPanel lengthPanel = new JPanel();
        txtLength = new JFormattedTextField(numberFormatter);
        txtLength.setColumns(10);
        txtLength.setToolTipText(NbBundle.getMessage(EdgeConfigurationDialog.class, "length.must.be.decimal.number"));

        JXLabel lblLength = new JXLabel(NbBundle.getMessage(EdgeConfigurationDialog.class, "length.m"));
        lblLength.setLabelFor(txtLength);
        lengthPanel.add(lblLength);
        lengthPanel.add(txtLength);
        centerPanel.add(lengthPanel);


        JPanel bottomPanel = new JPanel();
        JButton btnOk = new JButton(NbBundle.getMessage(EdgeConfigurationDialog.class, "ok"));
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!validateInput()) {
                    return;
                }

//                ResultObject resultObject = new ResultObject(Long.valueOf(txtSpeed.getText()), Integer.valueOf(txtLength.getText()));
                throw new UnsupportedOperationException("not yet implemented"); //todo uncoment and fix code above and below
//                setUserInput(resultObject);
//                closeDialog();
            }
        });
        JButton btnCancel = new JButton(NbBundle.getMessage(EdgeConfigurationDialog.class, "cancel"));
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUserInput(null);
                closeDialog();
            }
        });
        bottomPanel.add(btnOk);
        bottomPanel.add(btnCancel);


        add(bottomPanel, BorderLayout.PAGE_END);
        pack();
    }

    private boolean validateInput() {
        boolean ok = true;
        if (!StringUtils.isNumeric(txtSpeed.getText()) || StringUtils.isEmpty(txtSpeed.getText())) { //speed is numeric and non-empty
            txtSpeed.setBackground(new Color(249, 77, 77));
            ok = false;
        } else {
            if (Long.valueOf(txtSpeed.getText()) <= 0) {//speed is not negative nor zero
                txtSpeed.setBackground(new Color(249, 77, 77));
                ok = false;
            } else {
                txtSpeed.setBackground(UIManager.getColor("JFormattedTextField.background"));
            }
        }

        if (!StringUtils.isNumeric(txtLength.getText()) || StringUtils.isEmpty(txtLength.getText())) { //length is numeric and non-empty
            txtLength.setBackground(new Color(249, 77, 77));
            ok = false;
        } else {
            if (Long.valueOf(txtLength.getText()) <= 0) {//length is not negative nor zero
                txtLength.setBackground(new Color(249, 77, 77));
                ok = false;
            } else {
                txtLength.setBackground(UIManager.getColor("JFormattedTextField.background"));
            }
        }


        return ok;
    }

    @Getter
    public class ResultObject {

        private long speed;
        private int length;
        private int mtu;
        private double packetErrorRate;

        public ResultObject(long speed, int length, int mtu, double packetErrorRate) {
            this.speed = speed;
            this.length = length;
            this.mtu = mtu;
            this.packetErrorRate = packetErrorRate;
        }
    }
}
