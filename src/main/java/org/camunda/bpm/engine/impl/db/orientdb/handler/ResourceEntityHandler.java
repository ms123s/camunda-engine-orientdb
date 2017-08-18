package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;
import java.util.Map;
import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.camunda.bpm.engine.impl.db.orientdb.Parameter;

/**
 * @author Manfred Sattler
 */
public class ResourceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ResourceEntityHandler.class.getName());

	public ResourceEntityHandler(OrientGraph g) {
		super( g, ResourceEntity.class);
	}
	public void modifyParameterList(String statement, List<Parameter> parameterList) {
		for (Parameter p : parameterList){
			if( p.name.equals("resourceName")){
				if( p.value != null){
					p.name = "name";
				}
			}
		}
	}
}
