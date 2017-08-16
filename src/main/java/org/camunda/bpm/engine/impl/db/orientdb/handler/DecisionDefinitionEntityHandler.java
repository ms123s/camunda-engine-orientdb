package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.DecisionDefinitionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class DecisionDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DecisionDefinitionEntityHandler.class.getName());

	public DecisionDefinitionEntityHandler(OrientGraph g) {
		super( g, DecisionDefinitionEntity.class);
	}
}
