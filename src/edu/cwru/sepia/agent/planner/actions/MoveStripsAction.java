package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Collection;
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
	public List<Integer> getPeasantIdsForAction(GameState gameState) {
		// TODO Auto-generated method stub
		return null;
	}

}
