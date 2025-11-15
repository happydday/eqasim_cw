package org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.variables;

import org.eqasim.core.simulation.mode_choice.utilities.variables.BikeVariables;

public class SwissBikeVariables extends BikeVariables {
    public final double elevationUp_m;
    public final double elevationDown_m;

    public SwissBikeVariables(
            BikeVariables delegate,
            double elevationUp_m,
            double elevationDown_m) {

        // Calls parent: only travelTime_min
        super(delegate.travelTime_min);

        this.elevationUp_m = elevationUp_m;
        this.elevationDown_m = elevationDown_m;
    }
}
