package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricDecisionInputInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDecisionInputInstanceEntityHandler.class.getName());

	public HistoricDecisionInputInstanceEntityHandler(ODatabaseSession g) {
		super( g, HistoricDecisionInputInstanceEntity.class);
	}
}
