package edu.cwru.sepia.agent.planner;

import java.net.Authenticator.RequestorType;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;

public class ResourceState {

	private final Position postion;
	private final Type type;
	private int remaining;
	
	public ResourceState(ResourceView resource) {
		this.postion = new Position(resource.getXPosition(), resource.getYPosition());
		this.type = resource.getType();
		this.remaining = resource.getAmountRemaining();
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

	public Type getType() {
		return type;
	}
	
}
