package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.github.raymanrt.orientqb.query.Query;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;

/**
 * @author Manfred Sattler
 */
public class ProcessDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ProcessDefinitionEntityHandler.class.getName());

	public ProcessDefinitionEntityHandler(OrientGraph g) {
		super( g, ProcessDefinitionEntity.class);
	}
	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		for (CParameter p : parameterList){
			if( p.name.equals("processDefinitionKey")){
				if( p.value != null){
					p.name = "key";
				}
			}
		}
	}
	@Override
	public void postProcessQuery(Query q, String statement, List<CParameter> parameterList) {
	}
}
