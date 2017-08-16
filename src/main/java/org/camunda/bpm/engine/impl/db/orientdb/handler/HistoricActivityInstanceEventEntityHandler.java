package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricActivityInstanceEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricActivityInstanceEventEntityHandler.class.getName());

	public HistoricActivityInstanceEventEntityHandler(OrientGraph g) {
		super( g, HistoricActivityInstanceEventEntity.class);
	}
}
