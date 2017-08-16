package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableUpdateEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricVariableUpdateEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricVariableUpdateEventEntityHandler.class.getName());

	public HistoricVariableUpdateEventEntityHandler(OrientGraph g) {
		super( g, HistoricVariableUpdateEventEntity.class);
	}
}
