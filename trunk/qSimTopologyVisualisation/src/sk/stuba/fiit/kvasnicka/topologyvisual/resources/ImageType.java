package sk.stuba.fiit.kvasnicka.topologyvisual.resources;

/**
 * User: Igor Kvasnicka
 * Date: 9/2/11
 * Time: 1:15 PM
 */
public enum ImageType {

    TOPOLOGY_VERTEX_ROUTER("files/vertex_router.png"),
    TOPOLOGY_VERTEX_SWITCH("files/vertex_switch.png"),
    TOPOLOGY_VERTEX_COMPUTER("files/vertex_computer.png");
    private String path;

    private ImageType(String path) {
        this.path = path;
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
