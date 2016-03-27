package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class ProductionStripsAction
{
  public boolean preconditionsMet (GameState state)
  {
    boolean enoughGold = (400 <= state.getCurrentGold ()); // need 400 //
    boolean enoughFood = (1 <= state.getCurrentFood ()); // need 1 //use int foodAvailable
    return (enoughGold && enoughFood);
  }

  public GameState apply (GameState state)
  {
    return state.applyAction (this);
  }
}
