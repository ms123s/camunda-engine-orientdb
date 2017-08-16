package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class FilterEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(FilterEntityHandler.class.getName());

	public FilterEntityHandler(OrientGraph g) {
		super( g, FilterEntity.class);
	}
}
