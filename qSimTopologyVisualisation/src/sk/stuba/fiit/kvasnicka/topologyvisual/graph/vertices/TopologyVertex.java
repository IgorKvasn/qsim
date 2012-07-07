package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices;

import edu.uci.ics.jung.visualization.LayeredIcon;
import java.io.Serializable;
import javax.swing.Icon;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Computer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
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
    protected String description = "NA";
    protected String name;

    /**
     * creates new instance
     *
     * @param imageType type of vertex
     */
    protected TopologyVertex(ImageType imageType, String name, String description) {
        this.imageType = imageType;
        this.name = name;
        this.description = description;
        this.icon = ImageResourceHelper.loadImageVertex(imageType, null);
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

    @Override
    public String toString() {
        return name;
    }

    /**
     * identifies if this NetworkNode is able of <b>creating</b> routes</p> that
     * means it can be a source or destination of some route
     *
     * override this method if you want to create new type of NetworkNode
     *
     * @return false if not overriden
     */
    public boolean isRoutingAllowed() {
        return true;
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
     * deselects vertex
     */
    public void deSelectVertex() {
        if (icon instanceof LayeredIcon) {
            ((LayeredIcon) icon).setImage(ImageResourceHelper.loadImageVertexAsImage(imageType, null));
        }
    }

    /**
     * de-checks vertex
     */
    public void deCheckVertex() {
        if (icon instanceof LayeredIcon) {
            ((LayeredIcon) icon).setImage(ImageResourceHelper.loadImageVertexAsImage(imageType, null));
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
