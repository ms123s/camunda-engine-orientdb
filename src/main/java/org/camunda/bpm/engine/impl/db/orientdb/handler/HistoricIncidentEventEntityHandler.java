package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricIncidentEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricIncidentEventEntityHandler.class.getName());

	public HistoricIncidentEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricIncidentEventEntity.class);
	}
}
