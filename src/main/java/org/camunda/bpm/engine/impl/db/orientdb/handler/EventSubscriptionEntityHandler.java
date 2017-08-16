package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class EventSubscriptionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(EventSubscriptionEntityHandler.class.getName());

	public EventSubscriptionEntityHandler(OrientGraph g) {
		super( g, EventSubscriptionEntity.class);
	}
}
