package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class ResourceState {

	private final Position postion;
	private final ResourceType type;
	private int remaining;

	public ResourceState(Position postion, ResourceType type, int remaining) {
		this.postion = postion;
		this.type = type;
		this.remaining = remaining;
	}

	public ResourceState(ResourceView resource) {
		this.postion = new Position(resource.getXPosition(),
				resource.getYPosition());
		this.type = convertType(resource.getType());
		this.remaining = resource.getAmountRemaining();
	}

	private ResourceType convertType(Type type2) {
		if (type2 == Type.GOLD_MINE) {
			return ResourceType.GOLD;
		} else {
			return ResourceType.WOOD;
		}
	}

	public int getRemaining() {
		return remaining;
	}

	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}

	public Position getPostion() {
		return postion;
	}

	public ResourceType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((postion == null) ? 0 : postion.hashCode());
		result = prime * result + remaining;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ResourceState other = (ResourceState) obj;
		if (postion == null) {
			if (other.postion != null)
				return false;
		} else if (!postion.equals(other.postion))
			return false;
		if (remaining != other.remaining)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResourceState [postion=" + postion + ", type=" + type
				+ ", remaining=" + remaining + "]";
	}

}
