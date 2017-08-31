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
	public void insertAdditional(OrientGraph orientGraph, Vertex v, Object entity, Class entityClass, Map<Object, List<Vertex>> entityCache) {
		String processDefId = getValue(entity, "getProcessDefId");
		String taskId = getValue(entity, "getTaskId");
		LOG.info("IdentityLinkEntity.insertAdditional(" + processDefId + "," + taskId + "):" + v);
		Iterable<Vertex> result = entityCache.get(processDefId != null ? processDefId : taskId);
		if (result == null && processDefId != null) {
			OCommandRequest query = new OSQLSynchQuery("select from ProcessDefinitionEntity where id=?");
			result = orientGraph.command(query).execute(processDefId);
		} else if (result == null && taskId != null) {
			OCommandRequest query = new OSQLSynchQuery("select from TaskEntity where id=?");
			result = orientGraph.command(query).execute(taskId);
		}
		for (Element elem : result) {
			Iterable<Element> iter = elem.getProperty("identityLink");
			if (iter == null) {
				LOG.info("IdentityLinkEntity.insertAdditional.identityLink:" + v);
				elem.setProperty("identityLink", v);
			} else {
				Collection<Element> col = makeCollection(iter);
				LOG.info("IdentityLinkEntity.insertAdditional.identityLink(" + iter.getClass().getName() + "," + col + "):" + v);
				col.add(v);
				elem.setProperty("identityLink", col);
			}
		}
	}
}

