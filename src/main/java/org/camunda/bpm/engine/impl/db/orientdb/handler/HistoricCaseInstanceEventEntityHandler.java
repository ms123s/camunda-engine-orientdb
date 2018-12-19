package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricCaseInstanceEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricCaseInstanceEventEntityHandler.class.getName());

	public HistoricCaseInstanceEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricCaseInstanceEventEntity.class);
	}
}
