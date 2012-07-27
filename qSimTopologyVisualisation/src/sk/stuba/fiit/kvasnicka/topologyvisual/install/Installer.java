/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.install;

import java.util.Collection;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.RunningSimulationManager;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
    }

    @Override
    public boolean closing() {
        if (RunningSimulationManager.getInstance().getRunningList().isEmpty()) {//there are no running simulations
            return true;
        }
        //there is at least one simulation that is still running
        NotifyDescriptor nd = new NotifyDescriptor(
                "<html>There are some simulations that are still running. Are you sure you want to exit?<br><ul>" + createList(RunningSimulationManager.getInstance().getRunningList()) + "</ul>",
                "Title",
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null,
                NotifyDescriptor.YES_OPTION);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            return true;
        }
        return false;
    }

    private String createList(Collection<String> col) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s : col) {
            if (i == col.size() - 1) {
                sb.append("<li>").append(s).append("</li>");
                break;
            }
            i++;
            sb.append("<li>").append(s).append("</li>").append(", ");
        }
        return sb.toString();
    }
}
