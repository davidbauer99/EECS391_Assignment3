package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class GatherStripsAction implements StripsAction {

	private final int peasantId;
	private final Direction gatherDirection;

	public GatherStripsAction(int peasantId, Direction gatherDirection) {
		this.peasantId = peasantId;
		this.gatherDirection = gatherDirection;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Position peasantPos = state.getPeasantPosition(peasantId);
		Position gatherPos = peasantPos.move(gatherDirection);
		PeasantState peasant = state.getPeasant(peasantId);
		if (state.getNonEmptyResourcePositions().contains(gatherPos)
				&& peasant.getCargoAmount() == 0) {
			return true;
		}
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		return state.applyAction(this);
	}

	@Override
	public ActionType getActionType() {
		return ActionType.GATHER;
	}

}
