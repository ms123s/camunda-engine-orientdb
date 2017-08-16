package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDecisionEvaluationEvent;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricDecisionEvaluationEventHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDecisionEvaluationEventHandler.class.getName());

	public HistoricDecisionEvaluationEventHandler(OrientGraph g) {
		super( g, HistoricDecisionEvaluationEvent.class);
	}
}
