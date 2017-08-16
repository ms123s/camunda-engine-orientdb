package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentStatisticsEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class DeploymentStatisticsEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DeploymentStatisticsEntityHandler.class.getName());

	public DeploymentStatisticsEntityHandler(OrientGraph g) {
		super( g, DeploymentStatisticsEntity.class);
	}
}
