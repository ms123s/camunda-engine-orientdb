package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

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
}
