package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.Projection;
import com.github.raymanrt.orientqb.query.Query;
import static com.github.raymanrt.orientqb.query.Clause.and;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.not;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.NULL;
import static com.github.raymanrt.orientqb.query.Parameter.parameter;
import static com.github.raymanrt.orientqb.query.Projection.ALL;
import static com.github.raymanrt.orientqb.query.Projection.projection;
import static com.github.raymanrt.orientqb.query.Variable.variable;

/**
 * @author Manfred Sattler
 */
public class JobEntityHandler extends BaseEntityHandler{
	private final static Logger LOG = Logger.getLogger(JobEntityHandler.class.getName());

	public JobEntityHandler(OrientGraph g) {
		super( g, JobEntity.class);
	}
	public void modifyParameterMap(String statement, Map<String,Object> parameterMap) {
		parameterMap.put( "jobHandlerConfigurationRaw", parameterMap.remove( "handlerConfiguration" ) );
		parameterMap.put( "jobHandlerType", parameterMap.remove( "handlerType" ) );
	}

	public String buildQuery( String entityName, String statement, Map<String,Object> parameterMap){
		modifyParameterMap( statement, parameterMap );

		Object handlerConfigurationWithFollowUpJobCreatedProperty = parameterMap.remove("handlerConfigurationWithFollowUpJobCreatedProperty");
		List<Clause> clauseList = new ArrayList<Clause>();
		for (String field : parameterMap.keySet()){
			Object value = parameterMap.get(field);
			Clause c = null;
			if( value == null){
				c = projection(field).isNull();
			}else{
				if( field.equals("jobHandlerConfigurationRaw")){
					c = or ( 
								clause(field, EQ, value),
								clause(field, EQ, handlerConfigurationWithFollowUpJobCreatedProperty)
							);
				}else{
					c = clause(field, EQ, value);
				}
			}
			clauseList.add( c );
		}
		Clause w = and( clauseList.toArray(new Clause[clauseList.size()])  );
		Query q = new Query().from(entityName).where(w);

		postProcessQuery( q, statement, parameterMap );

		LOG.info("  - query:" + q);
		return q.toString();
	}
}
