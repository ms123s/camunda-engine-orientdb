package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;
import java.util.Map;

import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class ResourceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ResourceEntityHandler.class.getName());

	public ResourceEntityHandler(OrientGraph g) {
		super( g, ResourceEntity.class);
	}
	public void modifyParameterMap(String statement, Map<String,Object> parameterMap) {
		if( parameterMap.get("resourceName") != null){
			parameterMap.put( "name", parameterMap.remove( "resourceName" ) );
		}
	}
}
