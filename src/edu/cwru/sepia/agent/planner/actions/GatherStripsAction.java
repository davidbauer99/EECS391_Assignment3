package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceState;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class GatherStripsAction implements StripsAction {

	private final int peasantCount;
	private final Position gatherPosition;

	public GatherStripsAction(int peasantCount, Position gatherPosition) {
		this.peasantCount = peasantCount;
		this.gatherPosition = gatherPosition;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Collection<PeasantState> peasants = state.getPeasants();
		List<PeasantState> peasantsAtResource = new ArrayList<PeasantState>();
		for (PeasantState peasant : peasants) {
			if (peasant.getPosition().chebyshevDistance(gatherPosition) <= 1) {
				peasantsAtResource.add(peasant);
			}
		}
		if (peasantsAtResource.size() < peasantCount) {
			return false;
		}
		// Check that the desired number of peasants have empty hands
		int peasantsWithNoCargo = 0;
		for (PeasantState peasant : peasantsAtResource) {
			if (peasant.getCargoAmount() == 0) {
				peasantsWithNoCargo++;
			}
		}
		if (peasantsWithNoCargo < peasantCount) {
			return false;
		}

		// Check for enough resources
		ResourceState resource = state.getResourceByPosition(gatherPosition);
		if (resource.getRemaining() < peasantCount * 100) {
			return false;
		}

		if (resource.getType() == ResourceType.WOOD) {
			if (state.getRequiredWood() < state.getCurrentWood() + 100
					* peasantCount) {
				return false;
			}
		} else {
			if (state.getRequiredGold() < state.getCurrentGold() + 100
					* peasantCount) {
				return false;
			}
		}

		// If the right number of peasants are there and able to gather, check
		// that this is a valid gather location
		return state.getNonEmptyResourcePositions().contains(gatherPosition);
	}

	@Override
	public GameState apply(GameState state) {
		return state.applyAction(this);
	}

	@Override
	public ActionType getActionType() {
		return ActionType.GATHER;
	}

	@Override
	public int getPeasantCount() {
		return peasantCount;
	}

	@Override
	public int getActionCost() {
		return 1;
	}

	public Position getResourcePosition() {
		return gatherPosition;
	}

	@Override
	public List<Integer> getPeasantIdsForAction(GameState state) {
		Collection<PeasantState> peasants = state.getPeasants();
		Collection<PeasantState> toRemove = new HashSet<PeasantState>();
		List<PeasantState> peasantsAtResource = new ArrayList<PeasantState>();
		for (PeasantState peasant : peasants) {
			if (peasant.getPosition().chebyshevDistance(gatherPosition) <= 1) {
				peasantsAtResource.add(peasant);
			}
		}
		if (peasantsAtResource.size() < peasantCount) {
			return new ArrayList<Integer>();
		}
		// Check that the desired number of peasants have empty hands
		int peasantsWithNoCargo = 0;
		for (PeasantState peasant : peasantsAtResource) {
			if (peasant.getCargoAmount() == 0) {
				peasantsWithNoCargo++;
			} else {
				toRemove.add(peasant);
			}
		}
		if (peasantsWithNoCargo < peasantCount) {
			return new ArrayList<Integer>();
		}

		// Check for enough resources
		ResourceState resource = state.getResourceByPosition(gatherPosition);
		if (resource.getRemaining() < peasantCount * 100) {
			return new ArrayList<Integer>();
		}

		if (resource.getType() == ResourceType.WOOD) {
			if (state.getRequiredWood() < state.getCurrentWood() + 100
					* peasantCount) {
				return new ArrayList<Integer>();
			}
		} else {
			if (state.getRequiredGold() < state.getCurrentGold() + 100
					* peasantCount) {
				return new ArrayList<Integer>();
			}
		}

		// If the right number of peasants are there and able to gather, check
		// that this is a valid gather location
		List<Integer> ids = new ArrayList<Integer>();
		peasantsAtResource.removeAll(toRemove);
		peasantsAtResource.stream().limit(peasantCount)
				.forEach(p -> ids.add(p.getId()));
		return ids;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((gatherPosition == null) ? 0 : gatherPosition.hashCode());
		result = prime * result + peasantCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GatherStripsAction other = (GatherStripsAction) obj;
		if (gatherPosition == null) {
			if (other.gatherPosition != null)
				return false;
		} else if (!gatherPosition.equals(other.gatherPosition))
			return false;
		if (peasantCount != other.peasantCount)
			return false;
		return true;
	}
}
