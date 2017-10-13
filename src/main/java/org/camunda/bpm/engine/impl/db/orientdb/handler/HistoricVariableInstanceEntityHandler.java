package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.List;
import java.util.Map;

/**
 * @author Manfred Sattler
 */
public class HistoricVariableInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricVariableInstanceEntityHandler.class.getName());

	public HistoricVariableInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricVariableInstanceEntity.class);
	}
	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
	  settingLinksReverse(entity, "getExecutionId", "HistoricProcessInstanceEntity", "variables", v, entityCache);
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
