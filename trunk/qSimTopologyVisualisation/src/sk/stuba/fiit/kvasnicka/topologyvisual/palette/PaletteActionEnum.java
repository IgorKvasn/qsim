package sk.stuba.fiit.kvasnicka.topologyvisual.palette;

/**
 * Date: 9/3/11 Time: 3:44 PM
 * <p/>
 * what type of action is selected in task pane
 *
 * @author Igor Kvasnicka
 */
public enum PaletteActionEnum {

    NEW_VERTEX_ROUTER("router"),
    NEW_VERTEX_PC("PC"),
    NEW_VERTEX_SWITCH("Switch"),
    NEW_EDGE_ETHERNET("Ethernet"),
    NEW_EDGE_FAST_ETHERNET("Fast Ethernet"),
    NEW_EDGE_GIGA_ETHERNET("Giga Ethernet"),
    NEW_EDGE_CUSTOM("Custom link");
    private final String displayableName;

    PaletteActionEnum(String displayableName) {
        this.displayableName = displayableName;
    }

    /**
     * determines if specified action is action to create new edge
     *
     * @param action action to be determined
     * @return true if new edge is being created
     */
    public static boolean isEdgeAction(PaletteActionEnum action) {
        if (action == null) {
            return false;
        }
        return action.equals(NEW_EDGE_CUSTOM) || action.equals(NEW_EDGE_FAST_ETHERNET) || action.equals(NEW_EDGE_ETHERNET) || action.equals(NEW_EDGE_GIGA_ETHERNET);
    }

    public String getDisplayableName() {
        return displayableName;
    }
}
