/*
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
 */
package sk.stuba.fiit.kvasnicka.topologyvisual;

import org.openide.util.NbPreferences;

/**
 *
 * @author Igor Kvasnicka
 */
public class PreferenciesHelper {

    public static boolean isRoutingDistanceProtocol() {
        return NbPreferences.forModule(PreferenciesHelper.class).getBoolean("isRoutingDistanceProtocol", true);
    }

    public static void setRoutingDistanceProtocol(boolean routing) {
        NbPreferences.forModule(PreferenciesHelper.class).putBoolean("isRoutingDistanceProtocol", routing);
    }

    public static boolean isNeverShowEdgeDeleteConfirmation() {
        return NbPreferences.forModule(PreferenciesHelper.class).getBoolean("isNeverShowEdgeDeleteConfirmation", false);
    }

    public static void setNeverShowEdgeDeleteConfirmation(boolean never) {
        NbPreferences.forModule(PreferenciesHelper.class).putBoolean("isNeverShowEdgeDeleteConfirmation", never);
    }

    public static boolean isNeverShowVertexDeleteConfirmation() {
        return NbPreferences.forModule(PreferenciesHelper.class).getBoolean("isNeverShowVertexDeleteConfirmation", false);
    }

    public static void setNeverShowVertexDeleteConfirmation(boolean never) {
        NbPreferences.forModule(PreferenciesHelper.class).putBoolean("isNeverShowVertexDeleteConfirmation", never);
    }

    public static boolean isShowNodeNamesInTopology() {
        return NbPreferences.forModule(PreferenciesHelper.class).getBoolean("isShowNodeNamesInTopology", true);
    }

    public static void setShowNodeNamesInTopology(boolean show) {
        NbPreferences.forModule(PreferenciesHelper.class).putBoolean("isShowNodeNamesInTopology", show);
    }

    public static boolean isNodeTooltipName() {
        return NbPreferences.forModule(PreferenciesHelper.class).getBoolean("isNodeTooltipName", true);
    }

    public static void setNodeTooltipName(boolean show) {
        NbPreferences.forModule(PreferenciesHelper.class).putBoolean("isNodeTooltipName", show);
    }

    public static boolean isNodeTooltipDescription() {
        return NbPreferences.forModule(PreferenciesHelper.class).getBoolean("isNodeTooltipDescription", false);
    }

    public static void setNodeTooltipDescription(boolean show) {
        NbPreferences.forModule(PreferenciesHelper.class).putBoolean("isNodeTooltipDescription", show);
    }
}
