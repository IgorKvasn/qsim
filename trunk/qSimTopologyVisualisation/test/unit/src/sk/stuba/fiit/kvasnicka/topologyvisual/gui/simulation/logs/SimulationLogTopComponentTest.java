/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTable;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogSource;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLog;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationLogTopComponentTest {

    private SimulationLogTopComponent comp;

    @Before
    public void before() {
        comp = new SimulationLogTopComponent();
    }

    @Test
    public void testCreateRegex() throws Exception {
        List<String> list = Arrays.asList("hello", "world");

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("createRegex", List.class);
        privateStringMethod.setAccessible(true);

        String returnValue = (String) privateStringMethod.invoke(comp, list);

        Assert.assertEquals("(.*hello.*)|(.*world.*)", returnValue);
    }

    @Test
    public void testCreateRegex_single() throws Exception {
        List<String> list = Arrays.asList("hello");

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("createRegex", List.class);
        privateStringMethod.setAccessible(true);

        String returnValue = (String) privateStringMethod.invoke(comp, list);

        Assert.assertEquals("(.*hello.*)", returnValue);
    }

    @Test
    public void testCreateRegex_empty() throws Exception {
        List<String> list = new LinkedList<String>();

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("createRegex", List.class);
        privateStringMethod.setAccessible(true);

        String returnValue = (String) privateStringMethod.invoke(comp, list);

        Assert.assertEquals("", returnValue);
    }

    @Test
    public void testLogTable_add_bottom() throws Exception {
        PreferenciesHelper.setSimulationLogsCount(2);
        PreferenciesHelper.setAddNewSimulationLogsAtBottom(true);

        comp.simulationLogOccurred(new SimulationLogEvent(this, new SimulationLog(LogCategory.ERROR, "cause1", "sourceName", LogSource.VERTEX, 0)));
        comp.simulationLogOccurred(new SimulationLogEvent(this, new SimulationLog(LogCategory.ERROR, "cause2", "sourceName", LogSource.VERTEX, 1)));

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("getSimulationLogPanel", String.class);
        privateStringMethod.setAccessible(true);
        JTable returnValue = (JTable) privateStringMethod.invoke(comp, "sourceName");

        Assert.assertNotNull(returnValue);
        Assert.assertEquals(2, returnValue.getRowCount());
        Assert.assertEquals(1, returnValue.getValueAt(0, 0));
        Assert.assertEquals(2, returnValue.getValueAt(1, 0));

        comp.simulationLogOccurred(new SimulationLogEvent(this, new SimulationLog(LogCategory.ERROR, "cause3", "sourceName", LogSource.VERTEX, 2)));
        JTable returnValue2 = (JTable) privateStringMethod.invoke(comp, "sourceName");
        
        Assert.assertNotNull(returnValue2);
        Assert.assertEquals(2, returnValue2.getRowCount());
        Assert.assertEquals(2, returnValue2.getValueAt(0, 0));
        Assert.assertEquals(3, returnValue2.getValueAt(1, 0));
    }
    
    @Test
    public void testLogTable_add_top() throws Exception {
        PreferenciesHelper.setSimulationLogsCount(2);
        PreferenciesHelper.setAddNewSimulationLogsAtBottom(false);

        comp.simulationLogOccurred(new SimulationLogEvent(this, new SimulationLog(LogCategory.ERROR, "cause1", "sourceName", LogSource.VERTEX, 0)));
        comp.simulationLogOccurred(new SimulationLogEvent(this, new SimulationLog(LogCategory.ERROR, "cause2", "sourceName", LogSource.VERTEX, 1)));

        Method privateStringMethod = SimulationLogTopComponent.class.getDeclaredMethod("getSimulationLogPanel", String.class);
        privateStringMethod.setAccessible(true);
        JTable returnValue = (JTable) privateStringMethod.invoke(comp, "sourceName");

        Assert.assertNotNull(returnValue);
        Assert.assertEquals(2, returnValue.getRowCount());
        Assert.assertEquals(2, returnValue.getValueAt(0, 0));
        Assert.assertEquals(1, returnValue.getValueAt(1, 0));

        comp.simulationLogOccurred(new SimulationLogEvent(this, new SimulationLog(LogCategory.ERROR, "cause3", "sourceName", LogSource.VERTEX, 2)));
        JTable returnValue2 = (JTable) privateStringMethod.invoke(comp, "sourceName");
        
        Assert.assertNotNull(returnValue2);
        Assert.assertEquals(2, returnValue2.getRowCount());
        Assert.assertEquals(3, returnValue2.getValueAt(0, 0));
        Assert.assertEquals(2, returnValue2.getValueAt(1, 0));
    }
}
