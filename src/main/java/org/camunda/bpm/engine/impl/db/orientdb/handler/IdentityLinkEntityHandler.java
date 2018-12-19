package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.query.Clause;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
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

	public IdentityLinkEntityHandler(ODatabaseSession g) {
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
	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
	  settingLinksReverse(entity, "getProcessDefId", "ProcessDefinitionEntity", "identityLink", v, entityCache);
	  settingLinksReverse(entity, "getTaskId", "TaskEntity", "identityLink", v, entityCache);
	}
}

