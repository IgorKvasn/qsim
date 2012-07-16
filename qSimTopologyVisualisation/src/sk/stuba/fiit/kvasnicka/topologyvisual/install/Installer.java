/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.install;

import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;

public class Installer extends ModuleInstall implements LookupListener {

    Lookup.Result<TopologyFileTypeDataObject> result;  //result object is weakly referenced inside Lookup

    @Override
    public void restored() {
        result = Utilities.actionsGlobalContext().lookupResult(TopologyFileTypeDataObject.class);
        result.addLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        System.out.println("nazdar z installera");
    }
}
