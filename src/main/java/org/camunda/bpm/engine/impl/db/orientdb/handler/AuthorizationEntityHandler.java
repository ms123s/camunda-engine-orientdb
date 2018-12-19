package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class AuthorizationEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(AuthorizationEntityHandler.class.getName());

	public AuthorizationEntityHandler(ODatabaseSession g) {
		super( g, AuthorizationEntity.class);
	}
}
