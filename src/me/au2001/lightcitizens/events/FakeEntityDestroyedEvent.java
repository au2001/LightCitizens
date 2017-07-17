package me.au2001.lightcitizens.events;

import me.au2001.lightcitizens.FakeEntity;

public class FakeEntityDestroyedEvent extends FakeEntityEvent {

	private FakeEntity entity;
	
	public FakeEntityDestroyedEvent(FakeEntity entity) {
		super(entity.getEntityId());
		this.entity = entity;
	}
	
	public FakeEntity getEntity() {
		return entity;
	}

}
