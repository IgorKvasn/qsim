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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels;

import javax.swing.JPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;

/**
 * All wizard's panels must extends this interface
 *
 * @author Igor Kvasnicka
 */
public abstract class PanelInterface extends JPanel {

    /**
     * initializes default values
     *
     * @return false if there was a problem during initialisation
     */
    public abstract boolean init(SimulationRuleIterator iterator);

    /**
     * validation of user data
     *
     * @return false if there was a validation problem
     */
    public abstract boolean validateData();

    /**
     * initialises values of the panel according to Data object please note,
     * that fields in the Data object may be null, but Data object itself is
     * never null
     *
     * @param data
     */
    public abstract void initValues(SimulationData.Data data);
}
