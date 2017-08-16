package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricProcessInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricProcessInstanceEntityHandler.class.getName());

	public HistoricProcessInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricProcessInstanceEntity.class);
	}
}
