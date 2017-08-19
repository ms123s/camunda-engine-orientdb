package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Manfred Sattler
 */
public class IdentityLinkEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(IdentityLinkEntityHandler.class.getName());

	public IdentityLinkEntityHandler(OrientGraph g) {
		super( g, IdentityLinkEntity.class);
	}
	@Override
	public List<CParameter> getCParameterList(String statement, Object p) {
		if( statement.equals("selectIdentityLinksByProcessDefinition")){
			List<CParameter> parameterList = new ArrayList<CParameter>();
			CParameter cp = new CParameter( "processDefId", EQ, p);
			parameterList.add( cp);
			return parameterList;
		}
		throw new RuntimeException("IdentityLinkEntity.getCParameterList(String) cannot be handled here:"+p);
	}
}
