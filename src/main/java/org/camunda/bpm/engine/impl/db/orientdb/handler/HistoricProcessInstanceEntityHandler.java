package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryValue;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.SingleQueryVariableValueCondition;
import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.clause.VerbatimClause;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.tinkerpop.blueprints.Vertex;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * @author Manfred Sattler
 */
public class HistoricProcessInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricProcessInstanceEntityHandler.class.getName());

	public HistoricProcessInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricProcessInstanceEntity.class);
	}

	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		List<QueryVariableValue> varList = getValue(parameter, "getQueryVariableValues");
		if (varList != null) {
			for (QueryVariableValue var : varList) {

				SingleQueryVariableValueCondition cond = var.getValueConditions().get(0);
				String valueField = getValueField(cond.getType());
				String value = getQuotedValue(cond);
				String name = var.getName();
				String op = convertOperator(var.getOperator());

/*				Clause vars = or(new VerbatimClause("variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")"), 
												 new VerbatimClause("parent.variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")"));*/
				Clause vars = new VerbatimClause("variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")");
				clauseList.add(vars);
			}
		}
		Boolean isFinished= getValueByField(parameter, "finished");
		Clause clFin = null;
		if( isFinished != null){
			if( isFinished){
				clFin = new VerbatimClause("(state != 'ACTIVE')" );
			}else{
				clFin = new VerbatimClause("(state = 'ACTIVE')" );
			}
			clauseList.add(clFin);
		}
	}
	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
	  settingLinks(entity, "getId", v, "variables", "HistoricVariableInstanceEntity", "processInstanceId", entityCache);
	}
	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = getOrCreateClass(schema, "HistoricVariableInstanceEntity");
		getOrCreateLinkedProperty(oClass, "variables", OType.LINKSET, oLinkedClass);
	}
}
