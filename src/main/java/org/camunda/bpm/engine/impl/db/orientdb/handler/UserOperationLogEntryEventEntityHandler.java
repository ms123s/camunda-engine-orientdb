package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.UserOperationLogEntryEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class UserOperationLogEntryEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(UserOperationLogEntryEventEntityHandler.class.getName());

	public UserOperationLogEntryEventEntityHandler(OrientGraph g) {
		super( g, UserOperationLogEntryEventEntity.class);
	}
}
