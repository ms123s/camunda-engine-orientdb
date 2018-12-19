package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.TenantMembershipEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class TenantMembershipEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(TenantMembershipEntityHandler.class.getName());

	public TenantMembershipEntityHandler(ODatabaseSession g) {
		super( g, TenantMembershipEntity.class);
	}
}
