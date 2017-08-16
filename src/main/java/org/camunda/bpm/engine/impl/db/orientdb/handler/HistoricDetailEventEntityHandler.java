package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailEventEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricDetailEventEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDetailEventEntityHandler.class.getName());

	public HistoricDetailEventEntityHandler(OrientGraph g) {
		super( g, HistoricDetailEventEntity.class);
	}
}
