package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricCaseActivityInstanceEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricCaseActivityInstanceEventEntityHandler.class.getName());

	public HistoricCaseActivityInstanceEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricCaseActivityInstanceEventEntity.class);
	}
}
