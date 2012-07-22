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

package sk.stuba.fiit.kvasnicka;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.ReflectionHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Igor Kvasnicka
 */
public abstract class TestUtils {
    public static void setWithoutSetter(Class c, Object o, String field, Object value) {

        Field f = null;
        try {
            f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(o, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getPropertyWithoutGetter(Class klass, Object bean, String field) {
        Field f = null;
        try {
            f = klass.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(bean);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object callPrivateMethod(Class clazz, Object instance, String methodName, Class[] parameterTypes, Object[] parameters) {
        Method privateStringMethod = null;
        try {
            privateStringMethod = clazz.getDeclaredMethod(methodName, parameterTypes);
            privateStringMethod.setAccessible(true);
            return privateStringMethod.invoke(instance, parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("unable to call method");
    }

    /**
     * injects simulation log into network node
     */
    public static void initNetworkNode(NetworkNode networkNode, SimulationLogUtils simulationLogUtils) {
        ReflectionHelper.initSimulLog(networkNode, simulationLogUtils);
    }
}
