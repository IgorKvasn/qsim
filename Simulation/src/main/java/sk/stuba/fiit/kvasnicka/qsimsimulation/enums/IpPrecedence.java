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

package sk.stuba.fiit.kvasnicka.qsimsimulation.enums;

/**
 * @author Igor Kvasnicka
 */
public enum IpPrecedence {
    IP_PRECEDENCE_0(0),
    IP_PRECEDENCE_1(1),
    IP_PRECEDENCE_2(2),
    IP_PRECEDENCE_3(3),
    IP_PRECEDENCE_4(4),
    IP_PRECEDENCE_5(5),
    IP_PRECEDENCE_6(6),
    IP_PRECEDENCE_7(7);

    private final int ipPrecedence;

    private IpPrecedence(int ipPrecedence) {
        this.ipPrecedence = ipPrecedence;
    }

    public int getIntRepresentation() {
        return ipPrecedence;
    }
}
