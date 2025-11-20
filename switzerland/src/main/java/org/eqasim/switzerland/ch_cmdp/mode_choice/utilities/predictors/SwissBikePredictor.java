package org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.predictors;

import java.util.List;

import com.google.inject.Inject;

import org.eqasim.core.simulation.mode_choice.utilities.predictors.BikePredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CachedVariablePredictor;
import org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.variables.SwissBikeVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.BikeVariables;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import org.matsim.core.population.routes.NetworkRoute;

public class SwissBikePredictor extends CachedVariablePredictor<SwissBikeVariables> {

    public final BikePredictor delegate;
    private final Network network;

    @Inject
    public SwissBikePredictor(BikePredictor bikePredictor, Network network) {
        this.delegate = bikePredictor;
        this.network = network;
    }

    @Override
    protected SwissBikeVariables predict(Person person, DiscreteModeChoiceTrip trip,
                                        List<? extends PlanElement> elements) {
        // 1. Get travel time from the original BikePredictor
        BikeVariables bikeVars = delegate.predict(person, trip, elements);

        // Origin & destination coordinates (per trip)
        var from = trip.getOriginActivity().getCoord();
        var to = trip.getDestinationActivity().getCoord();

        // Elevation difference (only positive = uphill)
        double dz = to.getZ() - from.getZ();
        if (dz < 0) dz = 0;

        // 2D Euclidean distance
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        // Compute slope (rise/run)
        double slope = (dist > 0) ? dz / dist : 0.0;

        // 3. Wrap BikeVariables into SwissBikeVariables
        return new SwissBikeVariables(bikeVars, slope);
    }
}
