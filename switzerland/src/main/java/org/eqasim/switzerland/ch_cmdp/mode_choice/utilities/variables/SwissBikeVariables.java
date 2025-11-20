package org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.variables;

import org.eqasim.core.simulation.mode_choice.utilities.variables.BikeVariables;

public class SwissBikeVariables extends BikeVariables {
    public final double slope;

    public SwissBikeVariables(
            BikeVariables delegate,
            double slope) {

        // Calls parent: only travelTime_min
        super(delegate.travelTime_min);

        this.slope = slope;
    }
}
