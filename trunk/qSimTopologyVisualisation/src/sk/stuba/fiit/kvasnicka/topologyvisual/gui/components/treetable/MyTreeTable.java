package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.treetable;

/**
 * *****************************************************************************
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * qSim is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * qSim. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
/**
 * @author Igor Kvasnicka
 */
import javax.swing.JTable;
import java.awt.Dimension;
import javax.swing.table.TableModel;

public class MyTreeTable extends JTable {

    private MyTreeTableCellRenderer tree;

    public MyTreeTable(MyAbstractTreeTableModel treeTableModel) {
        super();

        tree = new MyTreeTableCellRenderer(this, treeTableModel);
        tree.setRootVisible(false);

        super.setModel(new MyTreeTableModelAdapter(treeTableModel, tree));

        MyTreeTableSelectionModel selectionModel = new MyTreeTableSelectionModel();
        tree.setSelectionModel(selectionModel); //For the tree
        setSelectionModel(selectionModel.getListSelectionModel()); //For the table
        tree.setShowsRootHandles(true);

        setDefaultRenderer(MyTreeTableModel.class, tree);
        setDefaultEditor(MyTreeTableModel.class, new MyTreeTableCellEditor(tree, this));

        setShowGrid(false);

        setIntercellSpacing(new Dimension(0, 0));

    }

    public MyTreeTableCellRenderer getTree() {
        return tree;
    }

    public void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
}