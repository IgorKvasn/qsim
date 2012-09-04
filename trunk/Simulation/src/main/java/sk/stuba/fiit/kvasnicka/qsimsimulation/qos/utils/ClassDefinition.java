/*******************************************************************************
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
 ******************************************************************************/

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * this is a definition of a QoS class
 * each class consists of a group of QoS queues
 *
 * @author Igor Kvasnicka
 */
@Getter
public class ClassDefinition implements Serializable {
    private static final long serialVersionUID = 4352954903028246499L;

    private List<Integer> queueNumbers;
    private String name;

    public ClassDefinition(List<Integer> queueNumbers, String name) {

        if (queueNumbers == null) throw new IllegalArgumentException("queue numbers is NULL");
        if (queueNumbers.isEmpty()) throw new IllegalArgumentException("queue numbers is empty");

        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }

        this.queueNumbers = queueNumbers;
    }

    public ClassDefinition(List<Integer> queueNumbers) {
        this(queueNumbers, "N/A");
    }

    public ClassDefinition(Integer... queueNumbers) {
        if (queueNumbers == null) throw new IllegalArgumentException("queueNumbers is NULL");
        if (queueNumbers.length == 0) throw new IllegalArgumentException("queueNumbers is empty");

        this.queueNumbers = new ArrayList<Integer>(Arrays.asList(queueNumbers));
    }

    public ClassDefinition(String name, Integer... queueNumbers) {
        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }

        if (queueNumbers == null) throw new IllegalArgumentException("queueNumbers is NULL");
        if (queueNumbers.length == 0) throw new IllegalArgumentException("queueNumbers is empty");

        this.queueNumbers = new ArrayList<Integer>(Arrays.asList(queueNumbers));
    }
}
