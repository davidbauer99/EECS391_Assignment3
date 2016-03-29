package edu.cwru.sepia.agent.planner.actions;

import java.util.List;

import edu.cwru.sepia.agent.planner.GameState;

public class ProductionStripsAction implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		boolean enoughGold = (400 <= state.getCurrentGold());
		boolean enoughFood = (1 <= state.getCurrentFood());
		return (enoughGold && enoughFood && state.desiredPeasantNumber() > state
				.getPeasants().size());
	}

	@Override
	public GameState apply(GameState state) {
		return state.applyAction(this);
	}

	@Override
	public ActionType getActionType() {
		return ActionType.BUILD_PEASANT;
	}

	@Override
	public int getPeasantCount() {
		return 0;
	}

	@Override
	public int getActionCost() {
		return 1;
	}

	@Override
	public List<Integer> getPeasantIdsForAction(GameState gameState) {
		// TODO Auto-generated method stub
		return null;
	}
}
