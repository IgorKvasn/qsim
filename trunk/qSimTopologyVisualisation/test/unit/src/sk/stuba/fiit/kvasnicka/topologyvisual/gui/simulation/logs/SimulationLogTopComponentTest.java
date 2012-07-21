/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationLogTopComponentTest {

    @Test
    public void testCreateRegex() throws Exception {
        List<String> list = Arrays.asList("hello", "world");
        SimulationLogTopComponent comp = new SimulationLogTopComponent();

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("createRegex", List.class);
        privateStringMethod.setAccessible(true);

        String returnValue = (String) privateStringMethod.invoke(comp, list);

        Assert.assertEquals("(hello)|(world)", returnValue);
    }

    @Test
    public void testCreateRegex_single() throws Exception {
        List<String> list = Arrays.asList("hello");
        SimulationLogTopComponent comp = new SimulationLogTopComponent();

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("createRegex", List.class);
        privateStringMethod.setAccessible(true);

        String returnValue = (String) privateStringMethod.invoke(comp, list);

        Assert.assertEquals("(hello)", returnValue);
    }

    @Test
    public void testCreateRegex_empty() throws Exception {
        List<String> list = new LinkedList<String>();
        SimulationLogTopComponent comp = new SimulationLogTopComponent();

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("createRegex", List.class);
        privateStringMethod.setAccessible(true);

        String returnValue = (String) privateStringMethod.invoke(comp, list);

        Assert.assertEquals("", returnValue);
    }
}
