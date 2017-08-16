package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricFormPropertyEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricFormPropertyEventEntityHandler.class.getName());

	public HistoricFormPropertyEventEntityHandler(OrientGraph g) {
		super( g, HistoricFormPropertyEventEntity.class);
	}
}
