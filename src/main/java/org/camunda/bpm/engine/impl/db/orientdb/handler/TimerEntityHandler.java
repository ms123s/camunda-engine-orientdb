package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class TimerEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(TimerEntityHandler.class.getName());

	public TimerEntityHandler(OrientGraph g) {
		super( g, TimerEntity.class);
	}
}
