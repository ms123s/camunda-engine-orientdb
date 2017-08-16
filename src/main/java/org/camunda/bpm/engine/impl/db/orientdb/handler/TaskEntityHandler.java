package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class TaskEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(TaskEntityHandler.class.getName());

	public TaskEntityHandler(OrientGraph g) {
		super( g, TaskEntity.class);
	}
}
