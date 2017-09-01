package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.Projection;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Projection.projection;

/**
 * @author Manfred Sattler
 */
public class EventSubscriptionEntityHandler extends BaseEntityHandler{
	private final static Logger LOG = Logger.getLogger(EventSubscriptionEntityHandler.class.getName());

	public EventSubscriptionEntityHandler(OrientGraph g) {
		super( g, EventSubscriptionEntity.class);
	}

	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		parameterList.remove(getCParameter(parameterList, "lockResult"));
	}
	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		LOG.info("EventSubscriptionEntity.getCParameterList("+statement+"):"+p);
		if (statement.equals("selectMessageStartEventSubscriptionByName")) {
			List<CParameter> parameterList = new ArrayList<CParameter>();
			parameterList.add(new CParameter("eventName", EQ, p));
			parameterList.add(new CParameter("eventType", EQ, EventType.MESSAGE.name()));
			parameterList.add(new CParameter("execution.id",EQ, null, true));
		LOG.info("EventSubscriptionEntity.return("+statement+"):"+parameterList);
			return parameterList;
		}
		return super.getCParameterList(statement, p);
	}
	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
	}

	@Override
	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
	  insertAdditional(entity, "getExecutionId", "ExecutionEntity", "eventSubscriptions", v, entityCache);
	}
}
