package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.github.raymanrt.orientqb.query.Projection;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Projection.projection;

/**
 * @author Manfred Sattler
 */
public class EventSubscriptionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(EventSubscriptionEntityHandler.class.getName());

	public EventSubscriptionEntityHandler(OrientGraph g) {
		super( g, EventSubscriptionEntity.class);
	}

	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		log.info("EventSubscriptionEntity.getCParameterList("+statement+"):"+p);
		if (statement.equals("selectMessageStartEventSubscriptionByName")) {
			List<CParameter> parameterList = new ArrayList<CParameter>();
			parameterList.add(new CParameter("eventName", EQ, p));
			parameterList.add(new CParameter("eventType", EQ, EventType.MESSAGE.name()));
			parameterList.add(new CParameter("execution.id",EQ, null, true));
		log.info("EventSubscriptionEntity.return("+statement+"):"+parameterList);
			return parameterList;
		}
		return super.getCParameterList(statement, p);
	}
	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = schema.getClass("ExecutionEntity");
		getOrCreateLinkedProperty(oClass, "execution", OType.LINKSET, oLinkedClass);
	}
}
