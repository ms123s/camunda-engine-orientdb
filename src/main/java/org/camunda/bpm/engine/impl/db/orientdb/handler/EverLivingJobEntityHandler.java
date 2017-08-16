package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class EverLivingJobEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(EverLivingJobEntityHandler.class.getName());

	public EverLivingJobEntityHandler(OrientGraph g) {
		super( g, EverLivingJobEntity.class);
	}
}
