package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricDecisionInputInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDecisionInputInstanceEntityHandler.class.getName());

	public HistoricDecisionInputInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricDecisionInputInstanceEntity.class);
	}
}
