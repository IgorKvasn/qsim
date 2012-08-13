/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.recentclosed;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *
 * @author Igor Kvasnicka
 */
@Getter
@EqualsAndHashCode
public class ClosedProject implements Serializable {

    private String name;
    private String path;

    public ClosedProject(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public ClosedProject() {
    }
}
