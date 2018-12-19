package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricJobLogEvent;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricJobLogEventHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricJobLogEventHandler.class.getName());

	public HistoricJobLogEventHandler(ODatabaseSession g) {
		super( g, HistoricJobLogEvent.class);
	}
}
