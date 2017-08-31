package org.camunda.bpm.engine.impl.db.orientdb.handler;


import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.Date;
import java.util.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import com.github.raymanrt.orientqb.query.Clause;
import static com.github.raymanrt.orientqb.query.Clause.and;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Parameter.parameter;
import static com.github.raymanrt.orientqb.query.Projection.projection;

/**
 * @author Manfred Sattler
 */
public class ExternalTaskEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ExternalTaskEntityHandler.class.getName());

	public ExternalTaskEntityHandler(OrientGraph g) {
		super( g, ExternalTaskEntity.class);
	}
	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		parameterList.remove(getCParameter(parameterList, "topics"));
		parameterList.remove(getCParameter(parameterList, "now"));
		parameterList.remove(getCParameter(parameterList, "orderingProperties"));
		parameterList.remove(getCParameter(parameterList, "applyOrdering"));
	}
	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		List<String> topics = getValueByField(parameter, "topics");
		if (topics != null && topics.size()>0) {
			List<Clause> orList = new ArrayList<Clause>();
			for (String topic : topics) {
				orList.add(clause("topicName", EQ, topic));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
		Clause lockExp = or(projection("lockExpirationTime").isNull(), clause("lockExpirationTime", LT, parameter("lockExpirationTime")));
		Date now = getValueByField(getValueByField(parameter, "parameter"), "now");
		queryParams.put("lockExpirationTime", now);
		clauseList.add(lockExp);
		Clause susp = or(projection("suspentionState").isNull(), clause("suspentionState", EQ, 1));
		clauseList.add(susp);
	}
}
