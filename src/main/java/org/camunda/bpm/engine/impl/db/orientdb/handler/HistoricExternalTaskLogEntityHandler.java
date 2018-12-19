package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricExternalTaskLogEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricExternalTaskLogEntityHandler.class.getName());

	public HistoricExternalTaskLogEntityHandler(ODatabaseSession g) {
		super( g, HistoricExternalTaskLogEntity.class);
	}
}
