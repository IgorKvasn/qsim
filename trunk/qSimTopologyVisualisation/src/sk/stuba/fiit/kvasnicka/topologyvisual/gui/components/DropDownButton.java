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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXSearchField;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted.VertexDeletedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.dropdownevent.DropDownHiddenEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.dropdownevent.DropDownHiddenListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;

/**
 *
 * @author Igor Kvasnicka
 */
public class DropDownButton extends JButton implements ActionListener {

    private JPopupMenu popup = new JPopupMenu();
    private List<JCheckBox> checkBoxMenuItems = new LinkedList<JCheckBox>();
    private JXSearchField searchField;
    private JLabel emptyLabel = new JLabel("No results");
    private JPanel mainPanel = new JPanel();
    private JPanel checkPanel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane();
    private boolean firstTime = true;
    private boolean isSelected = false;
    private GeneralPath arrow;

    public DropDownButton() {
        init(true);
    }

    public DropDownButton(boolean searchFieldEnabled) {
        init(searchFieldEnabled);
    }

    private void init(boolean searchFieldEnabled) {

        setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/arrow_down.gif"));
        setHorizontalTextPosition(JButton.LEFT);
        setFocusPainted(false);
        addActionListener(this);
        mainPanel.setLayout(new BorderLayout());


        JPanel northPanel = new JPanel(new BorderLayout());
        if (searchFieldEnabled) {
            addSearchField(northPanel);
        }
        final JCheckBox chSelectAll = new JCheckBox("All");
        chSelectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(chSelectAll.isSelected());
            }
        });
        chSelectAll.setAlignmentX(LEFT_ALIGNMENT);

        northPanel.add(chSelectAll, BorderLayout.CENTER);
        northPanel.setAlignmentX(LEFT_ALIGNMENT);
        northPanel.setBackground(Color.red);
        mainPanel.add(northPanel, BorderLayout.NORTH);


        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.PAGE_AXIS));
        scrollPane.setViewportView(checkPanel);
        scrollPane.setPreferredSize(new Dimension(200, 100));

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        popup.add(mainPanel);

        popup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                fireDropDownHiddenEvent(new DropDownHiddenEvent(this));
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

    }

    private void addSearchField(JPanel northPanel) {
        searchField = new JXSearchField("Filter");
        searchField.setColumns(7);
        searchField.setAlignmentX(LEFT_ALIGNMENT);
        searchField.setSearchMode(JXSearchField.SearchMode.INSTANT);
        searchField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filterCheckBoxList(e.getActionCommand());
            }
        });
        northPanel.add(searchField, BorderLayout.LINE_START);
    }

    /**
     * selects all items
     *
     * @param select boolean value to select items to
     */
    public void selectAll(boolean select) {
        for (JCheckBox item : checkBoxMenuItems) {
            item.setSelected(select);
        }
    }

    private void filterCheckBoxList(String text) {
        checkPanel.remove(emptyLabel);

        if (StringUtils.isEmpty(text)) {
            for (JCheckBox item : checkBoxMenuItems) {
                checkPanel.remove(item);//removes item, so no duplicate will occure
                checkPanel.add(item);//add new item
            }

            mainPanel.revalidate();
            mainPanel.repaint();

            popup.revalidate();
            popup.repaint();

            return;
        }
        boolean empty = true;
        for (JCheckBox item : checkBoxMenuItems) {
            checkPanel.remove(item);//removes item, so no duplicate will occure

            if (item.getText().toLowerCase().contains(text.toLowerCase())) {
                empty = false;
                checkPanel.add(item);//add new item
            }
        }
        if (empty) {
            checkPanel.add(emptyLabel);
        }


        mainPanel.revalidate();
        mainPanel.repaint();

        popup.revalidate();
        popup.repaint();
    }

    /**
     * returns labels of all selected JCheckBoxMenuItem
     *
     * @return
     */
    public List<String> getSelectedCheckBoxItems() {
        List<String> list = new LinkedList<String>();
        for (JCheckBox item : checkBoxMenuItems) {
            if (item.isSelected()) {
                list.add(item.getText());
            }
        }
        return list;
    }

    /**
     * adds new JCheckBoxMenuItem to the popup menu
     *
     * @param label
     * @param selected should it be selected by default?
     */
    public void addCheckBoxMenuItem(String label, boolean selected) {
        JCheckBox chbox = new JCheckBox(label);
        chbox.setSelected(selected);
        checkPanel.add(chbox);
        checkBoxMenuItems.add(chbox);
    }

    public void removeCheckBoxMenuItem(TopologyVertex deletedVertex) {
        for (JCheckBox check : checkBoxMenuItems) {
            if (check.getText().equals(deletedVertex.getName())) {
                checkPanel.remove(check);
                return;
            }
        }
        throw new IllegalStateException("unknown checkbox to delete: " + deletedVertex.getName());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        popup.show(this, 0, getHeight());
    }

    public class StayOpenCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {

        @Override
        protected void doClick(MenuSelectionManager msm) {
            menuItem.doClick(0);
        }
    }

    public void addDropDownHiddenListener(DropDownHiddenListener listener) {
        listenerList.add(DropDownHiddenListener.class, listener);
    }

    public void removeDropDownHiddenListener(DropDownHiddenListener listener) {
        listenerList.remove(DropDownHiddenListener.class, listener);
    }

    private void fireDropDownHiddenEvent(DropDownHiddenEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == DropDownHiddenListener.class) {
                ((DropDownHiddenListener) listeners[i + 1]).dropDownHiddenOccurred(evt);
            }
        }
    }
}
