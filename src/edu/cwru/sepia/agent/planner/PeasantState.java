package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;

public class PeasantState {

	private final int id;
	private final int cargoAmount;
	private final ResourceType cargoType;
	private final Position position;

	public PeasantState(int id, int cargoAmount, ResourceType cargoType,
			Position position) {
		this.id = id;
		this.cargoAmount = cargoAmount;
		this.cargoType = cargoType;
		this.position = position;
	}

	public int getId() {
		return id;
	}

	public int getCargoAmount() {
		return cargoAmount;
	}

	public ResourceType getCargoType() {
		return cargoType;
	}

	public Position getPosition() {
		return position;
	}
	
}
