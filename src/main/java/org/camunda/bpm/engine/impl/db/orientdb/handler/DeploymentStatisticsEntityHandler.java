package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentStatisticsEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class DeploymentStatisticsEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(DeploymentStatisticsEntityHandler.class.getName());

	public DeploymentStatisticsEntityHandler(ODatabaseSession g) {
		super( g, DeploymentStatisticsEntity.class);
	}
}
