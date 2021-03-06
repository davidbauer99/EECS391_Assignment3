package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantState;
import edu.cwru.sepia.agent.planner.Position;

public class DepositStripsAction implements StripsAction {

	private final int peasantCount;
	private final Position depositPosition;

	public DepositStripsAction(int peasantCount, Position depositPosition) {
		this.peasantCount = peasantCount;
		this.depositPosition = depositPosition;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Collection<PeasantState> peasants = state.getPeasants();
		// If the deposit site isn't the town hall, false
		if (!state.getTownHallPosition().equals(depositPosition)) {
			return false;
		}
		// Check that enough peasants are there
		List<PeasantState> peasantsAtDepositSite = new ArrayList<PeasantState>();
		for (PeasantState peasant : peasants) {
			if (peasant.getPosition().chebyshevDistance(depositPosition) <= 1) {
				peasantsAtDepositSite.add(peasant);
			}
		}
		if (peasantsAtDepositSite.size() < peasantCount) {
			return false;
		}
		// Check that the desired number of peasants have cargo
		int peasantsWithCargo = 0;
		for (PeasantState peasant : peasantsAtDepositSite) {
			if (peasant.getCargoAmount() > 0) {
				peasantsWithCargo++;
			}
		}
		if (peasantsWithCargo < peasantCount) {
			return false;
		}
		// All good.
		return true;
	}

	@Override
	public GameState apply(GameState state) {
		return state.applyAction(this);
	}

	@Override
	public ActionType getActionType() {
		return ActionType.DEPOSIT;
	}

	@Override
	public int getPeasantCount() {
		return peasantCount;
	}

	@Override
	public int getActionCost() {
		return 1;
	}

	@Override
	public List<Integer> getPeasantIdsForAction(GameState state) {
		Collection<PeasantState> peasants = state.getPeasants();
		Collection<PeasantState> toRemove = new HashSet<PeasantState>();
		// If the deposit site isn't the town hall, false
		if (!state.getTownHallPosition().equals(depositPosition)) {
			return new ArrayList<Integer>();
		}
		// Check that enough peasants are there
		List<PeasantState> peasantsAtDepositSite = new ArrayList<PeasantState>();
		for (PeasantState peasant : peasants) {
			if (peasant.getPosition().chebyshevDistance(depositPosition) <= 1) {
				peasantsAtDepositSite.add(peasant);
			}
		}
		if (peasantsAtDepositSite.size() < peasantCount) {
			return new ArrayList<Integer>();
		}
		// Check that the desired number of peasants have cargo
		int peasantsWithCargo = 0;
		for (PeasantState peasant : peasantsAtDepositSite) {
			if (peasant.getCargoAmount() > 0) {
				peasantsWithCargo++;
			} else {
				toRemove.add(peasant);
			}
		}
		if (peasantsWithCargo < peasantCount) {
			return new ArrayList<Integer>();
		}
		// All good.
		List<Integer> ids = new ArrayList<Integer>();
		peasantsAtDepositSite.removeAll(toRemove);
		peasantsAtDepositSite.stream().limit(peasantCount)
				.forEach(p -> ids.add(p.getId()));
		return ids;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((depositPosition == null) ? 0 : depositPosition.hashCode());
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
		DepositStripsAction other = (DepositStripsAction) obj;
		if (depositPosition == null) {
			if (other.depositPosition != null)
				return false;
		} else if (!depositPosition.equals(other.depositPosition))
			return false;
		if (peasantCount != other.peasantCount)
			return false;
		return true;
	}

}
