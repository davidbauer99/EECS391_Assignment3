package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class DepositStripsAction implements StripsAction
{

  private final Direction direction;
  private final int peasantID;

  public DepositStripsAction (Direction direction, int peasantID)
  {
    this.direction = direction;
    this.peasantID = peasantID;
  }

  public boolean preconditionsMet (GameState state)
  {
    Position peasantPosition = state.getPeasantPosition (peasantID);
    Position storagePosition = peasantPosition.move (direction);

    Position townHallPosition = state.getTownHallPosition ();

    if (storagePosition.equals (townHallPosition))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public GameState apply (GameState state)
  {
    return state.applyAction (this);
  }

}
