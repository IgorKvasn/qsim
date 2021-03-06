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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;

/**
 * @author Igor Kvasnicka
 */
public class TopologyPalette extends JPanel {

    private JXTaskPaneContainer taskpaneContainer = new JXTaskPaneContainer();
    private Map<String, JXTaskPane> categories = new LinkedHashMap<String, JXTaskPane>();
    private JToggleButton selected = null;

    public TopologyPalette() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(taskpaneContainer);
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void clearSelection() {
        if (isSelectedButton()) {
            selected.setSelected(false);
            selected = null;
        }
    }

    public boolean isSelectedButton() {
        return selected != null;
    }

    public JToggleButton getSelected() {
        return selected;
    }

    public void addCategory(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("category name is empty or NULL");
        }
        if (categories.containsKey(name)) {
            throw new IllegalArgumentException("category \"" + name + "\" already exists");
        }

        JXTaskPane pane = new JXTaskPane();
        pane.setFocusable(false);
        pane.setTitle(name);
        pane.setAnimated(true);
        categories.put(name, pane);
        taskpaneContainer.add(pane);
    }

    public void addChild(String category, String name, final PaletteActionEnum palSelEnum) {
        if (StringUtils.isEmpty(category)) {
            throw new IllegalArgumentException("category name is empty or NULL");
        }


        if (!categories.containsKey(category)) {
            addCategory(category);
        }
        JXTaskPane taskPane = categories.get(category);
        final JToggleButton btn = new JToggleButton(name);
        taskPane.add(btn);
        btn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSelectedButton()) {
                    selected.setSelected(false);
                }
                selected = btn;
                if (selected.isSelected()) {
                    firePaletteSelectedOccurred(new PaletteSelectionEvent(this, palSelEnum));
                } else {
                    firePaletteDeselectedOccurred(new PaletteSelectionEvent(this, palSelEnum));
                }
            }
        });
    }

    /**
     * deletes content of the category, not the category itself
     */
    public void clearCategory(String category) {
        if (StringUtils.isEmpty(category)) {
            throw new IllegalArgumentException("category name is empty or NULL");
        }
        if (!categories.containsKey(category)) {
            throw new IllegalArgumentException("category \"" + category + "\" does not exists");
        }
        JXTaskPane taskPane = categories.get(category);
        taskPane.removeAll();
        taskPane.revalidate();
        if (isSelectedButton()) {
            selected.setSelected(false);
            selected = null;
        }
    }
    // private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    public void addPaletteSelectionListener(PaletteSelectionListener listener) {
        listenerList.add(PaletteSelectionListener.class, listener);
    }

    public void removePaletteSelectionListener(PaletteSelectionListener listener) {
        listenerList.remove(PaletteSelectionListener.class, listener);
    }

    private synchronized void firePaletteSelectedOccurred(PaletteSelectionEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == PaletteSelectionListener.class) {
                ((PaletteSelectionListener) listeners[i + 1]).paletteSelectedOccurred(evt);
            }
        }
    }

    private synchronized void firePaletteDeselectedOccurred(PaletteSelectionEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == PaletteSelectionListener.class) {
                ((PaletteSelectionListener) listeners[i + 1]).paletteDeselectedOccurred(evt);
            }
        }
    }

    /**
     * disables/enables all buttons in all categories
     */
    public void setEnabledButtons(boolean enable) {
        for (JXTaskPane cat : categories.values()) {
            for (Component comp : cat.getComponents()) {
                if (comp instanceof JXCollapsiblePane) {
                    for (Component c : ((JXCollapsiblePane) comp).getContentPane().getComponents()) {
                        if (c instanceof JToggleButton) {
                            c.setEnabled(enable);
                        }
                    }
                }
            }

        }
    }
}
