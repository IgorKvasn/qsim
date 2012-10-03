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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils;

import org.apache.log4j.Logger;
import org.josql.Query;
import org.josql.QueryExecutionException;
import org.josql.QueryParseException;
import org.josql.QueryResults;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.PacketDscpClassificationInterf;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.josql.MyFunctionHandler;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.josql.NegativeMyFunctionHandler;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Igor Kvasnicka
 */
public abstract class ClassificationUtil {

    private static Logger logg = Logger.getLogger(ClassificationUtil.class);
    private static Query q = new Query();
    public static final String JO_SQL_PREFIX = "SELECT * FROM sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.PacketDscpClassificationInterf WHERE ";

    static {
        q.addFunctionHandler(new MyFunctionHandler());
        q.addFunctionHandler(new NegativeMyFunctionHandler());
    }

    /**
     * validates given DSCP query if it is correctly written and passable
     *
     * @param rule given DSCP query (rule)
     * @throws ClassificationException any problem related to given rule
     */
    public static void validateClassificationRule(String rule) throws ClassificationException {
        String josqlRule = JO_SQL_PREFIX + rule;
        try {
            q.parse(josqlRule);
            q.execute(Collections.emptyList());//I will try to execute JoSQL
        } catch (QueryParseException e) {
            throw new ClassificationException(e.getMessage(), e);
        } catch (QueryExecutionException e) {
            throw new ClassificationException(e.getMessage(), e);
        }
    }


    /**
     * determines whether packet fulfills specified classification rule
     *
     * @param rule   rule to be applied
     * @param packet packet to be classified
     * @return true/false
     */
    public static boolean isClassificationRuleApplied(String rule, PacketDscpClassificationInterf packet) throws ClassificationException {
        String josqlRule = JO_SQL_PREFIX + rule;

        try {
            q.parse(josqlRule);
            QueryResults qr = q.execute(Arrays.asList(packet));
            return ! qr.getResults().isEmpty();
        } catch (QueryParseException e) {
            throw new ClassificationException(e.getMessage(), e);
        } catch (QueryExecutionException e) {
            throw new ClassificationException(e.getMessage(), e);
        }
    }
}
