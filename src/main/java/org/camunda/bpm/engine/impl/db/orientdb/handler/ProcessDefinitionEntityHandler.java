package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.Map;
import java.util.List;
import com.github.raymanrt.orientqb.query.Query;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;

/**
 * @author Manfred Sattler
 */
public class ProcessDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ProcessDefinitionEntityHandler.class.getName());

	public ProcessDefinitionEntityHandler(OrientGraph g) {
		super( g, ProcessDefinitionEntity.class);
	}
	public void modifyParameterList(String statement, List<CParameter> parameterList) {
		for (CParameter p : parameterList){
			if( p.name.equals("processDefinitionKey")){
				if( p.value != null){
					p.name = "key";
				}
			}
		}
	}
	public void postProcessQuery(Query q, String statement, List<CParameter> parameterList) {
		if( statement.indexOf("Latest") > 0){
			q.orderBy("version");
		}
	}
}
