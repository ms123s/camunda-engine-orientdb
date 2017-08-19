package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
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

	public DeploymentEntityHandler(OrientGraph g) {
		super( g, DeploymentEntity.class);
	}
	public List<CParameter> getParameterList(Object p) {
		List<CParameter> parameterList = new ArrayList<CParameter>();
		String deploymentId = getValue( p, "getDeploymentId");
		if( deploymentId != null){
			parameterList.add( new CParameter( "id", EQ, deploymentId));
		}
		String name = getValue( p, "getName");
		if( name != null){
			parameterList.add( new CParameter( "name", EQ, name));
		}
		String nameLike = getValue( p, "getNameLike");
		if( nameLike != null){
			parameterList.add( new CParameter( "name", LIKE, nameLike));
		}
		Date before = getValue( p, "getDeploymentBefore");
		if( before != null){
			parameterList.add( new CParameter( "deploymentTime", LT, before));
		}
		Date after = getValue( p, "getDeploymentAfter");
		if( after != null){
			parameterList.add( new CParameter( "deploymentTime", GT, after));
		}
			parameterList.add( new CParameter( "deploymentTime", GT, new Date()));
		log.info("DeploymentEntityHandler.getParameterList:"+parameterList);
		return parameterList;
	}
}
