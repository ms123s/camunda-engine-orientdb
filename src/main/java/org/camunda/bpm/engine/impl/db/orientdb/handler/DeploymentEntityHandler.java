package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;

/**
 * @author Manfred Sattler
 */
public class DeploymentEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DeploymentEntityHandler.class.getName());

	public DeploymentEntityHandler(ODatabaseSession g) {
		super( g, DeploymentEntity.class);
	}
	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		List<CParameter> parameterList = super.getCParameterList(statement,p);
		Date before = getValue( p, "getDeploymentBefore");
		if( before != null){
			parameterList.add( new CParameter( "deploymentTime", LT, before));
		}
		Date after = getValue( p, "getDeploymentAfter");
		if( after != null){
			parameterList.add( new CParameter( "deploymentTime", GT, after));
		}
		log.info("DeploymentEntityHandler.getCParameterList:"+parameterList);
		return parameterList;
	}
}
