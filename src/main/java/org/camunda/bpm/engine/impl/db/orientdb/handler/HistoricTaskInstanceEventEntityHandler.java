package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricTaskInstanceEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricTaskInstanceEventEntityHandler.class.getName());

	public HistoricTaskInstanceEventEntityHandler(OrientGraph g) {
		super( g, HistoricTaskInstanceEventEntity.class);
	}
}
