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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * BlockingDialog blocks flow of the program and waits until user closes dialog
 * It is used to wait for user's input
 *
 * @author Igor Kvasnicka
 */
public abstract class BlockingDialog<R> extends JDialog {

    /**
     * here is stored user input
     */
    private R userInput;

    /**
     * creates new instance of JDialog this dialog cannot be closed by clicking
     * on "close" button in upper bar - so do not forget to create your own
     * Close/Cancel button
     *
     * @param owner owner of this dialog
     */
    protected BlockingDialog(Frame owner) {
        super(owner, true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                cancelDialog();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
    }

    /**
     * shows dialog - blocking operation
     */
    public void showDialog() {
        setLocationRelativeTo(super.getOwner());

        showDialogHook();

        setVisible(true);
    }

    /**
     * hook method that is called just <b>before</b> dialog is shown
     */
    public abstract void showDialogHook();

    /**
     * method that returns user input data
     *
     * @return any object according to implementation
     */
    public R getUserInput() {
        return userInput;
    }

    /**
     * sets userInput property this property will be returned as a result of
     * this dialog
     *
     * @param userInput user input
     */
    protected void setUserInput(R userInput) {
        this.userInput = userInput;
    }

    /**
     * closes dialog after <b>NOTE:</b> this method should be called after the
     * <i>userInput</i> property is being set
     */
    protected void closeDialog() {
        this.setVisible(false);
        this.dispose();
    }

    /**
     * user presses cancel button
     */
    protected void cancelDialog() {
        userInput = null;
        closeDialog();
    }
}
