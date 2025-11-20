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
        // 1. Get travel time from the original BikePredictor
        BikeVariables bikeVars = delegate.predict(person, trip, elements);

        // 2. Get network nodes of origin and destination
        Link fromLink = network.getLinks().get(trip.getOriginActivity().getLinkId());
        Link toLink = network.getLinks().get(trip.getDestinationActivity().getLinkId());

        // Use the from-node of the origin link and from-node of the destination link
        Coord fromNode = fromLink.getFromNode().getCoord();
        Coord toNode = toLink.getFromNode().getCoord(); // or getToNode() if more appropriate

        // 3. Compute elevation difference (only positive = uphill)
        double dz = toNode.getZ() - fromNode.getZ();
        if (dz < 0) dz = 0;

        // 4. Compute 2D Euclidean distance
        double dx = toNode.getX() - fromNode.getX();
        double dy = toNode.getY() - fromNode.getY();
        double dist = Math.sqrt(dx*dx + dy*dy);

        // 5. Compute slope (rise/run)
        double slope = (dist > 0) ? dz / dist : 0.0;

        // 3. Wrap BikeVariables into SwissBikeVariables
        return new SwissBikeVariables(bikeVars, slope);
    }
}
