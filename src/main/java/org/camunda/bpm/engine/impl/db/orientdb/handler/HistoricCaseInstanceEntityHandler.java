package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricCaseInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricCaseInstanceEntityHandler.class.getName());

	public HistoricCaseInstanceEntityHandler(ODatabaseSession g) {
		super( g, HistoricCaseInstanceEntity.class);
	}
}
