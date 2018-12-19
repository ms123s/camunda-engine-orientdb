package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class TenantEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(TenantEntityHandler.class.getName());

	public TenantEntityHandler(ODatabaseSession g) {
		super( g, TenantEntity.class);
	}
}
