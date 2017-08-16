package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class JobEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(JobEntityHandler.class.getName());

	public JobEntityHandler(OrientGraph g) {
		super( g, JobEntity.class);
	}
}
