package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.PeasantState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class DepositStripsAction implements StripsAction {

	private final Direction direction;
	private final int peasantID;

	public DepositStripsAction(Direction direction, int peasantID) {
		this.direction = direction;
		this.peasantID = peasantID;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Position peasantPosition = state.getPeasantPosition(peasantID);
		Position storagePosition = peasantPosition.move(direction);

		Position townHallPosition = state.getTownHallPosition();
		PeasantState peasant = state.getPeasant(peasantID);

		if (storagePosition.equals(townHallPosition)
				&& 0 < peasant.getCargoAmount()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public GameState apply(GameState state) {
		return state.applyAction(this);
	}

	@Override
	public ActionType getActionType() {
		return ActionType.DEPOSIT;
	}

}
