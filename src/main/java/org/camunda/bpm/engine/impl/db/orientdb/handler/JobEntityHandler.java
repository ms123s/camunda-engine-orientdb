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
import org.camunda.bpm.engine.impl.db.orientdb.Parameter;

/**
 * @author Manfred Sattler
 */
public class JobEntityHandler extends BaseEntityHandler{
	private final static Logger LOG = Logger.getLogger(JobEntityHandler.class.getName());

	public JobEntityHandler(OrientGraph g) {
		super( g, JobEntity.class);
	}
	public void modifyParameterList(String statement, List<Parameter> parameterList) {
		for (Parameter p : parameterList){
			if( p.name.equals("handlerConfiguration")){
				p.name = "jobHandlerConfigurationRaw";
			}
			if( p.name.equals("handlerType")){
				p.name = "jobHandlerType";
			}
		}
	}

	public String buildQuery( String entityName, String statement, List<Parameter> parameterList){
		modifyParameterList( statement, parameterList );

		Parameter ph = getParameter( parameterList, "handlerConfigurationWithFollowUpJobCreatedProperty");
		Object handlerConfigurationWithFollowUpJobCreatedProperty = ph.value;
		List<Clause> clauseList = new ArrayList<Clause>();
		for (Parameter p : parameterList){
			Clause c = null;
			if( p.value == null){
				c = projection(p.name).isNull();
			}else{
				if( p.name.equals("jobHandlerConfigurationRaw")){
					c = or ( 
								clause(p.name, EQ, p.value),
								clause(p.name, EQ, handlerConfigurationWithFollowUpJobCreatedProperty)
							);
				}else{
					c = clause(p.name, p.op, p.value);
				}
			}
			clauseList.add( c );
		}
		Clause w = and( clauseList.toArray(new Clause[clauseList.size()])  );
		Query q = new Query().from(entityName).where(w);

		postProcessQuery( q, statement, parameterList );

		LOG.info("  - query:" + q);
		return q.toString();
	}
}
