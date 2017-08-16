package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.BatchEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class BatchEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(BatchEntityHandler.class.getName());

	public BatchEntityHandler(OrientGraph g) {
		super( g, BatchEntity.class);
	}
}
