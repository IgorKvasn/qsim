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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification;

import org.josql.Query;
import org.josql.QueryExecutionException;
import org.josql.QueryParseException;
import org.josql.QueryResults;
import org.josql.functions.AbstractFunctionHandler;
import org.josql.functions.NotFixedResults;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class ClassificationUtil {

    private static MyFunctionHandler functionHandler = new MyFunctionHandler();
    private static Query q = new Query();
    public static final String JO_SQL_PREFIX = "SELECT * FROM sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet WHERE ";

    static {
        q.addFunctionHandler(functionHandler);
    }

    /**
     * determines whether packet fulfills specified classification rule
     *
     * @param rule   rule to be applied
     * @param packet packet to be classified
     * @return true/false
     */
    public static boolean isClassificationRuleApplied(String rule, Packet packet) throws ClassificationException {
        rule = JO_SQL_PREFIX + rule;

        try {
            q.parse(rule);
            QueryResults qr = q.execute(Arrays.asList(packet));
            return ! qr.getResults().isEmpty();
        } catch (QueryParseException e) {
            e.printStackTrace();
        } catch (QueryExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static class MyFunctionHandler extends AbstractFunctionHandler implements NotFixedResults {

        public MyFunctionHandler() {
        }

        public Boolean destination(List<String> nodesToSearch) {
            Packet packet = (Packet) this.q.getCurrentObject();
            return isNodeInList(packet.getDestination().getName(), nodesToSearch);
        }

        public Boolean destination(String node) {
            Packet packet = (Packet) this.q.getCurrentObject();

            if (packet.getDestination().getName().matches(node)) {
                return true;
            }

            return false;
        }

        public Boolean source(List<String> nodesToSearch) {
            Packet packet = (Packet) this.q.getCurrentObject();
            return isNodeInList(packet.getSource().getName(), nodesToSearch);
        }

        public Boolean source(String node) {
            Packet packet = (Packet) this.q.getCurrentObject();

            if (packet.getSource().getName().matches(node)) {
                return true;
            }
            return false;
        }

        private Boolean isNodeInList(String node, List<String> nodesToSearch) {
            for (String dest : nodesToSearch) {
                if (node.matches(dest)) {
                    return true;
                }
            }
            return false;
        }
    }
}
