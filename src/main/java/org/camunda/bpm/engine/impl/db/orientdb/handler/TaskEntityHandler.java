package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Operator.IN;
import com.github.raymanrt.orientqb.query.Clause;
import static com.github.raymanrt.orientqb.query.Clause.or;

/**
 * @author Manfred Sattler
 */
public class TaskEntityHandler extends BaseEntityHandler {
	private final static Logger LOG = Logger.getLogger(TaskEntityHandler.class.getName());

	public TaskEntityHandler(OrientGraph g) {
		super(g, TaskEntity.class);
	}

	@Override
	public void modifyMetadata() {
		setSetterByGetter("getAssignee", "setAssigneeWithoutCascade");
		setSetterByGetter("getOwner", "setOwnerWithoutCascade");
		setSetterByGetter("getDueDate", "setDueDateWithoutCascade");
		setSetterByGetter("getPriority", "setPriorityWithoutCascade");
		setSetterByGetter("getParentTaskId", "setParentTaskIdWithoutCascade");
		setSetterByGetter("getName", "setNameWithoutCascade");
		setSetterByGetter("getDescription", "setDescriptionWithoutCascade");
		setSetterByGetter("getTaskDefinitionKey", "setTaskDefinitionKeyWithoutCascade");
		setSetterByGetter("getDelegationState", "setDelegationStateWithoutCascade");
		setSetterByGetter("getCaseInstanceId", "setCaseInstanceIdWithoutCascade");
	}

	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = schema.getClass("IdentityLinkEntity");
		LOG.info("TaskEntity.createAdditionalProperties(" + oClass + "," + oLinkedClass + ")");
		getOrCreateLinkedProperty(oClass, "identityLink", OType.LINKSET, oLinkedClass);
	}

	@Override
	public void addToClauseList(List<Clause> clauseList, Object parameter) {
		String candidateGroupsStr = getValueByField(parameter, "candidateGroup");
		if (candidateGroupsStr != null) {
			List<Clause> orList = new ArrayList<Clause>();
			String[] candidateGroups = candidateGroupsStr.split(",");
			for (String group : candidateGroups) {
				LOG.info("TaskEntity.addToClauseList(" + group + ")");
				orList.add(clause("identityLink.groupId", EQ, group));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
		String candidateUser = getValueByField(parameter, "candidateUser");
		if (candidateUser != null) {
			clauseList.add(clause("identityLink.userId", EQ, candidateUser));
		}
	}
}

