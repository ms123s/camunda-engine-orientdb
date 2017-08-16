package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class ExecutionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ExecutionEntityHandler.class.getName());

	public ExecutionEntityHandler(OrientGraph g) {
		super( g, ExecutionEntity.class);
	}
}
