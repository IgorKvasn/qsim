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

package sk.stuba.fiit.kvasnicka.qsimsimulation.logs;

import lombok.Getter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * @author Igor Kvasnicka
 */
@Getter
public class SimulationLog {
    private static DecimalFormat simulationTimeFormat;

    static {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setGroupingSeparator(' ');

        String strange = "#,##0.###"; //maximum of 3 fraction digits
        simulationTimeFormat = new DecimalFormat(strange, formatSymbols);
        simulationTimeFormat.setGroupingSize(3);
    }

    private final LogCategory category;
    private String cause;
    private final String sourceName;
    private final LogSource logSource;
    private final double simulationTime;


    public SimulationLog(LogCategory category, String cause, String sourceName, LogSource logSource, double simulationTime) {
        this.category = category;
        this.cause = cause;
        this.sourceName = sourceName;
        this.logSource = logSource;
        this.simulationTime = simulationTime;
    }


    public String getFormattedSimulationTime() {
        return simulationTimeFormat.format(simulationTime);
    }
}
