package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Manfred Sattler
 */
public class ExecutionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ExecutionEntityHandler.class.getName());

	public ExecutionEntityHandler(OrientGraph g) {
		super( g, ExecutionEntity.class);
	}
	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		if( p instanceof String ){
			if( statement.equals("selectProcessInstanceIdsByProcessDefinitionId")){
				List<CParameter> parameterList = new ArrayList<CParameter>();
				parameterList.add( new CParameter( "processDefinitionId", EQ, p));
				return parameterList;
			}
		}
		return super.getCParameterList(statement,p);
	}
}
