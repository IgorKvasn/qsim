package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.closeabletabbedpane;


import java.util.EventListener;

public interface CloseableTabbedPaneListener extends EventListener {

    public boolean closeTab(int index);
}