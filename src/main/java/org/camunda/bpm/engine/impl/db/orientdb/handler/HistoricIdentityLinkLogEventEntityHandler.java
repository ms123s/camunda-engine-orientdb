package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricIdentityLinkLogEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricIdentityLinkLogEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricIdentityLinkLogEventEntityHandler.class.getName());

	public HistoricIdentityLinkLogEventEntityHandler(OrientGraph g) {
		super( g, HistoricIdentityLinkLogEventEntity.class);
	}
}
