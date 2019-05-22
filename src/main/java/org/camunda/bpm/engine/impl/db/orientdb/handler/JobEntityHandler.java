package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.Projection;
import com.github.raymanrt.orientqb.query.Query;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.logging.Logger;
import java.util.Map;
import com.orientechnologies.orient.core.record.OElement;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import com.github.raymanrt.orientqb.query.Parameter;
import static com.github.raymanrt.orientqb.query.Clause.and;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.not;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Operator.NULL;
import static com.github.raymanrt.orientqb.query.Parameter.parameter;
import static com.github.raymanrt.orientqb.query.Projection.ALL;
import static com.github.raymanrt.orientqb.query.Projection.projection;
import static com.github.raymanrt.orientqb.query.Variable.variable;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Manfred Sattler
 */
public class JobEntityHandler extends BaseEntityHandler {
	private final static Logger LOG = Logger.getLogger(JobEntityHandler.class.getName());

	public JobEntityHandler(ODatabaseSession g) {
		super(g, JobEntity.class);
	}

	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		for (CParameter p : parameterList) {
			if (p.name.equals("handlerConfiguration")) {
				p.name = "jobHandlerConfigurationRaw";
			}
			if (p.name.equals("handlerType")) {
				p.name = "jobHandlerType";
			}
		}
		parameterList.remove(getCParameter(parameterList, "deploymentAware"));
		parameterList.remove(getCParameter(parameterList, "now"));
		parameterList.remove(getCParameter(parameterList, "orderingProperties"));
		parameterList.remove(getCParameter(parameterList, "applyOrdering"));
	}

	@Override
	public void checkParameterList(List<CParameter> parameterList) {
	}

	@Override
	public OCommandRequest buildQuery(String entityName, String statement, List<CParameter> parameterList, Object parameter, Map<String, Object> queryParams) {
		modifyCParameterList(statement, parameterList);

		CParameter ph = getCParameter(parameterList, "handlerConfigurationWithFollowUpJobCreatedProperty");
		if (ph == null) {
			return super.buildQuery(entityName, statement, parameterList, parameter, queryParams);
		}
		Object handlerConfigurationWithFollowUpJobCreatedProperty = ph.value;
		List<Clause> clauseList = new ArrayList<Clause>();
		for (CParameter p : parameterList) {
			Clause c = null;
			if (p.value == null) {
				c = projection(p.name).isNull();
			} else {
				if (p.name.equals("jobHandlerConfigurationRaw")) {
					c = or(clause(p.name, EQ, p.value), clause(p.name, EQ, handlerConfigurationWithFollowUpJobCreatedProperty));
				} else {
					c = clause(p.name, p.op, p.value);
				}
			}
			clauseList.add(c);
		}
		Clause w = and(clauseList.toArray(new Clause[clauseList.size()]));
		Query q = new Query().from(entityName).where(w);

		postProcessQuery(q, statement, parameterList);

		debug("  - query:" + q);
		OCommandRequest query = new OSQLSynchQuery(q.toString());
		return query;
	}

	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		getOrCreateProperty(oClass, "type", OType.STRING);
	}

	@Override
	public Class getSubClass(Class entityClass, Map<String, Object> properties) {
		String type = (String) properties.get("type");
		if ("TimerEntity".equals(type)) {
			return TimerEntity.class;
		}
		if ("MessageEntity".equals(type)) {
			return MessageEntity.class;
		}
		return entityClass;
	}

	public Iterable<OElement> selectNextJobsToExecute(ListQueryParameterObject query) {
		debug("selectNextJobsToExecute");
		Map<String, Object> params = getValue(query, "getParameter");
		debug("selectNextJobsToExecute(" + params + ")");
		Date now = (Date) params.get("now");
		int maxResults = query.getMaxResults();
		String orderBy = null;//query.getOrderBy();
		return queryList("select from JobEntity" +
			 " where retries > 0" +
			 " and (duedate is null or duedate <= ?)" +
			 " and (lockOwner is null or lockExpirationTime < ?)" +
			 " and suspensionState = 1" +
			 ((orderBy != null) ? " order by " +
			 orderBy : "") +
			 " LIMIT ?", now, now, maxResults);
	}

	public Iterable<OElement> selectJobsByConfiguration(ListQueryParameterObject query) {
		Map<String, Object> params = getValue(query, "getParameter");
		String config = (String) params.get("handlerConfiguration");
		String followUpConfig = (String) params.get("handlerConfigurationWithFollowUpJobCreatedProperty");
		String type = (String) params.get("handlerType");
		List<String> args = new ArrayList<>();
		Query q = new Query().from("JobEntity");
		q.where(Clause.clause("jobHandlerType", EQ, Parameter.PARAMETER));
		args.add(type);
		Clause eqConfig = Clause.clause("JobHandlerConfigurationRaw", EQ, Parameter.PARAMETER);
		if (isEmpty(followUpConfig)) {
			q.where(eqConfig);
			args.add(config);
		} else {
			q.where(Clause.or(eqConfig, eqConfig));
			args.add(config);
			args.add(followUpConfig);
		}
		return queryList(q.toString(), args.toArray());
	}

	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		if( "selectNextJobsToExecute".equals(statement)){
			ListQueryParameterObject query = (ListQueryParameterObject) parameter;
			Map<String, Object> param = getValue(query,"getParameter");
			Date now = getValueFromMap(param, "now");
			//	  String orderBy = query.getOrderBy();
			if( now == null){
				now = new Date();
			}

			clauseList.add(clause("retries", GT, 0));
			Clause dueDate = or(projection("duedate").isNull(), clause("duedate", LT, parameter("duedate")));
			queryParams.put("duedate", now);
			clauseList.add(dueDate);

			Clause lockOwner = or(projection("lockOwner").isNull(), clause("lockExpirationTime", LT, parameter("lockExpirationTime")));
			queryParams.put("lockExpirationTime", now);
			clauseList.add(lockOwner);

			clauseList.add(clause("suspensionState", EQ, 1));
		}
	}
	private void debug(String msg){
		com.jcabi.log.Logger.debug(this,msg);
	}
}

