package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class CaseDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(CaseDefinitionEntityHandler.class.getName());

	public CaseDefinitionEntityHandler(ODatabaseSession g) {
		super( g, CaseDefinitionEntity.class);
	}
}
