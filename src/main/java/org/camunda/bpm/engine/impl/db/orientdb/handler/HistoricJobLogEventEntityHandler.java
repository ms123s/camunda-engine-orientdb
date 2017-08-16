package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricJobLogEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricJobLogEventEntityHandler.class.getName());

	public HistoricJobLogEventEntityHandler(OrientGraph g) {
		super( g, HistoricJobLogEventEntity.class);
	}
}
