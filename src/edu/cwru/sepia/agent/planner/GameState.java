package edu.cwru.sepia.agent.planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cwru.sepia.agent.planner.actions.DepositStripsAction;
import edu.cwru.sepia.agent.planner.actions.GatherStripsAction;
import edu.cwru.sepia.agent.planner.actions.MoveStripsAction;
import edu.cwru.sepia.agent.planner.actions.ProductionStripsAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

/**
 * This class is used to represent the state of the game after applying one of
 * the avaiable actions. It will also track the A* specific information such as
 * the parent pointer and the cost and heuristic function. Remember that unlike
 * the path planning A* from the first assignment the cost of an action may be
 * more than 1. Specifically the cost of executing a compound action such as
 * move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2).
 * Implement the methods provided and add any other methods and member variables
 * you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState
 * in this class using whatever class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

	private final int playernum;
	private final int requiredGold;
	private final int requiredWood;
	private final boolean buildPeasants;
	private final Map<Integer, PeasantState> peasantStates;
	private final List<ResourceState> trees;
	private final List<ResourceState> gold;
	private Position townHall;
	private final int currentWood;
	private final int currentGold;
	private final StripsAction previousAction;
	private final double cost;
	private final int xExtent;
	private final int yExtent;
	private final GameState parent;
	private final int townHallID;

	public GameState(int playernum, int requiredGold, int requiredWood,
			boolean buildPeasants, Map<Integer, PeasantState> peasantStates,
			List<ResourceState> trees, List<ResourceState> gold,
			Position townHall, int currentWood, int currentGold,
			StripsAction previousAction, double cost, int xExtent, int yExtent,
			GameState parent, int townHallID) {
		this.playernum = playernum;
		this.requiredGold = requiredGold;
		this.requiredWood = requiredWood;
		this.buildPeasants = buildPeasants;
		this.peasantStates = peasantStates;
		this.trees = new ArrayList<ResourceState>(trees);
		this.gold = new ArrayList<ResourceState>(gold);
		this.townHall = townHall;
		this.currentWood = currentWood;
		this.currentGold = currentGold;
		this.previousAction = previousAction;
		this.cost = cost;
		this.xExtent = xExtent;
		this.yExtent = yExtent;
		this.parent = parent;
		this.townHallID = townHallID;
	}

	/**
	 * Construct a GameState from a stateview object. This is used to construct
	 * the initial search node. All other nodes should be constructed from the
	 * another constructor you create or by factory functions that you create.
	 *
	 * @param state
	 *            The current stateview at the time the plan is being created
	 * @param playernum
	 *            The player number of agent that is planning
	 * @param requiredGold
	 *            The goal amount of gold (e.g. 200 for the small scenario)
	 * @param requiredWood
	 *            The goal amount of wood (e.g. 200 for the small scenario)
	 * @param buildPeasants
	 *            True if the BuildPeasant action should be considered
	 */
	public GameState(State.StateView state, int playernum, int requiredGold,
			int requiredWood, boolean buildPeasants) {
		this.playernum = playernum;
		this.requiredGold = requiredGold;
		this.requiredWood = requiredWood;
		this.buildPeasants = buildPeasants;
		this.peasantStates = new HashMap<Integer, PeasantState>();
		this.trees = new ArrayList<ResourceState>();
		this.gold = new ArrayList<ResourceState>();
		for (ResourceView resource : state.getAllResourceNodes()) {
			if (resource.getType().equals(Type.GOLD_MINE)) {
				gold.add(new ResourceState(resource));
			}
			if (resource.getType().equals(Type.TREE)) {
				trees.add(new ResourceState(resource));
			}
		}
		int tHallID = 0;
		for (UnitView unit : state.getUnits(playernum)) {
			if (unit.getTemplateView().getName().equals("TownHall")) {
				townHall = new Position(unit.getXPosition(),
						unit.getYPosition());
				tHallID = unit.getID();
			}
			if (unit.getTemplateView().getName().equals("Peasant")) {
				peasantStates
				.put(unit.getID(),
						new PeasantState(unit.getID(), unit
								.getCargoAmount(), unit.getCargoType(),
								new Position(unit.getXPosition(), unit
										.getYPosition())));
			}
		}
		this.townHallID = tHallID;
		this.currentGold = state
				.getResourceAmount(playernum, ResourceType.GOLD);
		this.currentWood = state
				.getResourceAmount(playernum, ResourceType.WOOD);
		this.previousAction = null;
		this.cost = 0;
		this.xExtent = state.getXExtent();
		this.yExtent = state.getYExtent();
		this.parent = null;
	}

	/**
	 * Unlike in the first A* assignment there are many possible goal states. As
	 * long as the wood and gold requirements are met the peasants can be at any
	 * location and the capacities of the resource locations can be anything.
	 * Use this function to check if the goal conditions are met and return true
	 * if they are.
	 *
	 * @return true if the goal conditions are met in this instance of game
	 *         state.
	 */
	public boolean isGoal() {
		return requiredGold <= currentGold && requiredWood <= currentWood;
	}

	/**
	 * The branching factor of this search graph are much higher than the
	 * planning. Generate all of the possible successor states and their
	 * associated actions in this method.
	 *
	 * @return A list of the possible successor states and their associated
	 *         actions
	 */
	public List<GameState> generateChildren() {
		List<StripsAction> actions = new ArrayList<StripsAction>();
		// For each possible peasant count
		for (int i = 1; i <= optimalPeasantCount(); i++) {
			// Create a movement to/from and gather for each resource
			for (Position resource : getAllResourcePositions()) {
				actions.add(new MoveStripsAction(i, townHall, resource));
				actions.add(new MoveStripsAction(i, resource, townHall));
				actions.add(new GatherStripsAction(i, resource));
			}
			// Have i peasants deposit at the townhall
			actions.add(new DepositStripsAction(i, townHall));
		}
		// Build a peasant
		actions.add(new ProductionStripsAction());
		List<GameState> children = new ArrayList<GameState>();
		// Filter actions that have not met preonditions then create children
		// states
		actions.stream().filter((a) -> a.preconditionsMet(this))
		.forEach((a) -> children.add(a.apply(this)));
		return children;
	}

	private Collection<Position> getAllResourcePositions() {
		Set<Position> positions = new HashSet<Position>();
		trees.stream().forEach(t -> positions.add(t.getPostion()));
		gold.stream().forEach(t -> positions.add(t.getPostion()));
		return positions;
	}

	/**
	 * Assuming that move, gather, deposit and creating a peasant all take one
	 * action, it will take one peasant 36 actions to gather 900 resources.
	 * 
	 * This will also take 36 actions if a second peasant is built. First it
	 * takes 16 actions to gather 400 gold. Then one action to build a peasant.
	 * The original peasant will begin it's second set of 16 actions
	 * simultaneously to this action. When the second peasant is built it will
	 * take 16 actions to gather 400 resources. This will finish one action
	 * after the original peasant deposits its 400. The original peasant is
	 * taking it's move action to the last resource and it will finish that
	 * move-gather-move-deposit cycle in 3 more actions for a total of 36. Thus
	 * for more than 900 total resources, 2 peasants is better than 1.
	 * 
	 * Using the same logic, a resource total of 1300 will take 44 turns for two
	 * peasants or three peasants. After this it will be optimal to use three
	 * peasants.
	 * 
	 * @return
	 */
	private int optimalPeasantCount() {
		if (!buildPeasants) {
			return 1;
		}
		int totalResources = requiredGold + requiredWood;
		if (totalResources < 900) {
			return 1;
		} else if (totalResources < 1300) {
			return 2;
		} else {
			return 3;
		}
	}

	/**
	 * Write your heuristic function here. Remember this must be admissible for
	 * the properties of A* to hold. If you can come up with an easy way of
	 * computing a consistent heuristic that is even better, but not strictly
	 * necessary.
	 *
	 * This function figures out how many cycles of move-gather-move-deposit are
	 * needed to get the remaining required resources and then multiplies this
	 * by 15, which equates to a round trip of 13 moves. If the previous action
	 * was one that was completed by multiple peasants, a bonus is applied
	 * because it is assumed that these peasants will be able to work together
	 * for the following moves. Building a new peasant is weighted the same as 4
	 * gold runs, as long as the current number of peasants is not the same as
	 * the optimal number of peasants.
	 *
	 * @return The value estimated remaining cost to reach a goal state from
	 *         this state.
	 */
	public double heuristic() {
		int hVal = 0;

		// Same weight per peasant as 4 gold runs
		hVal += (desiredPeasantNumber() - peasantStates.size()) * 15 * 4;

		// Number of times a peasant must complete a move-gather-move-deposit
		// cycle to gather the remaining gold. Assume that the peasants from the
		// previous action will work in harmony.
		int goldRuns = Math.max(0, requiredGold - currentGold)
				/ (100 * Math.max(1, previousAction.getPeasantCount()));

		// Number of times a peasant must complete a move-gather-move-deposit
		// cycle to gather the remaining wood. Assume that the peasants from the
		// previous action will work in harmony.
		int woodRuns = Math.max(0, requiredWood - currentWood)
				/ (100 * Math.max(1, previousAction.getPeasantCount()));

		// Estimate ~7 steps to be the distance from a resource
		hVal += 15 * (goldRuns + woodRuns);

		return hVal;
	}

	public StripsAction getPreviousAction() {
		return previousAction;
	}

	/**
	 * 
	 * Write the function that computes the current cost to get to this node.
	 * This is combined with your heuristic to determine which actions/states
	 * are better to explore.
	 *
	 * @return The current cost to reach this goal
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * This is necessary to use your state in the Java priority queue. See the
	 * official priority queue and Comparable interface documentation to learn
	 * how this function should work.
	 *
	 * @param o
	 *            The other game state to compare
	 * @return 1 if this state costs more than the other, 0 if equal, -1
	 *         otherwise
	 */
	@Override
	public int compareTo(GameState o) {
		Double thisVal = getCost() + heuristic();
		Double otherVal = o.getCost() + o.heuristic();
		return thisVal.compareTo(otherVal);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameState other = (GameState) obj;
		if (currentGold != other.currentGold)
			return false;
		if (currentWood != other.currentWood)
			return false;
		if (peasantStates == null) {
			if (other.peasantStates != null)
				return false;
		} else if (!peasantStates.equals(other.peasantStates))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currentGold;
		result = prime * result + currentWood;
		result = prime * result
				+ ((peasantStates == null) ? 0 : peasantStates.hashCode());
		return result;
	}

	public Position getPeasantPosition(int peasantId) {
		return peasantStates.get(peasantId).getPosition();
	}

	public int getXExtent() {
		return xExtent;
	}

	public int getYExtent() {
		return yExtent;
	}

	/**
	 * Gets all positions that are occupied by a resource, townhall or peasant.
	 * 
	 * @return A List containing Positions of occupied spaces.
	 */
	public List<Position> getOccupiedPositions() {
		List<Position> allPositions = new ArrayList<Position>();
		for (ResourceState state : trees) {
			allPositions.add(state.getPostion());
		}
		for (ResourceState state : gold) {
			allPositions.add(state.getPostion());
		}
		allPositions.add(townHall);
		List<Position> peasantPos = new ArrayList<Position>();
		getPeasants().stream().forEach((p) -> peasantPos.add(p.getPosition()));
		allPositions.addAll(peasantPos);
		return allPositions;
	}

	public GameState applyAction(StripsAction stripsAction) {
		switch (stripsAction.getActionType()) {
		// Adds a new peasant and deducts 400 gold
		case BUILD_PEASANT:
			ProductionStripsAction action = (ProductionStripsAction) stripsAction;
			int newID = getValidIDForNewPeasant();
			Position newPosition = getValidPositionForNewPeasant();
			Collection<PeasantState> peasants = getPeasants();
			peasants.add(new PeasantState(newID, 0, null, newPosition));

			return new GameState(playernum, requiredGold, requiredWood,
					buildPeasants, buildPeasantMap(peasants), trees, gold,
					townHall, currentWood, currentGold - 400, action, getCost()
					+ action.getActionCost(), xExtent, yExtent, this,
					townHallID);

			// Has the specified peasants deposit their goods at the TownHall
		case DEPOSIT:
			DepositStripsAction deposit = (DepositStripsAction) stripsAction;
			List<Integer> ids = deposit.getPeasantIdsForAction(this);
			Collection<PeasantState> updatedPeasants = getPeasants();
			int newWood = currentWood;
			int newGold = currentGold;
			// For each peasant
			for (Integer id : ids) {
				PeasantState peasant = getPeasant(id);
				updatedPeasants.remove(peasant);
				// Incremennt the wood or gold
				if (peasant.getCargoType() == ResourceType.WOOD) {
					newWood += peasant.getCargoAmount();
				} else if (peasant.getCargoType() == ResourceType.GOLD) {
					newGold += peasant.getCargoAmount();
				}
				// Update the peasant to have no cargo
				updatedPeasants.add(new PeasantState(id, 0, null, peasant
						.getPosition()));
			}
			return new GameState(playernum, requiredGold, requiredWood,
					buildPeasants, buildPeasantMap(updatedPeasants), trees,
					gold, townHall, newWood, newGold, deposit, getCost()
					+ deposit.getActionCost(), xExtent, yExtent, this,
					townHallID);

			// Has the specified peasants gather from a given resource
		case GATHER:
			GatherStripsAction gather = (GatherStripsAction) stripsAction;
			List<Integer> gatherIds = gather.getPeasantIdsForAction(this);
			Collection<PeasantState> gatherUpdatedPeasants = getPeasants();
			ResourceState resource = getResourceByPosition(gather
					.getResourcePosition());
			int remaining = resource.getRemaining();
			// For each peasant
			for (Integer id : gatherIds) {
				PeasantState peasant = getPeasant(id);
				gatherUpdatedPeasants.remove(peasant);
				// decrement resources remaining count
				remaining -= 100;
				// Add new peasant with cargo
				gatherUpdatedPeasants.add(new PeasantState(id, 100, resource
						.getType(), peasant.getPosition()));
			}

			List<ResourceState> newTrees = getTreeCopy();
			List<ResourceState> newGolds = getGoldCopy();
			// Update the resource
			if (resource.getType() == ResourceType.WOOD) {
				newTrees.remove(resource);
				newTrees.add(new ResourceState(resource.getPostion(), resource
						.getType(), remaining, resource.getResourceId()));
			} else {
				newGolds.remove(resource);
				newGolds.add(new ResourceState(resource.getPostion(), resource
						.getType(), remaining, resource.getResourceId()));
			}

			return new GameState(playernum, requiredGold, requiredWood,
					buildPeasants, buildPeasantMap(gatherUpdatedPeasants),
					newTrees, newGolds, townHall, currentWood, currentGold,
					gather, getCost() + gather.getActionCost(), xExtent,
					yExtent, this, townHallID);

			// Move the specified peasants
		case MOVE:
			MoveStripsAction move = (MoveStripsAction) stripsAction;
			List<Integer> moveIds = move.getPeasantIdsForAction(this);
			Collection<PeasantState> moveUpdatedPeasants = getPeasants();
			Position dest = move.getDestination();
			// For each peasant
			for (Integer id : moveIds) {
				PeasantState peasant = getPeasant(id);
				moveUpdatedPeasants.remove(peasant);
				// Update the peasant location.
				moveUpdatedPeasants.add(new PeasantState(id, peasant
						.getCargoAmount(), peasant.getCargoType(), dest));
			}

			return new GameState(playernum, requiredGold, requiredWood,
					buildPeasants, buildPeasantMap(moveUpdatedPeasants), trees,
					gold, townHall, currentWood, currentGold, move, getCost()
					+ move.getActionCost(), xExtent, yExtent, this,
					townHallID);
		default:
			throw new RuntimeException("Default reached on switch statement.");
		}
	}

	public int getTownHallID() {
		return townHallID;
	}

	private Position getValidPositionForNewPeasant() {
		List<Position> occupied = getOccupiedPositions();
		for (Direction dir : Direction.values()) {
			if (!occupied.contains(townHall.move(dir))) {
				return townHall.move(dir);
			}
		}
		return null;
	}

	private int getValidIDForNewPeasant() {
		int max = -1;
		for (int i : peasantStates.keySet()) {
			max = Math.max(i, max);
		}
		return max + 1;
	}

	private List<ResourceState> getTreeCopy() {
		return new ArrayList<ResourceState>(trees);
	}

	private List<ResourceState> getGoldCopy() {
		return new ArrayList<ResourceState>(gold);
	}

	public ResourceState getResourceByPosition(Position resourcePosition) {
		for (ResourceState resource : gold) {
			if (resource.getPostion().equals(resourcePosition)) {
				return resource;
			}
		}
		for (ResourceState resource : trees) {
			if (resource.getPostion().equals(resourcePosition)) {
				return resource;
			}
		}
		return null;
	}

	private Map<Integer, PeasantState> buildPeasantMap(
			Collection<PeasantState> updatedPeasants) {
		Map<Integer, PeasantState> newMap = new HashMap<Integer, PeasantState>();
		for (PeasantState state : updatedPeasants) {
			newMap.put(state.getId(), state);
		}
		return newMap;
	}

	public Position getTownHallPosition() {
		return townHall;
	}

	public List<Position> getNonEmptyResourcePositions() {
		List<Position> positions = new ArrayList<Position>();
		for (ResourceState state : trees) {
			if (state.getRemaining() > 0) {
				positions.add(state.getPostion());
			}
		}
		for (ResourceState state : gold) {
			if (state.getRemaining() > 0) {
				positions.add(state.getPostion());
			}
		}
		return positions;
	}

	public PeasantState getPeasant(int peasantID) {
		return peasantStates.get(peasantID);
	}

	public Collection<PeasantState> getPeasants() {
		return new HashSet<PeasantState>(peasantStates.values());
	}

	public int getCurrentGold() {
		return currentGold;
	}

	public int getCurrentFood() {
		return 3 - peasantStates.size();
	}

	public int desiredPeasantNumber() {
		return optimalPeasantCount();
	}

	public GameState getParent() {
		return parent;
	}

	public int getCurrentWood() {
		return currentWood;
	}

	public int getRequiredWood() {
		return requiredWood;
	}

	public int getRequiredGold() {
		return requiredGold;
	}
}
