package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Manfred Sattler
 */
public class VariableInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger LOG = Logger.getLogger(VariableInstanceEntityHandler.class.getName());

	public VariableInstanceEntityHandler(ODatabaseSession g) {
		super( g, VariableInstanceEntity.class);
	}
	@Override
	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
	  settingLinksReverse(entity, "getExecutionId", "ExecutionEntity", "variables", v, entityCache);
	  settingLinksReverse(entity, "getTaskId", "TaskEntity", "variables", v, entityCache);
	}
}
