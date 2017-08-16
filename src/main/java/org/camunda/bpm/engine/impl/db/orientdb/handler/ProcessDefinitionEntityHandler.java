package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.Map;
import com.github.raymanrt.orientqb.query.Query;

/**
 * @author Manfred Sattler
 */
public class ProcessDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ProcessDefinitionEntityHandler.class.getName());

	public ProcessDefinitionEntityHandler(OrientGraph g) {
		super( g, ProcessDefinitionEntity.class);
	}
	public void modifyParameterMap(String statement, Map<String,Object> parameterMap) {
		parameterMap.put( "key", parameterMap.remove( "processDefinitionKey" ) );
	}
	public void postProcessQuery(Query q, String statement, Map<String,Object> parameterMap) {
		if( statement.indexOf("Latest") > 0){
			q.orderBy("version");
		}
	}
}
