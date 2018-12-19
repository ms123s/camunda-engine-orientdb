package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricDecisionOutputInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDecisionOutputInstanceEntityHandler.class.getName());

	public HistoricDecisionOutputInstanceEntityHandler(ODatabaseSession g) {
		super( g, HistoricDecisionOutputInstanceEntity.class);
	}
}
