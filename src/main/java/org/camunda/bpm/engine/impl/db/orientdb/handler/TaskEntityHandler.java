package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class TaskEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(TaskEntityHandler.class.getName());

	public TaskEntityHandler(OrientGraph g) {
		super( g, TaskEntity.class);
	}
	@Override
	public void modifyMetadata() {
		setSetterByGetter( "getAssignee", "setAssigneeWithoutCascade");
		setSetterByGetter( "getOwner", "setOwnerWithoutCascade");
		setSetterByGetter( "getDueDate", "setDueDateWithoutCascade");
		setSetterByGetter( "getPriority", "setPriorityWithoutCascade");
		setSetterByGetter( "getParentTaskId", "setParentTaskIdWithoutCascade");
		setSetterByGetter( "getName", "setNameWithoutCascade");
		setSetterByGetter( "getDescription", "setDescriptionWithoutCascade");
		setSetterByGetter( "getTaskDefinitionKey", "setTaskDefinitionKeyWithoutCascade");
		setSetterByGetter( "getDelegationState", "setDelegationStateWithoutCascade");
		setSetterByGetter( "getCaseInstanceId", "setCaseInstanceIdWithoutCascade");
	}
}
