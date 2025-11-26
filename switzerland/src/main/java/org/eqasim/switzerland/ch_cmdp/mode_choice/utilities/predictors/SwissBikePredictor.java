package org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.predictors;

import java.util.List;

import com.google.inject.Inject;

import org.eqasim.core.simulation.mode_choice.utilities.predictors.BikePredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CachedVariablePredictor;
import org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.variables.SwissBikeVariables;
import org.eqasim.core.simulation.mode_choice.utilities.variables.BikeVariables;
import org.matsim.api.core.v01.Coord;
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

        // 1) Base bike variables from delegate
        BikeVariables bikeVars = delegate.predict(person, trip, elements);

        // 2) Find the leg + route (if any)
        Leg leg = elements.stream()
                .filter(pe -> pe instanceof Leg)
                .map(pe -> (Leg) pe)
                .findFirst()
                .orElse(null);

        double totalUphillDZ = 0.0;
        double uphillDistance = 0.0;

        if (leg != null && leg.getRoute() instanceof NetworkRoute) {
            NetworkRoute route = (NetworkRoute) leg.getRoute();

            // build the ordered list of links: start, intermediate, end
            List<Id<Link>> allLinks = new java.util.ArrayList<>();
            allLinks.add(route.getStartLinkId());
            allLinks.addAll(route.getLinkIds());
            allLinks.add(route.getEndLinkId());

            for (Id<Link> linkId : allLinks) {
                Link link = network.getLinks().get(linkId);
                if (link == null) {
                    // defensive: missing link in network
                    continue;
                }

                Coord cFrom = link.getFromNode().getCoord();
                Coord cTo   = link.getToNode().getCoord();

                // If z is not set (== 0 or NaN), you may want to handle that here.
                double dz = cTo.getZ() - cFrom.getZ();
                double len = link.getLength();

                if (dz > 0.0) {
                    totalUphillDZ += dz;
                    uphillDistance += len;
                }
            }
        }

        // 3) uphill-only mean slope
        double slope = (uphillDistance > 0.0) ? (totalUphillDZ / uphillDistance) : 0.0;

        // 4) Return SwissBikeVariables (adjust constructor if you extended it)
        return new SwissBikeVariables(bikeVars, slope);
    }

}
