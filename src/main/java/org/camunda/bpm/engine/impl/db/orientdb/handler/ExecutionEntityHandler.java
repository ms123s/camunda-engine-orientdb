package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.github.raymanrt.orientqb.query.Clause;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.github.raymanrt.orientqb.query.clause.VerbatimClause;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;
import java.lang.Iterable;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;

/**
 * @author Manfred Sattler
 */
public class ExecutionEntityHandler extends BaseEntityHandler {
	private final static Logger LOG = Logger.getLogger(ExecutionEntityHandler.class.getName());

	public ExecutionEntityHandler(OrientGraph g) {
		super(g, ExecutionEntity.class);
	}

	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		LOG.info("ExecutionEntity.modifyCParameterList(" + statement + "," + parameterList);
		for (CParameter p : parameterList) {
			if (p.name.equals("suspensionState")) {
				if ("active".equals(String.valueOf(p.value))) {
					p.value = 1;
				} else if ("suspended".equals(String.valueOf(p.value))) {
					p.value = 2;
				}
			}
		}
		LOG.info("ExecutionEntity.modifyCParameterList2(" + statement + "," + parameterList);
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
		String businessKey = getValue(p, "getBusinessKey");
		LOG.info("ExecutionEntity.getCParameterList.businessKey:" + businessKey);
		if (businessKey != null) {
			Iterable<Element> procIterable = this.orientGraph.command(new OSQLSynchQuery<>("select processInstanceId from ExecutionEntity where businessKey=?")).execute(businessKey);
			Iterator<Element> iter = procIterable.iterator();
			if (iter.hasNext()) {
				String processInstanceId = iter.next().getProperty("processInstanceId");
				LOG.info("ExecutionEntity.getCParameterList.processInstanceId:" + processInstanceId);
				list.add(new CParameter("processInstanceId", EQ, processInstanceId));
			}
		}
		return list;
	}

	@Override
	public void addToClauseList(List<Clause> clauseList, Object parameter, Map<String, Object> queryParams) {
		List<EventSubscriptionQueryValue> evList = getValue(parameter, "getEventSubscriptions");
		if (evList != null) {
			for (EventSubscriptionQueryValue ev : evList) {
				if (ev.getEventName() != null) {
					clauseList.add(new VerbatimClause("eventSubscriptions CONTAINS (eventName='" + ev.getEventName() + "' and eventType='" + ev.getEventType() + "')"));
				} else {
					clauseList.add(new VerbatimClause("eventSubscriptions CONTAINS (eventType='" + ev.getEventType() + "')"));
				}
			}
		}
	}

	@Override
	public void insertAdditional(OrientGraph orientGraph, Vertex v, Object entity, Class entityClass, Map<Object, List<Vertex>> entityCache) {
	}

	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = getOrCreateClass(schema, "EventSubscriptionEntity");
		getOrCreateLinkedProperty(oClass, "eventSubscriptions", OType.LINKSET, oLinkedClass);
		LOG.info("createAdditional.ExecutionEntity(" + oLinkedClass + "," + oClass + ")");
	}
}

