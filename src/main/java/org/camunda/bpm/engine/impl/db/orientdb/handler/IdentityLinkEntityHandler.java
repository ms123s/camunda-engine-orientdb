package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;

/**
 * @author Manfred Sattler
 */
public class IdentityLinkEntityHandler extends BaseEntityHandler {
	private final static Logger LOG = Logger.getLogger(IdentityLinkEntityHandler.class.getName());

	public IdentityLinkEntityHandler(OrientGraph g) {
		super(g, IdentityLinkEntity.class);
	}

	@Override
	public void modifyMetadata() {
		addToMeta("taskId", "getTaskId", "setTaskId", String.class);
	}

	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		if (statement.equals("selectIdentityLinksByProcessDefinition")) {
			List<CParameter> parameterList = new ArrayList<CParameter>();
			CParameter cp = new CParameter("processDefId", EQ, p);
			parameterList.add(cp);
			return parameterList;
		}
		return super.getCParameterList(statement, p);
	}

	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
	  settingChildren(entity, "getProcessDefId", "ProcessDefinitionEntity", "identityLink", v, entityCache);
	  settingChildren(entity, "getTaskId", "TaskEntity", "identityLink", v, entityCache);
	}
}

