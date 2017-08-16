package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class TenantEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(TenantEntityHandler.class.getName());

	public TenantEntityHandler(OrientGraph g) {
		super( g, TenantEntity.class);
	}
}
