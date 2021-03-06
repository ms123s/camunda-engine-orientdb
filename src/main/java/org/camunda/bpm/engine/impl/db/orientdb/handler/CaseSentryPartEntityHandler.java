package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class CaseSentryPartEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(CaseSentryPartEntityHandler.class.getName());

	public CaseSentryPartEntityHandler(ODatabaseSession g) {
		super( g, CaseSentryPartEntity.class);
	}
}
