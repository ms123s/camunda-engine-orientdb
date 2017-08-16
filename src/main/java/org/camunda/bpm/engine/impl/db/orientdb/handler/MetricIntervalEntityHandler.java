package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.MetricIntervalEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class MetricIntervalEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(MetricIntervalEntityHandler.class.getName());

	public MetricIntervalEntityHandler(OrientGraph g) {
		super( g, MetricIntervalEntity.class);
	}
}
