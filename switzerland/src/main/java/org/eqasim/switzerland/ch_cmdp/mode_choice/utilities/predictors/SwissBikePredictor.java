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

        double elevationUp_m = 0.0;
        double elevationDown_m = 0.0;

        // 2. Compute elevation if the first element is a Leg
        if (elements != null && !elements.isEmpty() && elements.get(0) instanceof Leg) {
            Leg leg = (Leg) elements.get(0);

            if (leg.getRoute() instanceof NetworkRoute) {
                NetworkRoute networkRoute = (NetworkRoute) leg.getRoute();

                // Iterate over link IDs
                for (Id<Link> linkId : networkRoute.getLinkIds()) {
                    Link link = network.getLinks().get(linkId);
                    if (link != null && link.getAttributes().getAttribute("elevationChange") != null) {
                        double delta = (Double) link.getAttributes().getAttribute("elevationChange");
                        if (delta > 0) {
                            elevationUp_m += delta;
                        } else {
                            elevationDown_m += -delta;
                        }
                    }
                }
            }
        }

        // 3. Wrap BikeVariables into SwissBikeVariables
        return new SwissBikeVariables(bikeVars, elevationUp_m, elevationDown_m);
    }
}
