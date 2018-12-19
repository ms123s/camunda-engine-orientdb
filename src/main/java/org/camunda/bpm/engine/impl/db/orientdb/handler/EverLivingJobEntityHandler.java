package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class EverLivingJobEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(EverLivingJobEntityHandler.class.getName());

	public EverLivingJobEntityHandler(ODatabaseSession g) {
		super( g, EverLivingJobEntity.class);
	}
}
