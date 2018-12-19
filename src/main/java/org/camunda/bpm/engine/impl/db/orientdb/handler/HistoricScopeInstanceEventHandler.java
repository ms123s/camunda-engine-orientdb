package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricScopeInstanceEvent;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricScopeInstanceEventHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricScopeInstanceEventHandler.class.getName());

	public HistoricScopeInstanceEventHandler(ODatabaseSession g) {
		super( g, HistoricScopeInstanceEvent.class);
	}
}
