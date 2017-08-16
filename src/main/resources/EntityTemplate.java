package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.${entityName};
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class ${entityName}Handler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(${entityName}Handler.class.getName());

	public ${entityName}Handler(OrientGraph g) {
		super( g, ${entityName}.class);
	}
}
