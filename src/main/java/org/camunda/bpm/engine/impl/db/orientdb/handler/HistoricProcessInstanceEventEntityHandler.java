package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricProcessInstanceEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricProcessInstanceEventEntityHandler.class.getName());

	public HistoricProcessInstanceEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricProcessInstanceEventEntity.class);
	}
}
