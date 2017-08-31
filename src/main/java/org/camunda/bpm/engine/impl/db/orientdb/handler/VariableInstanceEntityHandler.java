package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Manfred Sattler
 */
public class VariableInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger LOG = Logger.getLogger(VariableInstanceEntityHandler.class.getName());

	public VariableInstanceEntityHandler(OrientGraph g) {
		super( g, VariableInstanceEntity.class);
	}
	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
	  insertAdditional(entity, "getExecutionId", "ExecutionEntity", "variables", v, entityCache);
	}
}
