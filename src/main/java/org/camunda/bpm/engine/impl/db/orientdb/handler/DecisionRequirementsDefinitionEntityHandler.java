package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class DecisionRequirementsDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DecisionRequirementsDefinitionEntityHandler.class.getName());

	public DecisionRequirementsDefinitionEntityHandler(ODatabaseSession g) {
		super( g, DecisionRequirementsDefinitionEntity.class);
	}
}
