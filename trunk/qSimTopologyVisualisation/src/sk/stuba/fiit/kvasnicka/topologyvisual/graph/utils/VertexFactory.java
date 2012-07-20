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
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import org.apache.log4j.Logger;
import org.openide.util.NbBundle;

/**
 * @author Igor Kvasnicka
 */
public class VertexFactory {

    private static Logger logg = Logger.getLogger(VertexFactory.class);
    private int routerCount = 0;
    private int computerCount = 0;
    private int switchCount = 0;

    public VertexFactory() {
    }

    public VertexFactory(int routerCount, int switchCount, int computerCount) {
        this.routerCount = routerCount;
        this.switchCount = switchCount;
        this.computerCount = computerCount;
    }

    /**
     * creates a default name for a new router
     *
     * @return name of a new router
     */
    public String createRouterName() {
        routerCount++;
        return NbBundle.getMessage(VertexFactory.class, "router") + " #" + routerCount;
    }

    /**
     * creates a default name for a new switch
     *
     * @return name of a new switch
     */
    public String createSwitchName() {
        switchCount++;
        return NbBundle.getMessage(VertexFactory.class, "switch") + " #" + switchCount;
    }

    /**
     * creates a default name for a new computer
     *
     * @return name of a new computer
     */
    public String createComputerName() {
        computerCount++;
        return NbBundle.getMessage(VertexFactory.class, "computer") + " #" + computerCount;
    }

    /**
     * decrements counter of computers in topology by 1
     */
    public void decrementNumberOfComputers() {
        computerCount--;
        if (computerCount < 0) {
            computerCount = 0;
            logg.error("Computer count was negative - something is wrong");
        }
    }

    /**
     * decrements counter of routers in topology by 1
     */
    public void decrementNumberOfRouters() {
        routerCount--;
        if (routerCount < 0) {
            routerCount = 0;
            logg.error("Router count was negative - something is wrong");
        }
    }

    /**
     * decrements counter of routers in topology by 1
     */
    public void decrementNumberOfSwitches() {
        switchCount--;
        if (switchCount < 0) {
            switchCount = 0;
            logg.error("Switch count was negative - something is wrong");
        }
    }
}
