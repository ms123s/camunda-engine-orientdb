package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.MembershipEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class MembershipEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(MembershipEntityHandler.class.getName());

	public MembershipEntityHandler(ODatabaseSession g) {
		super( g, MembershipEntity.class);
	}
}
