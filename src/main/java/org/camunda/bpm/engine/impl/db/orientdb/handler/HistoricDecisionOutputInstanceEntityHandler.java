package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricDecisionOutputInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDecisionOutputInstanceEntityHandler.class.getName());

	public HistoricDecisionOutputInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricDecisionOutputInstanceEntity.class);
	}
}
