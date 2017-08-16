package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class VariableInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(VariableInstanceEntityHandler.class.getName());

	public VariableInstanceEntityHandler(OrientGraph g) {
		super( g, VariableInstanceEntity.class);
	}
}
