/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.recentclosed;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

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
}
