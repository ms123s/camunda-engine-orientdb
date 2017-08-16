package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class CommentEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(CommentEntityHandler.class.getName());

	public CommentEntityHandler(OrientGraph g) {
		super( g, CommentEntity.class);
	}
}
