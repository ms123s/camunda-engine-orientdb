package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricIdentityLinkLogEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricIdentityLinkLogEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricIdentityLinkLogEntityHandler.class.getName());

	public HistoricIdentityLinkLogEntityHandler(OrientGraph g) {
		super( g, HistoricIdentityLinkLogEntity.class);
	}
}
