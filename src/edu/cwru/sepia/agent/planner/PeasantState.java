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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cargoAmount;
		result = prime * result
				+ ((cargoType == null) ? 0 : cargoType.hashCode());
		result = prime * result + id;
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PeasantState other = (PeasantState) obj;
		if (cargoAmount != other.cargoAmount)
			return false;
		if (cargoType != other.cargoType)
			return false;
		if (id != other.id)
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PeasantState [id=" + id + ", cargoAmount=" + cargoAmount
				+ ", cargoType=" + cargoType + ", position=" + position + "]";
	}

}
