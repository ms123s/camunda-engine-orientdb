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
import java.util.logging.Logger;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;

/**
 * @author Manfred Sattler
 */
public class ExecutionEntityHandler extends BaseEntityHandler{
	private final static Logger LOG = Logger.getLogger(ExecutionEntityHandler.class.getName());

	public ExecutionEntityHandler(OrientGraph g) {
		super( g, ExecutionEntity.class);
	}

	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		LOG.info("ExecutionEntity.modifyCParameterList("+statement+","+parameterList);
		for (CParameter p : parameterList) {
			if (p.name.equals("suspensionState")) {
				if( "active".equals(String.valueOf(p.value))){
					p.value = 1;
				}else  if( "suspended".equals(String.valueOf(p.value))){
					p.value = 2;
				}
			}
		}
		LOG.info("ExecutionEntity.modifyCParameterList2("+statement+","+parameterList);
		parameterList.remove(getCParameter(parameterList, "deploymentAware"));
		parameterList.remove(getCParameter(parameterList, "now"));
		parameterList.remove(getCParameter(parameterList, "orderingProperties"));
		parameterList.remove(getCParameter(parameterList, "applyOrdering"));
	}

	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		if( p instanceof String ){
			if( statement.equals("selectProcessInstanceIdsByProcessDefinitionId")){
				List<CParameter> parameterList = new ArrayList<CParameter>();
				parameterList.add( new CParameter( "processDefinitionId", EQ, p));
				return parameterList;
			}
			if( statement.equals("selectExecutionsByParentExecutionId")){
				List<CParameter> parameterList = new ArrayList<CParameter>();
				parameterList.add( new CParameter( "parentId", EQ, p));
				return parameterList;
			}
		}
		List<CParameter> list = super.getCParameterList(statement,p);
		if( statement.equals("selectProcessInstanceByQueryCriteria")){
			list.add( new CParameter( "parentId", EQ, null));
		}
		if( statement.equals("selectExecutionsByQueryCriteria")){
		}
		return list;
	}

	@Override
		public void addToClauseList(List<Clause> clauseList, Object parameter, Map<String,Object> queryParams) {
		List<EventSubscriptionQueryValue> evList = getValue( parameter, "getEventSubscriptions");
		if( evList != null){
			for( EventSubscriptionQueryValue ev : evList){	
				clauseList.add(new VerbatimClause("eventSubscriptions CONTAINS (eventName='"+ev.getEventName()+"' and eventType='"+ev.getEventType()+"')" ));
			}
		}
	}

	@Override
	public void insertAdditional(OrientGraph orientGraph, Vertex v, Object entity, Class entityClass, Map<String, Vertex> entityCache) {
if( true) return;
		String eventSubscriptionsId = getValue(entity, "getId");
		LOG.info("ExecutionEntity.insertAdditional(" + eventSubscriptionsId +"):" + v);
		Vertex cachedEntity = entityCache.get(eventSubscriptionsId);
		Iterable<Element> result = null;
		if (cachedEntity != null) {
			LOG.info("ExecutionEntity.insertAdditional.fromCache(" + cachedEntity +"):"+entityCache);
			List<Element> el = new ArrayList<Element>();
			el.add(cachedEntity);
			result = el; 
		}
		if (eventSubscriptionsId != null) {
			if (result == null) {
				OCommandRequest query = new OSQLSynchQuery("select from EventSubscriptionEntity where id=?");
				result = orientGraph.command(query).execute(eventSubscriptionsId);
			}
		} 
		if( result == null){
			LOG.info("ExecutionEntity.insertAdditional(" + eventSubscriptionsId +"):not found");
			return;
		}
		for (Element elem : result) {
			Iterable<Element> iter = elem.getProperty("execution");
			if (iter == null) {
				LOG.info("EventSubscriptionEntity("+elem+").insertAdditional.execution:" + v);
				elem.setProperty("execution", v);
			} else {
				Collection<Element> col = makeCollection(iter);
				LOG.info("EventSubscriptionEntity("+elem+").insertAdditional.execution(" + iter.getClass().getName() + "," + col + "):" + v);
				col.add(v);
				elem.setProperty("execution", col);
			}
			break;
		}
	}

	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = getOrCreateClass( schema, "EventSubscriptionEntity");
		getOrCreateLinkedProperty(oClass, "eventSubscriptions", OType.LINKSET, oLinkedClass);
		LOG.info("createAdditional.ExecutionEntity("+oLinkedClass+","+oClass+")");
	}
}
