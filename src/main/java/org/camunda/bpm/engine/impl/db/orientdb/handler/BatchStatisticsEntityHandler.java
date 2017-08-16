package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.batch.BatchStatisticsEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class BatchStatisticsEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(BatchStatisticsEntityHandler.class.getName());

	public BatchStatisticsEntityHandler(OrientGraph g) {
		super( g, BatchStatisticsEntity.class);
	}
}
