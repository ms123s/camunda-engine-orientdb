package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricVariableUpdateEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricVariableUpdateEventEntityHandler.class.getName());

	public HistoricVariableUpdateEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricVariableUpdateEventEntity.class);
	}
}
