/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.recentclosed;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;

@ActionID(
    category = "File",
id = "sk.stuba.fiit.kvasnicka.topologyvisual.recentclosed.OpenRecentProjectAction")
@ActionRegistration(
    displayName = "#CTL_OpenRecentProjectAction")
@ActionReference(path = "Menu/File", position = 650)
@Messages("CTL_OpenRecentProjectAction=Open recent projects")
public final class OpenRecentProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("DOKONCI");
    }
}
