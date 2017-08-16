package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricVariableInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricVariableInstanceEntityHandler.class.getName());

	public HistoricVariableInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricVariableInstanceEntity.class);
	}
}
