/*******************************************************************************
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
 ******************************************************************************/

package sk.stuba.fiit.kvasnicka.qsimsimulation.helpers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimLog;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public abstract class ReflectionHelper {
    private static Logger logg = Logger.getLogger(ReflectionHelper.class);

    /**
     * injects simulationLogUtils object into all NetworkNodes
     *
     * @param nodeList
     * @param simulationLogUtils
     */
    public static void initSimulLog(List<NetworkNode> nodeList, SimulationLogUtils simulationLogUtils) {
        if (nodeList == null) throw new IllegalArgumentException("nodeList is NULL");
        if (simulationLogUtils == null) throw new IllegalArgumentException("cannot inject NULL");
        for (NetworkNode node : nodeList) {
            initSimulLog(node, simulationLogUtils);
        }
    }

    /**
     * injects simulationLogUtils object into  NetworkNode object
     *
     * @param node
     * @param simulationLogUtils
     */
    public static void initSimulLog(NetworkNode node, SimulationLogUtils simulationLogUtils) {
        if (node == null) throw new IllegalArgumentException("node is NULL");
        if (simulationLogUtils == null) throw new IllegalArgumentException("cannot inject NULL");

        Class d = NetworkNode.class;

        Field fs[] = d.getDeclaredFields();
        for (Field f : fs) {
            Annotation a = f.getAnnotation(SimLog.class);
            if (a != null) {
                try {
                    setWithoutSetter(NetworkNode.class, node, f.getName(), simulationLogUtils);
                } catch (NoSuchFieldException e) {
                    logg.error(e);
                } catch (IllegalAccessException e) {
                    logg.error(e);
                }
            }
        }
    }


    /**
     * set private field without setter
     *
     * @param c
     * @param o
     * @param field
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void setWithoutSetter(Class c, Object o, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = null;
        f = c.getDeclaredField(field);
        f.setAccessible(true);
        f.set(o, value);
    }
}
