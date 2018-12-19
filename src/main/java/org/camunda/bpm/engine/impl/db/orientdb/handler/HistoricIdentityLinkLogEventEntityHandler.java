package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class HistoricIdentityLinkLogEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricIdentityLinkLogEventEntityHandler.class.getName());

	public HistoricIdentityLinkLogEventEntityHandler(ODatabaseSession g) {
		super( g, HistoricIdentityLinkLogEventEntity.class);
	}
}
