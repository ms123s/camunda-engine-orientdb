package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoryEvent;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoryEventHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoryEventHandler.class.getName());

	public HistoryEventHandler(OrientGraph g) {
		super( g, HistoryEvent.class);
	}
}
