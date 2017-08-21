package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class PropertyEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(PropertyEntityHandler.class.getName());

	public PropertyEntityHandler(OrientGraph g) {
		super( g, PropertyEntity.class);
	}
	@Override
	public void modifyMetadata() {
		setSetterByGetter( "getId", null);
	}
}
