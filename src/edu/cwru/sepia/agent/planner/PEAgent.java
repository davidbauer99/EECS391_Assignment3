package edu.cwru.sepia.agent.planner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.MoveStripsAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction.ActionType;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent
{

  // The plan being executed
  private Stack <StripsAction> plan = null;

  // maps the real unit Ids to the plan's unit ids
  // when you're planning you won't know the true unit IDs that sepia assigns.
  // So you'll use placeholders (1, 2, 3).
  // this maps those placeholders to the actual unit IDs.

  private Map <Integer, Integer> peasantIdMap;
  private int townhallId;
  private int peasantTemplateId;
  private int requiredGold;
  private int requiredWood;
  private boolean buildPeasants;

  public PEAgent (int playernum, Stack <StripsAction> plan, int requiredWood, int requiredGold, boolean buildPeasants)
  {
    super (playernum);
    peasantIdMap = new HashMap <Integer, Integer> ();
    this.plan = plan;
    this.requiredGold = requiredGold;
    this.requiredWood = requiredWood;
    this.buildPeasants = buildPeasants;
  }

  @Override
  public Map <Integer, Action> initialStep (State.StateView stateView, History.HistoryView historyView)
  {
    // gets the townhall ID and the peasant ID
    for (int unitId : stateView.getUnitIds (playernum))
    {
      Unit.UnitView unit = stateView.getUnit (unitId);
      String unitType = unit.getTemplateView ().getName ().toLowerCase ();
      if (unitType.equals ("townhall"))
      {
        townhallId = unitId;
      }
      else if (unitType.equals ("peasant"))
      {
        peasantIdMap.put (unitId, unitId);
      }
    }

    // Gets the peasant template ID. This is used when building a new
    // peasant with the townhall
    for (Template.TemplateView templateView : stateView.getTemplates (playernum))
    {
      if (templateView.getName ().toLowerCase ().equals ("peasant"))
      {
        peasantTemplateId = templateView.getID ();
        break;
      }
    }

    return middleStep (stateView, historyView);
  }

  /**
   * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
   * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute then
   * either your plan is incorrect or your execution of the plan has a bug.
   *
   * You can create a SEPIA deposit action with the following method Action.createPrimitiveDeposit(int peasantId,
   * Direction townhallDirection)
   *
   * You can create a SEPIA harvest action with the following method Action.createPrimitiveGather(int peasantId,
   * Direction resourceDirection)
   *
   * You can create a SEPIA build action with the following method Action.createPrimitiveProduction(int townhallId, int
   * peasantTemplateId)
   *
   * You can create a SEPIA move action with the following method Action.createCompoundMove(int peasantId, int x, int y)
   *
   * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
   *
   * For the compound actions you will need to check their progress and wait until they are complete before issuing
   * another action for that unit. If you issue an action before the compound action is complete then the peasant will
   * stop what it was doing and begin executing the new action.
   *
   * To check an action's progress you can call getCurrentDurativeAction on each UnitView. If the Action is null nothing
   * is being executed. If the action is not null then you should also call getCurrentDurativeProgress. If the value is
   * less than 1 then the action is still in progress.
   *
   * Also remember to check your plan's preconditions before executing!
   */
  @Override
  public Map <Integer, Action> middleStep (State.StateView stateView, History.HistoryView historyView)
  {
    List <Integer> busyIDs = new ArrayList <Integer> ();
    Map <Integer, ActionResult> actionResults = historyView.getCommandFeedback (playernum,
                                                                                stateView.getTurnNumber () - 1);

    for (Entry <Integer, ActionResult> resEntry : actionResults.entrySet ())
    {
      if (resEntry.getValue ().getFeedback () == ActionFeedback.INCOMPLETE)
      {
        busyIDs.add (resEntry.getKey ());
      }
    }

    HashMap <Integer, Action> result = new HashMap <Integer, Action> ();
    StripsAction nextAction = plan.peek ();
    GameState state = new GameState (stateView, playernum, requiredGold, requiredWood, buildPeasants);
    while (actionCanHappen (nextAction, busyIDs, state))
    {
      plan.pop ();
      Map <Integer, Action> sepiaActions = createSepiaAction (nextAction, state);
      busyIDs.addAll (sepiaActions.keySet ());
      for (Entry <Integer, Action> entries : sepiaActions.entrySet ())
      {
        result.put (entries.getKey (), entries.getValue ());
      }
      nextAction = plan.peek ();
    }
    return result;
  }

  private boolean actionCanHappen (StripsAction nextAction, List <Integer> busyIDs, GameState state)
  {
    if (nextAction == null)
    {
      return false;
    }
    else if (!nextAction.preconditionsMet (state))
    {
      return false;
    }
    else
    {
      for (Integer id : nextAction.getPeasantIdsForAction (state))
      {
        if (busyIDs.contains (id))
        {
          return false;
        }
      }
      return true;
    }
  }

  boolean first = true;

  /**
   * Returns a SEPIA version of the specified Strips Action.
   * 
   * @param action
   *          StripsAction
   * @return SEPIA representation of same action
   */
  private Map <Integer, Action> createSepiaAction (StripsAction action, GameState gameState)
  {// make it also take a gamestate
    HashMap <Integer, Action> result = new HashMap <Integer, Action> ();
    boolean precons = action.preconditionsMet (gameState);// check for
    // preconditions
    if (precons)
    {
      List <Integer> peasantsToUse = action.getPeasantIdsForAction (gameState);
      ActionType actionType = action.getActionType ();

      if (actionType == ActionType.MOVE)
      {
        for (int i = 0; i < peasantsToUse.size (); i++)
        {
          result.put (i, // should this i also be turned into
                      // peasantsToUse.get (i)?
                      Action.createCompoundMove (peasantsToUse.get (i),
                                                 ((MoveStripsAction) action).getDestination ().getXCoord (),
                                                 ((MoveStripsAction) action).getDestination ().getYCoord ()));
        }
      }
      else if (actionType == ActionType.GATHER)
      {
        for (int i = 0; i < peasantsToUse.size (); i++)
        {
          result.put (i, Action.createCompoundGather (peasantsToUse.get (i), targetid));// how to find
          // targetID?;
        }
      }
      else if (actionType == ActionType.DEPOSIT)
      {
        for (int i = 0; i < peasantsToUse.size (); i++)
        {
          result.put (peasantsToUse.get (i), Action.createCompoundDeposit (peasantsToUse.get (i), targetid));// how to
          // find
          // targetID?
        }
      }
      else // Action type is Build Peasant
      {
        result.put (0, Action.createCompoundBuild (unitid, templateID, x, y));// where
        // to
        // suppy
        // these
        // arguments?
      }
    }
    return result;
  }

  @Override
  public void terminalStep (State.StateView stateView, History.HistoryView historyView)
  {

  }

  @Override
  public void savePlayerData (OutputStream outputStream)
  {

  }

  @Override
  public void loadPlayerData (InputStream inputStream)
  {

  }
}
