package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEvent;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricJobLogEventHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricJobLogEventHandler.class.getName());

	public HistoricJobLogEventHandler(OrientGraph g) {
		super( g, HistoricJobLogEvent.class);
	}
}
