package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import com.github.raymanrt.orientqb.query.Clause;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import static com.github.raymanrt.orientqb.query.Clause.and;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;

/**
 * @author Manfred Sattler
 */
public class HistoricVariableInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricVariableInstanceEntityHandler.class.getName());

	public HistoricVariableInstanceEntityHandler(ODatabaseSession g) {
		super( g, HistoricVariableInstanceEntity.class);
	}
	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		List<CParameter> list = super.getCParameterList(statement, p);
		if (statement.equals("selectHistoricVariableInstanceByQueryCriteria")) {
		  String varName= getValueByField(p, "variableName");
			list.add(new CParameter("name", EQ, varName));
		}
		return list;
	}
	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		String[] taskIds = getValueByField(parameter, "taskIds");
		debug("HistoricVariableInstanceEntity.taskIds:"+taskIds);
		if (taskIds != null && taskIds.length > 0) {
			List<Clause> orList = new ArrayList<Clause>();
			for (String taskId : taskIds) {
				orList.add(clause("taskId", EQ, taskId));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
		String[] activityInstanceIds = getValueByField(parameter, "activityInstanceIds");
		debug("HistoricVariableInstanceEntity.activityInstanceIds:"+activityInstanceIds);
		if (activityInstanceIds != null && activityInstanceIds.length > 0) {
			List<Clause> orList = new ArrayList<Clause>();
			for (String activityInstanceId : activityInstanceIds) {
				orList.add(clause("activityInstanceId", EQ, activityInstanceId));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
		String[] executionIds = getValueByField(parameter, "executionIds");
		debug("HistoricVariableInstanceEntity.executionIds:"+executionIds);
		if (executionIds != null && executionIds.length > 0) {
			List<Clause> orList = new ArrayList<Clause>();
			for (String executionId : executionIds) {
				orList.add(clause("executionId", EQ, executionId));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
	}
	@Override
	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
	  settingLinksReverse(entity, "getExecutionId", "HistoricProcessInstanceEntity", "variables", v, entityCache);
	  settingLinksReverse(entity, "getTaskId", "HistoricTaskInstanceEntity", "variables", v, entityCache);
	}
	@Override
	public String getCacheName(Object entity, String entityName) {
		String id = getValue(entity, "getExecutionId");
		if (id != null) {
			return id+entityName;
		}
		return null;
	}
	private void debug(String msg){
		com.jcabi.log.Logger.debug(this,msg);
	}
}
