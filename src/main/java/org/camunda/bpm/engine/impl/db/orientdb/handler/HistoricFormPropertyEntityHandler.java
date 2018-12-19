package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricFormPropertyEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricFormPropertyEntityHandler.class.getName());

	public HistoricFormPropertyEntityHandler(ODatabaseSession g) {
		super( g, HistoricFormPropertyEntity.class);
	}
}
