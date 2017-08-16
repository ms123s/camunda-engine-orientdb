package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricIncidentEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricIncidentEventEntityHandler.class.getName());

	public HistoricIncidentEventEntityHandler(OrientGraph g) {
		super( g, HistoricIncidentEventEntity.class);
	}
}
