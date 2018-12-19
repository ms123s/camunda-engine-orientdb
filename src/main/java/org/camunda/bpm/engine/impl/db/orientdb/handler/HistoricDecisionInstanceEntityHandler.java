package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricDecisionInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDecisionInstanceEntityHandler.class.getName());

	public HistoricDecisionInstanceEntityHandler(ODatabaseSession g) {
		super( g, HistoricDecisionInstanceEntity.class);
	}
}
