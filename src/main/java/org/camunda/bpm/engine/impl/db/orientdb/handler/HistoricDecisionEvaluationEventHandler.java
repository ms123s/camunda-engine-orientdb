package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDecisionEvaluationEvent;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricDecisionEvaluationEventHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDecisionEvaluationEventHandler.class.getName());

	public HistoricDecisionEvaluationEventHandler(ODatabaseSession g) {
		super( g, HistoricDecisionEvaluationEvent.class);
	}
}
