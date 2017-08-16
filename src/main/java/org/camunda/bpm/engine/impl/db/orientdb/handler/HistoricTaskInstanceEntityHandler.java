package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricTaskInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricTaskInstanceEntityHandler.class.getName());

	public HistoricTaskInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricTaskInstanceEntity.class);
	}
}
