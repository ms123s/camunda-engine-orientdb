package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class UserOperationLogEntryEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(UserOperationLogEntryEventEntityHandler.class.getName());

	public UserOperationLogEntryEventEntityHandler(ODatabaseSession g) {
		super( g, UserOperationLogEntryEventEntity.class);
	}
}
