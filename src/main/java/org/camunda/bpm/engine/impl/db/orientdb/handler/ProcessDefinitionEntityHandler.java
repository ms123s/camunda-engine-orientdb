package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.github.raymanrt.orientqb.query.Query;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import com.github.raymanrt.orientqb.query.Clause;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;

/**
 * @author Manfred Sattler
 */
public class ProcessDefinitionEntityHandler extends BaseEntityHandler {
	private final static Logger log = Logger.getLogger(ProcessDefinitionEntityHandler.class.getName());

	public ProcessDefinitionEntityHandler(OrientGraph g) {
		super(g, ProcessDefinitionEntity.class);
	}

	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		if (p instanceof String) {
			if (statement.equals("selectProcessDefinitionByDeploymentId")) {
				List<CParameter> parameterList = new ArrayList<CParameter>();
				parameterList.add(new CParameter("deploymentId", EQ, p));
				return parameterList;
			}
			throw new RuntimeException("ProcessDefinitionEntity.getCParameterList(" + statement + ",String) cannot be handled here:" + p);
		} else {
			return super.getCParameterList(statement, p);
		}
	}

	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		for (CParameter p : parameterList) {
			if (p.name.equals("processDefinitionKey")) {
				if (p.value != null) {
					p.name = "key";
				}
			}
		}
	}

	@Override
	public void createAdditionalProperties(OSchema schema, OClass oClass) {
		OClass oLinkedClass = schema.getClass("IdentityLinkEntity");
		getOrCreateLinkedProperty(oClass, "identityLink", OType.LINKSET, oLinkedClass);
	}

	@Override
	public void postProcessQuery(Query q, String statement, List<CParameter> parameterList) {
	}

	@Override
	public void addToClauseList(List<Clause> clauseList, Object parameter) {
		String authorizationUserId = getValueByField(parameter, "authorizationUserId");
		if (authorizationUserId != null) {
			clauseList.add(clause("identityLink.userId", EQ, authorizationUserId));
		}
	}
}

