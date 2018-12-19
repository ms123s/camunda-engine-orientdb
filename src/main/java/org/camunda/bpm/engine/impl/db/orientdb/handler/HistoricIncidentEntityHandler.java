package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricIncidentEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricIncidentEntityHandler.class.getName());

	public HistoricIncidentEntityHandler(ODatabaseSession g) {
		super( g, HistoricIncidentEntity.class);
	}
}
