package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.clause.VerbatimClause;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.SingleQueryVariableValueCondition;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.IN;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;

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

		oLinkedClass = getOrCreateClass(schema, "ExecutionEntity");
		getOrCreateLinkedProperty(oClass, "processInstance", OType.LINK, oLinkedClass);
	}

	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
		settingLink(entity, "getProcessInstanceId", "ExecutionEntity", "processInstance", v, entityCache);
	}

	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		String candidateGroup = getValueByField(parameter, "candidateGroup");
		Collection<String> candidateGroups = getValueByField(parameter, "candidateGroups");
		if (candidateGroups == null && candidateGroup != null) {
			candidateGroups = new HashSet<String>();
			candidateGroups.add(candidateGroup);
		}

		if (candidateGroups != null && candidateGroups.size() > 0) {
			List<Clause> orList = new ArrayList<Clause>();
			for (String group : candidateGroups) {
				orList.add(clause("identityLink.groupId", EQ, group));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
		String candidateUser = getValueByField(parameter, "candidateUser");
		if (candidateUser != null) {
			clauseList.add(clause("identityLink.userId", EQ, candidateUser));
		}
		List<QueryVariableValue> varList = getValue(parameter, "getQueryVariableValues");
		if (varList != null) {
			for (QueryVariableValue var : varList) {

				SingleQueryVariableValueCondition cond = var.getValueConditions().get(0);
				String valueField = getValueField(cond.getType());
				String value = getQuotedValue(cond);
				String name = var.getName();
				String op = convertOperator(var.getOperator());

				Clause vars = or(new VerbatimClause("variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")"), new VerbatimClause("parent.variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")"));
				clauseList.add(vars);
			}
		}
	}
}

