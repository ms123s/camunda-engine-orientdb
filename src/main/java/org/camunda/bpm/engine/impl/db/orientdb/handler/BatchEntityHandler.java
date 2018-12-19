package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.batch.BatchEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class BatchEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(BatchEntityHandler.class.getName());

	public BatchEntityHandler(ODatabaseSession g) {
		super( g, BatchEntity.class);
	}
}
