package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricBatchEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricBatchEntityHandler.class.getName());

	public HistoricBatchEntityHandler(OrientGraph g) {
		super( g, HistoricBatchEntity.class);
	}
}
