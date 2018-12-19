package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class JobDefinitionEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(JobDefinitionEntityHandler.class.getName());

	public JobDefinitionEntityHandler(ODatabaseSession g) {
		super( g, JobDefinitionEntity.class);
	}
}
