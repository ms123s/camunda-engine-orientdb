package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.CaseSentryPartEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class CaseSentryPartEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(CaseSentryPartEntityHandler.class.getName());

	public CaseSentryPartEntityHandler(OrientGraph g) {
		super( g, CaseSentryPartEntity.class);
	}
}
