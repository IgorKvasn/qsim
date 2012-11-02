/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.topology;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;

/**
 * wrapper for clipboard copy/paste functionality this wrapper adds NetworkNode
 * new Transferable interface, so it can be added to the clipboard
 *
 * @author Igor Kvasnicka
 */
public class ClipboardWrapper implements Transferable {

    private final NetworkNode node;
    private final DataFlavor[] networkNodeFlavorArray;
    public static final NetworkNodeDataFlavow networkNodeFlavor;

    static {
        try {
            networkNodeFlavor = new NetworkNodeDataFlavow();
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("fatal error: unable to create transferable wrapper", ex);
        }
    }

    public ClipboardWrapper(NetworkNode node) {
        this.node = node;
        networkNodeFlavorArray = new DataFlavor[]{networkNodeFlavor};
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return networkNodeFlavorArray;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return networkNodeFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return node;
    }

    public static class NetworkNodeDataFlavow extends DataFlavor {

        public NetworkNodeDataFlavow() throws ClassNotFoundException {
            super(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + NetworkNode.class.getCanonicalName());
        }
    }
}
