package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class IdentityInfoEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(IdentityInfoEntityHandler.class.getName());

	public IdentityInfoEntityHandler(ODatabaseSession g) {
		super( g, IdentityInfoEntity.class);
	}
}
