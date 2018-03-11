package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.clause.VerbatimClause;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Iterator;
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
import static com.github.raymanrt.orientqb.query.Operator.NULL;
import static com.github.raymanrt.orientqb.query.Operator.NOT_NULL;

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

		//oLinkedClass = getOrCreateClass(schema, "VariableInstanceEntity");
		//getOrCreateLinkedProperty(oClass, "variable", OType.LINKSET, oLinkedClass);
	}

	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
		settingLink(entity, "getProcessInstanceId", "ExecutionEntity", "processInstance", v, entityCache);
	  //settingLinks(entity, "getProcessInstanceId", v, "variables", "VariableInstanceEntity", "processInstanceId", entityCache);
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
//				orList.add(clause("identityLink.groupId", EQ, group));
				orList.add(new VerbatimClause("identityLink CONTAINS (groupId='" + group + "')"));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
		String candidateUser = getValueByField(parameter, "candidateUser");
		if (candidateUser != null) {
//			clauseList.add(clause("identityLink.userId", EQ, candidateUser));
			clauseList.add(new VerbatimClause("identityLink CONTAINS (userId='" + candidateUser + "')"));
		}
		String businessKey = getValueByField(parameter, "processInstanceBusinessKey");
		if (businessKey != null) {
			clauseList.add(clause("processInstance.businessKey", EQ, businessKey));
		}
		String businessKeyLike = getValueByField(parameter, "processInstanceBusinessKeyLike");
		if (businessKeyLike != null) {
			clauseList.add(clause("processInstance.businessKey", LIKE, businessKeyLike));
		}

		Boolean isAssigned= getValueByField(parameter, "assigned");
		Boolean isUnAssigned= getValueByField(parameter, "unassigned");
		debug("isUnAssigned:" + isUnAssigned);
		debug("isAssigned:" + isAssigned);
		if( isAssigned != null && isAssigned.booleanValue() == true){
			Clause clFin = clause("assignee", NOT_NULL,"" );
			clauseList.add(clFin);
		}
		if( isUnAssigned != null && isUnAssigned.booleanValue() == true){
			Clause clFin = clause("assignee", NULL,"" );
			clauseList.add(clFin);
		}
		String processDefinitionKey = getValueByField(parameter, "processDefinitionKey");
		debug("TaskEntityHandler.addToClauseList.processDefinitionKey:" + processDefinitionKey);
		if (processDefinitionKey != null) {
			Iterable<Element> procIterable = this.orientGraph.command(new OSQLSynchQuery<>("select id from ProcessDefinitionEntity where key=?")).execute(processDefinitionKey);
			Iterator<Element> iter = procIterable.iterator();
			if (iter.hasNext()) {
				String processDefinitionId = iter.next().getProperty("id");
				debug("TaskEntityHandler.addToClauseList.processDefinitionId:" + processDefinitionId);
				clauseList.add(clause( "processDefinitionId", EQ, processDefinitionId));
			}else{
				debug("TaskEntityHandler.addToClauseList.processDefinitionId:notFound");
				clauseList.add(clause( "processDefinitionId", EQ, "__notFound__"));
			}
		}

		List<QueryVariableValue> varList = getValue(parameter, "getVariables");
		if (varList != null) {
			for (QueryVariableValue var : varList) {

				SingleQueryVariableValueCondition cond = var.getValueConditions().get(0);
				String valueField = getValueField(cond.getType());
				String value = getQuotedValue(cond);
				String name = var.getName();
				String op = convertOperator(var.getOperator());

				Clause vars = new VerbatimClause("processInstance.variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")");
				clauseList.add(vars);
			}
		}
	}
	private void debug(String msg){
		//LOG.fine(msg);
		com.jcabi.log.Logger.debug(this,msg);
	}
}

