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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp;

import lombok.Getter;

/**
 * @author Igor Kvasnicka
 */
public enum DscpValuesEnum {
        BEST_EFFORT(0) {
            @Override
            public String getTextName() {
                return "Best effort";
            }
        },
        AF11(1),
        AF12(2),
        AF13(3),
        AF21(4),
        AF22(5),
        AF23(6),
        AF31(7),
        AF32(8),
        AF33(9),
        AF41(10),
        AF42(11),
        AF43(12),
        EF(13);
        @Getter
        private int qosQueue;

        private DscpValuesEnum(int qosQueue) {
            this.qosQueue = qosQueue;
        }

        public String getTextName() {
            return toString();
        }
    }