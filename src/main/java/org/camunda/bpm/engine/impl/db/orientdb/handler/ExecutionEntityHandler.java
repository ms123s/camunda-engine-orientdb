package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.clause.VerbatimClause;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.OElement;
import java.lang.Iterable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryValue;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.SingleQueryVariableValueCondition;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import org.camunda.bpm.engine.impl.db.orientdb.SingleExpression;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "deprecation" })
public class ExecutionEntityHandler extends BaseEntityHandler {
	private final static Logger LOG = Logger.getLogger(ExecutionEntityHandler.class.getName());

	public ExecutionEntityHandler(ODatabaseSession g) {
		super(g, ExecutionEntity.class);
	}

	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		for (CParameter p : parameterList) {
			if (p.name.equals("suspensionState")) {
				if ("active".equals(String.valueOf(p.value))) {
					p.value = 1;
				} else if ("suspended".equals(String.valueOf(p.value))) {
					p.value = 2;
				}
			}
		}
		parameterList.remove(getCParameter(parameterList, "deploymentAware"));
		parameterList.remove(getCParameter(parameterList, "now"));
		parameterList.remove(getCParameter(parameterList, "orderingProperties"));
		parameterList.remove(getCParameter(parameterList, "applyOrdering"));
		parameterList.remove(getCParameter(parameterList, "businessKey"));
	}

	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		if (p instanceof String) {
			if (statement.equals("selectProcessInstanceIdsByProcessDefinitionId")) {
				List<CParameter> parameterList = new ArrayList<CParameter>();
				parameterList.add(new CParameter("processDefinitionId", EQ, p));
				return parameterList;
			}
			if (statement.equals("selectExecutionsByParentExecutionId")) {
				List<CParameter> parameterList = new ArrayList<CParameter>();
				parameterList.add(new CParameter("parentId", EQ, p));
				return parameterList;
			}
		}
		List<CParameter> list = super.getCParameterList(statement, p);
		if (statement.equals("selectProcessInstanceByQueryCriteria")) {
			list.add(new CParameter("parentId", EQ, null));
		}
		String processDefinitionKey = getValue(p, "getProcessDefinitionKey");
				LOG.fine("ExecutionEntity.getCParameterList.processDefinitionKey:" + processDefinitionKey);
		if (processDefinitionKey != null) {
			ODatabaseDocumentInternal currentDatabase = ODatabaseRecordThreadLocal.instance().get();
			Iterable<OElement> procIterable = currentDatabase.command(new OSQLSynchQuery<>("select id from ProcessDefinitionEntity where key=?")).execute(processDefinitionKey);
			Iterator<OElement> iter = procIterable.iterator();
			if (iter.hasNext()) {
				String processDefinitionId = iter.next().getProperty("id");
				LOG.fine("ExecutionEntity.getCParameterList.processDefinitionId:" + processDefinitionId);
				list.add(new CParameter("processDefinitionId", EQ, processDefinitionId));
			}else{
				LOG.fine("ExecutionEntity.getCParameterList.processDefinitionId:notFound");
				list.add(new CParameter("processDefinitionId", EQ, "__notFound__"));
			}
		}
		return list;
	}

	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		List<EventSubscriptionQueryValue> evList = getValue(parameter, "getEventSubscriptions");
		if (evList != null) {
			for (EventSubscriptionQueryValue ev : evList) {
				dump("  --  Event:", ev);
				if (ev.getEventName() != null) {
					clauseList.add(new VerbatimClause("eventSubscriptions CONTAINS (eventName='" + ev.getEventName() + "' and eventType='" + ev.getEventType() + "')"));
				} else {
					clauseList.add(new VerbatimClause("eventSubscriptions CONTAINS (eventType='" + ev.getEventType() + "')"));
				}
			}
		}
		String businessKey = getValue(parameter, "getBusinessKey");
		if (businessKey != null) {
			ODatabaseDocumentInternal currentDatabase = ODatabaseRecordThreadLocal.instance().get();
			Iterable<OElement> procIterable = currentDatabase.command(new OSQLSynchQuery<>("select processInstanceId from ExecutionEntity where businessKey=?")).execute(businessKey);
			Iterator<OElement> iter = procIterable.iterator();
			List<Clause> orList = new ArrayList<Clause>();
			while (iter.hasNext()) {
				String processInstanceId = iter.next().getProperty("processInstanceId");
				LOG.info("ExecutionEntity.addToClauseList.processInstanceId:" + processInstanceId);
				orList.add(clause("processInstanceId", EQ, processInstanceId));
			}
			if( orList.size() > 0){
				clauseList.add(or(orList.toArray(new Clause[orList.size()])));
			}
		}
		List<QueryVariableValue> varList = getValue(parameter, "getQueryVariableValues");
		if (varList != null) {
			for (QueryVariableValue var : varList) {

				SingleQueryVariableValueCondition cond = var.getValueConditions().get(0);
				SingleExpression ex = getExpression( var, cond );
				String valueField = ex.getValueField();
				String value = ex.getValue();
				String name = var.getName();
				String op = ex.getOp();

				Clause vars = or(new VerbatimClause("variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")"), new VerbatimClause("parent.variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")"));
				clauseList.add(vars);
			}
		}
	}

	@Override
	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
		settingLink(entity, "getParentId", "ExecutionEntity", "parent", v, entityCache);
	}

	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = getOrCreateClass(schema, "EventSubscriptionEntity");
		getOrCreateLinkedProperty(oClass, "eventSubscriptions", OType.LINKSET, oLinkedClass);

		oLinkedClass = getOrCreateClass(schema, "VariableInstanceEntity");
		getOrCreateLinkedProperty(oClass, "variables", OType.LINKSET, oLinkedClass);

		oLinkedClass = getOrCreateClass(schema, "ExecutionEntity");
		getOrCreateLinkedProperty(oClass, "parent", OType.LINK, oLinkedClass);
	}

}

