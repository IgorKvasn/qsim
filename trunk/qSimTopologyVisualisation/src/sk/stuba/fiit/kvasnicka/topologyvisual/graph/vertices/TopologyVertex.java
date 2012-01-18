package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices;

import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import java.awt.Color;
import java.io.Serializable;
import javax.swing.Icon;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Computer;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Router;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * User: Igor Kvasnicka Date: 9/2/11 Time: 11:35 AM
 */
/**
 * abstract class that represents vertices in topology
 */
@XmlSeeAlso({RouterVertex.class, SwitchVertex.class, ComputerVertex.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TopologyVertex implements Serializable {

    protected ImageType imageType;
    @Getter
    protected final Icon icon;
    @Getter
    @Setter
    @XmlTransient
    private boolean selected = false;
    @Getter
    @Setter
    protected String description = "NA";
    protected String name;

    /**
     * creates new instance
     *
     * @param imageType type of vertex
     */
    protected TopologyVertex(ImageType imageType, String name) {
        this.imageType = imageType;
        this.name = name;
        this.icon = ImageResourceHelper.loadImageVertex(imageType, false);
        if (icon == null) {
            throw new IllegalStateException("icon not loaded");
        }
    }

    /**
     * returns type of image that is associate with specific vertex
     *
     * @return image type
     */
    public ImageType getImageType() {
        return imageType;
    }

    /**
     * returns name of the vertex that will be shown as label
     *
     * @return label
     */
    public String getName() {
        return name;
    }

    ;

    @Override
    public String toString() {
        return name;
    }

    /**
     * identifies if this NetworkNode is able of <b>creating</b> routes</p> that
     * means it can be a source or destination of some route a typical example
     * of such a node is Router on the other hand Computer or Switch cannot
     * create a route - they are either only a destination (Computer) or routing
     * protocol will create a route instead of them (Switch)
     *
     * override this method if you want to create new type of NetworkNode
     *
     * @return false if not overriden
     */
    public boolean isRoutingAllowed() {
        return false;
    }

    /**
     * returns underlying data model
     *
     * @see Router
     * @see Computer
     * @return
     */
    public abstract NetworkNode getDataModel();

    /**
     * deselects vertex (removes routeCreationIcon from LayeredIcon)
     */
    public void deSelectVertex() {
        if (icon instanceof LayeredIcon) {
            ((LayeredIcon) icon).setImage(ImageResourceHelper.loadImageVertexAsImage(imageType, selected));
        }
    }

    public void deCheckVertex() {
        if (icon instanceof LayeredIcon) {
            ((LayeredIcon) icon).setImage(ImageResourceHelper.loadImageVertexAsImage(imageType, selected));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TopologyVertex other = (TopologyVertex) obj;
        return other.getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
