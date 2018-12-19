package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoryEventHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoryEventHandler.class.getName());

	public HistoryEventHandler(ODatabaseSession g) {
		super( g, HistoryEvent.class);
	}
}
