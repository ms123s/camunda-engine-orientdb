package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricActivityInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricActivityInstanceEntityHandler.class.getName());

	public HistoricActivityInstanceEntityHandler(ODatabaseSession g) {
		super( g, HistoricActivityInstanceEntity.class);
	}
}
