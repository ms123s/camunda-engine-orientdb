package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.github.raymanrt.orientqb.query.Clause;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
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
			List<CParameter> parameterList = new ArrayList<CParameter>();
			list.add( new CParameter( "parentId", EQ, null));
		}
		return list;
	}
	@Override
	public void insertAdditional(OrientGraph orientGraph, Vertex v, Object entity, Class entityClass, Map<String, Vertex> entityCache) {
		String executionId = getValue(entity, "getId");
		LOG.info("ExecutionEntity.insertAdditional(" + executionId +"):" + v);
		Vertex cachedEntity = entityCache.get(executionId);
		Iterable<Element> result = null;
		if (cachedEntity != null) {
			List<Element> el = new ArrayList<Element>();
			el.add(cachedEntity);
			result = el; 
		}
		if (executionId != null) {
			if (result == null) {
				OCommandRequest query = new OSQLSynchQuery("select from ExecutionEntity where id=?");
				result = orientGraph.command(query).execute(executionId);
			}
		} 
		if( result == null){
			LOG.info("ExecutionEntity.insertAdditional(" + executionId +"):not found");
			return;
		}
		for (Element elem : result) {
			Iterable<Element> iter = elem.getProperty("execution");
			if (iter == null) {
				LOG.info("ExecutionEntity.insertAdditional.execution:" + v);
				elem.setProperty("identityLink", v);
			} else {
				Collection<Element> col = makeCollection(iter);
				LOG.info("ExecutionEntity.insertAdditional.execution(" + iter.getClass().getName() + "," + col + "):" + v);
				col.add(v);
				elem.setProperty("execution", col);
			}
			break;
		}
	}
}
