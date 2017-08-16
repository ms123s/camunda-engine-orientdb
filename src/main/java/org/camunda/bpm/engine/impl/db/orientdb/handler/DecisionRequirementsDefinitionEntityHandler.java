package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.DecisionRequirementsDefinitionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class DecisionRequirementsDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DecisionRequirementsDefinitionEntityHandler.class.getName());

	public DecisionRequirementsDefinitionEntityHandler(OrientGraph g) {
		super( g, DecisionRequirementsDefinitionEntity.class);
	}
}
