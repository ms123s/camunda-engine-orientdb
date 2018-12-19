package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricTaskInstanceEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricTaskInstanceEventEntityHandler.class.getName());

	public HistoricTaskInstanceEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricTaskInstanceEventEntity.class);
	}
}
