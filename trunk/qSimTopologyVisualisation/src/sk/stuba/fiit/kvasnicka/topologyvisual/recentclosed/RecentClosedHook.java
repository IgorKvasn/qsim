/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.recentclosed;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import javax.sound.midi.Receiver;
import org.apache.log4j.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;

/**
 * This class is based on Geertjan's blog post
 * https://blogs.oracle.com/geertjan/entry/org_netbeans_spi_project_ui
 *
 *
 * thanks a lot
 *
 * @author Igor Kvasnicka
 */
@LookupProvider.Registration(projectType = {
    "sk-stuba-fiit-kvasnicka-qsimproject"
})
public class RecentClosedHook implements LookupProvider {

    private static Logger logg = Logger.getLogger(RecentClosedHook.class);

    @Override
    public Lookup createAdditionalLookup(final Lookup lookup) {

        Project p = lookup.lookup(Project.class);
        final String name = p.getProjectDirectory().getName();
        final String path = p.getProjectDirectory().getPath();

        return Lookups.fixed(new ProjectOpenedHook() {
            @Override
            protected void projectOpened() {
            }

            @Override
            protected void projectClosed() {
                try {
                    addToRecentlyClosed(path, name);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }

                NotificationDisplayer.getDefault().notify(
                        //display name:
                        "Opened " + name + "\npath: " + path,
                        //icon:
                        null,
                        //description:
                        "Search Google for " + name + "!",
                        //action listener:
                        new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String searchText = URLEncoder.encode(name, "UTF-8");
                            URLDisplayer.getDefault().showURL(
                                    new URL("http://www.google.com/search?hl=en&q="
                                    + searchText + "&btnG=Google+Search"));
                        } catch (Exception eee) {
                            return;//nothing much to do
                        }

                    }
                });
            }
        });
    }

    private void addToRecentlyClosed(String projectPath, String projectName) throws IOException, ClassNotFoundException {
        String recently = PreferenciesHelper.loadRecentlyClosed();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(recently.getBytes()));
        HashSet<ClosedProject> set = (HashSet<ClosedProject>) ois.readObject();
        ois.close();

        ClosedProject project = new ClosedProject(projectName, projectPath);
        set.add(project);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(set);
        oos.close();
        String pref = new String(baos.toByteArray());

        PreferenciesHelper.saveRecentlyClosed(pref);
    }
}
