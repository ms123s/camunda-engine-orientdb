package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class CaseExecutionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(CaseExecutionEntityHandler.class.getName());

	public CaseExecutionEntityHandler(OrientGraph g) {
		super( g, CaseExecutionEntity.class);
	}
}
