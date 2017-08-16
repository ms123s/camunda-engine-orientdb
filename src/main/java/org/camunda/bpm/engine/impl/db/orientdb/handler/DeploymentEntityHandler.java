package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class DeploymentEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DeploymentEntityHandler.class.getName());

	public DeploymentEntityHandler(OrientGraph g) {
		super( g, DeploymentEntity.class);
	}
}
