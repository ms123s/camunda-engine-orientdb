package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import com.github.raymanrt.orientqb.query.Clause;
import static com.github.raymanrt.orientqb.query.Clause.and;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;

/**
 * @author Manfred Sattler
 */
public class HistoricVariableInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricVariableInstanceEntityHandler.class.getName());

	public HistoricVariableInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricVariableInstanceEntity.class);
	}
	@Override
	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> queryParams) {
		String[] taskIds = getValueByField(parameter, "taskIds");
		log.info("HistoricVariableInstanceEntity.taskIds:"+taskIds);
		if (taskIds != null && taskIds.length > 0) {
			List<Clause> orList = new ArrayList<Clause>();
			for (String taskId : taskIds) {
				orList.add(clause("taskId", EQ, taskId));
			}
			clauseList.add(or(orList.toArray(new Clause[orList.size()])));
		}
	}
	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
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
}
