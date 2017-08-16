package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class IdentityLinkEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(IdentityLinkEntityHandler.class.getName());

	public IdentityLinkEntityHandler(OrientGraph g) {
		super( g, IdentityLinkEntity.class);
	}
}
