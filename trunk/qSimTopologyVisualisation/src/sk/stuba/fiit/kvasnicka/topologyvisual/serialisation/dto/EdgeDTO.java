/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Igor Kvasnicka
 */
@Getter
@Setter
public class EdgeDTO {

    private long speed;
    private int length;
    private String node1, node2;
}
