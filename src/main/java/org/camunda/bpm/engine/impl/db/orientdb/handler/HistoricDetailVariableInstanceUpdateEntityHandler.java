package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricDetailVariableInstanceUpdateEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDetailVariableInstanceUpdateEntityHandler.class.getName());

	public HistoricDetailVariableInstanceUpdateEntityHandler(ODatabaseSession g) {
		super( g, HistoricDetailVariableInstanceUpdateEntity.class);
	}
}
