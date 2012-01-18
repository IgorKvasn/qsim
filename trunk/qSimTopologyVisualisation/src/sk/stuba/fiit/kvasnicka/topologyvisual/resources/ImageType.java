package sk.stuba.fiit.kvasnicka.topologyvisual.resources;

/**
 * User: Igor Kvasnicka Date: 9/2/11 Time: 1:15 PM
 */
public enum ImageType {

    TOPOLOGY_VERTEX_ROUTER("files/vertex_router.png", "files/vertex_router_selected.png", "files/vertex_router_checked.png"),
    TOPOLOGY_VERTEX_SWITCH("files/vertex_switch.png", "files/vertex_switch_selected.png", "files/vertex_router_checked.png"),
    TOPOLOGY_VERTEX_COMPUTER("files/vertex_computer.png", "files/vertex_computer_selected.png", "files/vertex_router_checked.png");
    private String path;
    private String selectedPath;
    private String checkedPath;

    private ImageType(String path, String selectedPath, String checkedPath) {
        this.path = path;
        this.selectedPath = selectedPath;
        this.checkedPath = checkedPath;
    }

    private ImageType(String path) {
        this.path = path;
        this.selectedPath = path;
        this.checkedPath = path;
    }

    /**
     * path to selected icon if this ImageType has got no selection icon, path
     * to the standard image is returned
     *
     * @see #getResourcePath()
     */
    public String getSelectePath() {
        return selectedPath;
    }

    /**
     * path to checked icon if this ImageType has got no checked icon, path to
     * the standard image is returned
     *
     * @see #getResourcePath()
     */
    public String getCheckedPath() {
        return checkedPath;
    }

    /**
     * returns path to the image itself
     *
     * @return path to the image within jar
     */
    public String getResourcePath() {
        return path;
    }
}
