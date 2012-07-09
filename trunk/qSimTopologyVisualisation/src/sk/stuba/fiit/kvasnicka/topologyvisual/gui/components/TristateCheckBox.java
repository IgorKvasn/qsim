/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;

/**
 *
 * @author Igor Kvasnicka
 */
public class TristateCheckBox extends JCheckBox {

    public enum State {

        NOT_SELECTED, CHECKED, CROSSED
    }
    private final TristateModel model;

    public TristateCheckBox(String text, State initial) {
        super(text);

        Icon icon = new TristateCheckBoxIcon();
        super.setIcon(icon);

// Add a listener for when the mouse is pressed and released
        super.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                TristateCheckBox.this.mousePressed();
            }

            public void mouseReleased(MouseEvent e) {
                TristateCheckBox.this.mouseReleased();
            }
        });
// Reset the keyboard action map
        ActionMap map = new ActionMapUIResource();
        map.put("pressed", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                TristateCheckBox.this.mousePressed();
            }
        });
        map.put("released", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                TristateCheckBox.this.mouseReleased();
            }
        });
        SwingUtilities.replaceUIActionMap(this, map);
// set the model to the adapted model
        model = new TristateModel(getModel());
        setModel(model);
        setState(initial);
    }

    private void mousePressed() {
        grabFocus();
        model.setPressed(true);
        model.setArmed(true);
    }

    private void mouseReleased() {
        model.nextState();
        model.setArmed(false);
        model.setPressed(false);
    }

    public void doClick() {
        mousePressed();
        mouseReleased();
    }

    public TristateCheckBox(String text) {
        this(text, State.NOT_SELECTED);
    }

    public TristateCheckBox() {
        this(null);
    }

    /**
     * No one may add mouse listeners, not even Swing!
     */
    public void addMouseListener(MouseListener l) {
    }

    /**
     * No one may set a new icon
     */
    public void setIcon(Icon icon) {
    }

    /**
     * Set the new state to either State.CHECKED, State.CROSSED or
     * State.NOT_SELECTED.
     */
    public void setState(State state) {
        model.setState(state);
    }

    /**
     * Return the current state, which is determined by the selection status of
     * the model.
     */
    public State getState() {
        return model.getState();
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setState(State.CHECKED);
        } else {
            setState(State.NOT_SELECTED);
        }
    }

    private class TristateModel implements ButtonModel {

        private final ButtonModel other;
        private State currentState = State.NOT_SELECTED;

        private TristateModel(ButtonModel other) {
            this.other = other;
        }

        private State getState() {
            return currentState;
        }

        private void setState(State state) {
            this.currentState = state;
        }

        public boolean isSelected() {
            return (currentState == State.CHECKED || currentState == State.CROSSED);
        }

        /**
         * We rotate between State.NOT_SELECTED, State.CHECKED and
         * State.CROSSED.
         */
        private void nextState() {
            State current = getState();
            if (current == State.NOT_SELECTED) {
                setState(State.CHECKED);
            } else {
                if (current == State.CHECKED) {
                    setState(State.CROSSED);
                } else {
                    if (current == State.CROSSED) {
                        setState(State.NOT_SELECTED);
                    }
                }
            }

//This is to enforce a call to the fireStateChanged method
            other.setSelected(!other.isSelected());
        }

        public void setArmed(boolean b) {
            other.setArmed(b);
        }

        /**
         * We disable focusing on the component when it is not enabled.
         */
        public void setEnabled(boolean b) {
            try {
                setFocusable(b);
            } catch (Exception ex) {
                ex.printStackTrace();
            }//catch

            other.setEnabled(b);
        }

        /**
         * All these methods simply delegate to the "other" model that is being
         * decorated.
         */
        public boolean isArmed() {
            return other.isArmed();
        }

        /*
         * public boolean isSelected() { return other.isSelected(); }
         */
        public boolean isEnabled() {
            return other.isEnabled();
        }

        public boolean isPressed() {
            return other.isPressed();
        }

        public boolean isRollover() {
            return other.isRollover();
        }

        public void setSelected(boolean b) {
            other.setSelected(b);
        }

        public void setPressed(boolean b) {
            other.setPressed(b);
        }

        public void setRollover(boolean b) {
            other.setRollover(b);
        }

        public void setMnemonic(int key) {
            other.setMnemonic(key);
        }

        public int getMnemonic() {
            return other.getMnemonic();
        }

        public void setActionCommand(String s) {
            other.setActionCommand(s);
        }

        public String getActionCommand() {
            return other.getActionCommand();
        }

        public void setGroup(ButtonGroup group) {
            other.setGroup(group);
        }

        public void addActionListener(ActionListener l) {
            other.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            other.removeActionListener(l);
        }

        public void addItemListener(ItemListener l) {
            other.addItemListener(l);
        }

        public void removeItemListener(ItemListener l) {
            other.removeItemListener(l);
        }

        public void addChangeListener(ChangeListener l) {
            other.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            other.removeChangeListener(l);
        }

        public Object[] getSelectedObjects() {
            return other.getSelectedObjects();
        }
    }

    private class TristateCheckBoxIcon implements Icon, UIResource,
            Serializable {

        protected int getControlSize() {
            return 13;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            JCheckBox cb = (JCheckBox) c;
            TristateModel model = (TristateModel) cb.getModel();
            int controlSize = getControlSize();

            boolean drawCheck = model.getState() == State.CHECKED;
            boolean drawCross = model.getState() == State.CROSSED;

            if (model.isEnabled()) {
                if (model.isPressed() && model.isArmed()) {
                    g.setColor(MetalLookAndFeel.getControlShadow());
                    g.fillRect(x, y, controlSize - 1, controlSize - 1);
                    drawPressed3DBorder(g, x, y, controlSize, controlSize);
                } else {
                    drawFlush3DBorder(g, x, y, controlSize, controlSize);
                }
                g.setColor(MetalLookAndFeel.getControlInfo());
            } else {
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.drawRect(x, y, controlSize - 1, controlSize - 1);
            }

            if (drawCross) {
                drawCross(c, g, x, y);
            }

            if (drawCheck) {
                if (cb.isBorderPaintedFlat()) {
                    x++;
                }
                drawCheck(c, g, x, y);
            }
        }// paintIcon

        protected void drawCross(Component c, Graphics g, int x, int y) {
            int controlSize = getControlSize();
            g.drawLine(x + (controlSize - 4), y + 2, x + 3, y
                    + (controlSize - 5));
            g.drawLine(x + (controlSize - 4), y + 3, x + 3, y
                    + (controlSize - 4));
            g.drawLine(x + 3, y + 2, x + (controlSize - 4), y
                    + (controlSize - 5));
            g.drawLine(x + 3, y + 3, x + (controlSize - 4), y
                    + (controlSize - 4));
        }

        protected void drawCheck(Component c, Graphics g, int x, int y) {
            int controlSize = getControlSize();
            g.fillRect(x + 3, y + 5, 2, controlSize - 8);
            g.drawLine(x + (controlSize - 4), y + 3, x + 5, y
                    + (controlSize - 6));
            g.drawLine(x + (controlSize - 4), y + 4, x + 5, y
                    + (controlSize - 5));
        }

        private void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControlHighlight());
            g.drawRect(1, 1, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControl());
            g.drawLine(0, h - 1, 1, h - 2);
            g.drawLine(w - 1, 0, w - 2, 1);
            g.translate(-x, -y);
        }

        private void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            drawFlush3DBorder(g, 0, 0, w, h);
            g.setColor(MetalLookAndFeel.getControlShadow());
            g.drawLine(1, 1, 1, h - 2);
            g.drawLine(1, 1, w - 2, 1);
            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return getControlSize();
        }

        public int getIconHeight() {
            return getControlSize();
        }
    }
}
