package edu.cwru.sepia.agent.planner.actions;

import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class MoveStripsAction implements StripsAction {
	
	private final Direction direction;
	private final int peasantId;
	
	public MoveStripsAction(Direction direction, int peasantId) {
		this.direction = direction;
		this.peasantId = peasantId;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		Position peasantPosition = state.getPeasantPosition(peasantId);
		Position nextPosition = peasantPosition.move(direction);
		List<Position> invalidPositions = state.getOccupiedPositions();
		if (nextPosition.inBounds(state.getXExtent(), state.getYExtent())
				&& !invalidPositions.contains(nextPosition)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public GameState apply(GameState state) {
		return state.applyAction(this);
	}

}
