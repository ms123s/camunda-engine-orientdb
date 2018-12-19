package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.clause.VerbatimClause;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.SingleQueryVariableValueCondition;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.IN;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Operator.NULL;
import static com.github.raymanrt.orientqb.query.Operator.NOT_NULL;
import org.camunda.bpm.engine.impl.db.orientdb.SingleExpression;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;

/**
 * @author Manfred Sattler
 */
public class HistoricTaskInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricTaskInstanceEntityHandler.class.getName());

	public HistoricTaskInstanceEntityHandler(ODatabaseSession g) {
		super( g, HistoricTaskInstanceEntity.class);
	}
	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = getOrCreateClass(schema, "HistoricProcessInstanceEntity");
		getOrCreateLinkedProperty(oClass, "processInstance", OType.LINK, oLinkedClass);
		oLinkedClass = getOrCreateClass(schema, "HistoricVariableInstanceEntity");
		getOrCreateLinkedProperty(oClass, "variables", OType.LINKSET, oLinkedClass);
	}

	@Override
	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
		settingLink(entity, "getProcessInstanceId", "HistoricProcessInstanceEntity", "processInstance", v, entityCache);
	  settingLinks(entity, "getId", v, "variables", "HistoricVariableInstanceEntity", "taskId", entityCache);
	}
	@Override
	public String postProcessQueryLiteral(String q, String statement, List<CParameter> parameterList) {
		return q.replace("WHERE", " LET $tid = taskId WHERE "); 	
	}
	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		String businessKey = getValueByField(parameter, "processInstanceBusinessKey");
		if (businessKey != null) {
			clauseList.add(clause("processInstance.businessKey", EQ, businessKey));
		}
		String businessKeyLike = getValueByField(parameter, "processInstanceBusinessKeyLike");
		if (businessKeyLike != null) {
			clauseList.add(clause("processInstance.businessKey", LIKE, businessKeyLike));
		}

		List<QueryVariableValue> varList = getValue(parameter, "getVariables");
		if (varList != null) {
			for (QueryVariableValue var : varList) {

				boolean isTaskVar = false;
				if( var instanceof TaskQueryVariableValue){
					Boolean b = getValueByField(var, "isProcessInstanceVariable");
					isTaskVar = b == false;
				}
				SingleQueryVariableValueCondition cond = var.getValueConditions().get(0);
				SingleExpression ex = getExpression( var, cond );
				String valueField = ex.getValueField();
				String value = ex.getValue();
				String name = var.getName();
				String op = ex.getOp();

				if( isTaskVar ){
					Clause vars = new VerbatimClause("variables CONTAINS ($tid=taskId and name='" + name + "' and " + valueField + " " + op + " " + value + ")");
					clauseList.add(vars);
				}else{
					Clause vars = new VerbatimClause("processInstance.variables CONTAINS (name='" + name + "' and " + valueField + " " + op + " " + value + ")");
					clauseList.add(vars);
				}
			}
		}
	}
}
