package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;
import java.util.Map;
import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;

/**
 * @author Manfred Sattler
 */
public class ResourceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ResourceEntityHandler.class.getName());

	public ResourceEntityHandler(ODatabaseSession g) {
		super( g, ResourceEntity.class);
	}
	@Override
	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
		for (CParameter p : parameterList){
			if( p.name.equals("resourceName")){
				if( p.value != null){
					p.name = "name";
				}
			}
		}
	}
}
