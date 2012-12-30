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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;

public class MyDataNode {

    private static DecimalFormat twoDForm = new DecimalFormat("#.##");
    private String name;
    private double currentUsage;
    private int maxCapacity;
    private UsageStatistics usageStatistics;
    private List<MyDataNode> children = new ArrayList<MyDataNode>();
    private Boolean inChart;

    public MyDataNode(String name, int maxCapacity, UsageStatistics usageStatistics, boolean leaf) {
        if (leaf) {
            this.name = " - " + name;
        } else {
            this.name = name;
        }
        this.maxCapacity = maxCapacity;
        this.inChart = false;
        this.usageStatistics = usageStatistics;
    }

    public UsageStatistics getUsageStatistics() {
        return usageStatistics;
    }

    /**
     * formats usage to two decimal places
     *
     * @return
     */
    public String calculateUsage() {
        if (maxCapacity == 0) {//this prevents division by zero
            return String.valueOf(0);
        }
        return twoDForm.format(calculateUsageAsDouble());
    }

    public Double calculateUsageAsDouble() {
        if (maxCapacity == 0) {//this prevents division by zero
            return 0.0;
        }
        return (currentUsage * 100) / maxCapacity;
    }

    public Boolean isInChart() {
        return inChart;
    }

    public void setInChart(Boolean inChart) {
        this.inChart = inChart;
    }

    public double getCurrentUsage() {
        return currentUsage;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setCurrentUsage(double currentUsage) {
        this.currentUsage = currentUsage;
    }

    public String getName() {
        return name;
    }

    public List<MyDataNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return getName();
    }
}