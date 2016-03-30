package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantState;
import edu.cwru.sepia.agent.planner.Position;

public class MoveStripsAction implements StripsAction {

	private final int peasantCount;
	private final Position start;
	private final Position finish;

	public MoveStripsAction(int peasantCount, Position start, Position finish) {
		this.peasantCount = peasantCount;
		this.start = start;
		this.finish = finish;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Collection<PeasantState> peasants = state.getPeasants();
		List<PeasantState> peasantsAtStart = new ArrayList<PeasantState>();
		for (PeasantState peasant : peasants) {
			if (peasant.getPosition().chebyshevDistance(start) <= 1) {
				peasantsAtStart.add(peasant);
			}
		}
		if (peasantsAtStart.size() < peasantCount) {
			return false;
		}
		// If starting at the town hall, peasants must have empty hands
		int peasantsWithCorrectCargo = 0;
		if (state.getTownHallPosition().equals(start)) {
			for (PeasantState peasant : peasantsAtStart) {
				if (peasant.getCargoAmount() == 0) {
					peasantsWithCorrectCargo++;
				}
			}
			if (peasantsWithCorrectCargo >= peasantCount
					&& state.getNonEmptyResourcePositions().contains(finish)) {
				return true;
			} else {
				return false;
			}
			// If ending at the town hall, peasants must be carrying something
		} else if (state.getTownHallPosition().equals(finish)) {
			for (PeasantState peasant : peasantsAtStart) {
				if (peasant.getCargoAmount() > 0) {
					peasantsWithCorrectCargo++;
				}
			}
			if (peasantsWithCorrectCargo >= peasantCount) {
				return true;
			} else {
				return false;
			}
		} else {
			// not starting or ending at the town hall is invalid
			return false;
		}
	}

	@Override
	public GameState apply(GameState state) {
		return state.applyAction(this);
	}

	@Override
	public ActionType getActionType() {
		return ActionType.MOVE;
	}

	@Override
	public int getPeasantCount() {
		return peasantCount;
	}

	@Override
	public int getActionCost() {
		return start.chebyshevDistance(finish);
	}

	public Position getDestination() {
		return finish;
	}

	@Override
	public List<Integer> getPeasantIdsForAction(GameState state) {
		List<PeasantState> peasantsAtStart = new ArrayList<PeasantState>();
		Collection<PeasantState> peasants = state.getPeasants();
		Collection<PeasantState> toRemove = new HashSet<PeasantState>();
		for (PeasantState peasant : peasants) {
			if (peasant.getPosition().chebyshevDistance(start) <= 1) {
				peasantsAtStart.add(peasant);
			}
		}
		if (peasantsAtStart.size() < peasantCount) {
			return new ArrayList<Integer>();
		}
		// If starting at the town hall, peasants must have empty hands
		int peasantsWithCorrectCargo = 0;
		if (state.getTownHallPosition().equals(start)) {
			for (PeasantState peasant : peasantsAtStart) {
				if (peasant.getCargoAmount() == 0) {
					peasantsWithCorrectCargo++;
				} else {
					toRemove.add(peasant);
				}
			}
			if (peasantsWithCorrectCargo >= peasantCount
					&& state.getNonEmptyResourcePositions().contains(finish)) {
				List<Integer> ids = new ArrayList<Integer>();
				peasantsAtStart.removeAll(toRemove);
				peasantsAtStart.stream().limit(peasantCount)
						.forEach(p -> ids.add(p.getId()));
				return ids;
			} else {
				return new ArrayList<Integer>();
			}
			// If ending at the town hall, peasants must be carrying something
		} else if (state.getTownHallPosition().equals(finish)) {
			for (PeasantState peasant : peasantsAtStart) {
				if (peasant.getCargoAmount() > 0) {
					peasantsWithCorrectCargo++;
				} else {
					toRemove.add(peasant);
				}
			}
			if (peasantsWithCorrectCargo >= peasantCount) {
				List<Integer> ids = new ArrayList<Integer>();
				peasantsAtStart.removeAll(toRemove);
				peasantsAtStart.stream().limit(peasantCount)
						.forEach(p -> ids.add(p.getId()));
				return ids;
			} else {
				return new ArrayList<Integer>();
			}
		} else {
			// not starting or ending at the town hall is invalid
			return new ArrayList<Integer>();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((finish == null) ? 0 : finish.hashCode());
		result = prime * result + peasantCount;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		MoveStripsAction other = (MoveStripsAction) obj;
		if (finish == null) {
			if (other.finish != null)
				return false;
		} else if (!finish.equals(other.finish))
			return false;
		if (peasantCount != other.peasantCount)
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

}
