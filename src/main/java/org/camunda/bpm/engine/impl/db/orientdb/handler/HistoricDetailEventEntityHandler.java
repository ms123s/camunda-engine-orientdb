package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricDetailEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDetailEventEntityHandler.class.getName());

	public HistoricDetailEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricDetailEventEntity.class);
	}
}
