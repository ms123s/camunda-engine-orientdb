package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class DecisionDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DecisionDefinitionEntityHandler.class.getName());

	public DecisionDefinitionEntityHandler(ODatabaseSession g) {
		super( g, DecisionDefinitionEntity.class);
	}
}
