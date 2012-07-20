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
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.windows.CloneableTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.descriptors.TopologyInformationDescription;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.descriptors.TopologyVisualisationDescription;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;

/**
 *
 * @author Igor Kvasnicka
 */
public class MyOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    public MyOpenSupport(MultiDataObject.Entry entry) {
        super(entry);
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        TopologyFileTypeDataObject obj = (TopologyFileTypeDataObject) entry.getDataObject();
        TopologyInformationDescription main1 = new TopologyInformationDescription(obj);
        TopologyVisualisationDescription main2 = new TopologyVisualisationDescription(obj);
        MultiViewDescription[] descArry = {main1, main2};

        // Create the multiview:
        CloneableTopComponent tc = MultiViewFactory.createCloneableMultiView(descArry, main1, null); 
        tc.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png").getImage());
        return tc;
    }
}
