/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.db.orientdb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.Iterable;
import java.util.Map;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.db.AbstractPersistenceSession;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.db.orientdb.handler.*;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

	
/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class OrientdbPersistenceSession extends AbstractPersistenceSession {

	private final static Logger LOG = Logger.getLogger(OrientdbPersistenceSession.class.getName());
	private boolean isOpen = false;
	long sessionId;

	protected OrientGraph orientGraph;

	public OrientdbPersistenceSession(OrientGraph g, boolean openTransaction) {
		this.orientGraph = g;
		this.isOpen = true;
		sessionId = new java.util.Date().getTime();
		LOG.info("openSession:" + sessionId);
	}

	public List<?> selectList(String statement, Object parameter) {
		if (parameter instanceof org.camunda.bpm.engine.impl.db.ListQueryParameterObject) {
			Object p = ((org.camunda.bpm.engine.impl.db.ListQueryParameterObject) parameter).getParameter();
			if (p != null) {
				LOG.info("selectList1(" + statement + "):" + p);
			} else {
				LOG.info("selectList2(" + statement + "):" + parameter);
			}
		} else {
			LOG.info("selectList3(" + statement + "):" + parameter);
		}
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("executing selectList " + statement);
		}

		return new ArrayList();
	}

	public <T extends DbEntity> T selectById(Class<T> entityClass, String id) {
		String entityName = entityClass.getSimpleName();
		LOG.info("selectById(" + entityName + ").id:" + id);
		OCommandRequest query = new OSQLSynchQuery("select  from "+entityName+" where id=?");
		LOG.info("- query:" + query);
		Iterable<Element> result = orientGraph.command(query).execute(id);
		LOG.info("- result:"+result);
		OrientVertex v = null;
		for (Element elem : result) {
			LOG.info("-- :"+elem+"/"+elem.getClass());
			v = (OrientVertex)elem;
			break;
		}
		if( v == null){
			return null;
		}
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass);
		try {
			T entity = (T)entityClass.newInstance();
			List<Map<String, Object>> fields = handler.getMetadata();
			Map<String,Object> props = v.getProperties();
			for (Map<String, Object> f : fields) {
				String name = (String) f.get("name");
				Object value = props.get(name);
				if( value == null){
					continue;
				}
				LOG.info("- Prop(" + name + "):" + value);
				String setter = (String) f.get("setter");
				if( setter == null){
					continue;
				}
				Class type = (Class) f.get("type");
				Class[] args = new Class[1];
				args[0] = type;

				Method method = entityClass.getMethod(setter,args);
				method.invoke(entity, value);
			}
			LOG.info("selectById.return:"+entity);
			return entity;
		} catch (Exception e) {
			LOG.throwing("OrientdbPersistenceSession", "selectById", e);
			e.printStackTrace();
		}
		return null;
	}

	public Object selectOne(String statement, Object parameter) {
		LOG.info("selectOne(" + statement + "):" + parameter);

		/*SelectEntityStatementHandler statementHandler = HazelcastSessionFactory.getSelectEntityStatementHandler(statement);
		if(statementHandler != null) {
			DbEntity dbEntity = statementHandler.execute(this, parameter);
			if(dbEntity != null) {
				fireEntityLoaded(dbEntity);
			}
			return dbEntity;
		}
		else {
			LOG.log(Level.WARNING, "SELECT one statement '{}' currently not supported:"+ statement);
			return null;
		}*/
		return null;
	}

	protected void insertEntity(DbEntityOperation operation) {
		LOG.info("insertEntity:" + operation.getEntity().getClass().getSimpleName());

		DbEntity entity = operation.getEntity();
		Class entityClass = entity.getClass();
		String entityName = entityClass.getSimpleName();
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass);
		//LOG.info("insertEntity.handler:" + handler.getMetadata());

		if (entity instanceof HasDbRevision) {
			((HasDbRevision) entity).setRevision(1);
		}

		try {
			Vertex v = this.orientGraph.addVertex("class:"+entityName);
			List<Map<String, Object>> fields = handler.getMetadata();
			for (Map<String, Object> f : fields) {
				String getter = (String) f.get("getter");
				String name = (String) f.get("name");
				Method method = entityClass.getMethod(getter);
				Object value = method.invoke(entity);
				LOG.info("- Field(" + name + "):" + value);
				v.setProperty(name, value);
			}
		} catch (Exception e) {
			LOG.throwing("OrientdbPersistenceSession", "insertEntity", e);
			e.printStackTrace();
		}
	}

	protected void deleteEntity(DbEntityOperation operation) {
		LOG.info("deleteEntity:" + operation.getEntity());
		/*		BaseMap<String, AbstractPortableEntity<?>> map = getTransactionalMap(operation);

		 DbEntity removedEntity = operation.getEntity();

		 if (removedEntity instanceof HasDbRevision) {
		 HasDbRevision removedRevision = (HasDbRevision) removedEntity;
		 AbstractPortableEntity<?> dbPortable = map.remove(removedEntity.getId());
		 ensureNotNull(OptimisticLockingException.class, "dbRevision", dbPortable);
		 HasDbRevision dbRevision = (HasDbRevision) dbPortable.getEntity();
		 if (dbRevision.getRevision() != removedRevision.getRevision()) {
		 throw new OptimisticLockingException(removedEntity +  " was updated by another transaction concurrently");
		 }
		 }
		 else {
		 map.remove(removedEntity.getId());
		 }*/
	}

	protected void updateEntity(DbEntityOperation operation) {
		LOG.info("updateEntity:" + operation.getEntity());
		/*		BaseMap<String, AbstractPortableEntity<?>> map = getTransactionalMap(operation);
		 DbEntity updatedEntity = operation.getEntity();

		 AbstractPortableEntity<?> portable = PortableSerialization.createPortableInstance(updatedEntity);

		 if (updatedEntity instanceof HasDbRevision) {
		 HasDbRevision updatedRevision = (HasDbRevision) updatedEntity;
		 int oldRevision = updatedRevision.getRevision();
		 updatedRevision.setRevision(updatedRevision.getRevisionNext());
		 AbstractPortableEntity<?> dbPortable = map.put(updatedEntity.getId(), portable);
		 ensureNotNull(OptimisticLockingException.class, "dbRevision", dbPortable);
		 HasDbRevision dbRevision = (HasDbRevision) dbPortable.getEntity();
		 if (dbRevision.getRevision() != oldRevision) {
		 throw new OptimisticLockingException(updatedEntity + " was updated by another transaction concurrently");
		 }
		 }
		 else {
		 map.put(updatedEntity.getId(), portable);
		 }*/
	}

	protected void deleteBulk(DbBulkOperation operation) {
		String statement = operation.getStatement();

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("executing deleteBulk " + statement);
		}

		Object parameter = operation.getParameter();

		/*DeleteStatementHandler statementHandler = HazelcastSessionFactory.getDeleteStatementHandler(statement);
		if(statementHandler != null) {
			statementHandler.execute(this, parameter);
		}
		else {
			LOG.log(Level.WARNING, "Delete statement '{}' currently not supported", statement);
		}*/

	}

	protected void updateBulk(DbBulkOperation operation) {
		// TODO: implement

	}

	protected String getDbVersion() {
		return "fox";
	}

	protected void dbSchemaCreateIdentity() {
		// nothing to do
	}

	protected void dbSchemaCreateHistory() {
		// nothing to do
	}

	protected void dbSchemaCreateEngine() {
	}

	protected void dbSchemaCreateCmmn() {
	}

	protected void dbSchemaDropIdentity() {
	}

	protected void dbSchemaDropHistory() {
	}

	protected void dbSchemaDropEngine() {
	}

	protected void dbSchemaDropCmmn() {
	}

	public boolean isEngineTablePresent() {
		return true;
	}

	public boolean isHistoryTablePresent() {
		return true;
	}

	public boolean isIdentityTablePresent() {
		return true;
	}

	public boolean isCaseDefinitionTablePresent() {
		return true;
	}


	public void lock(String statement) {
	}

	public void commit() {
		LOG.info("commitSession:" + sessionId);
		orientGraph.commit();
	}

	public void rollback() {
		orientGraph.rollback();
	}

	public void flush() {
		// nothing to do
	}

	public void close() {
		// nothing to do
		if (this.isOpen) {
			LOG.info("closeSession:" + sessionId);
			orientGraph.shutdown();
		}
		this.isOpen = false;
	}

	public void dbSchemaCheckVersion() {
		// TODO: implement
	}

	public int executeUpdate(String s, Object o) {
		return 0;
	}

	public int executeNonEmptyUpdateStmt(String s, Object o) {
		return 0;
	}

	public void lock(String statement, Object parameter) {
		// TODO: not implemented
	}

	protected void dbSchemaCreateDmnHistory() {
		// not supported
	}

	protected void dbSchemaCreateCmmnHistory() {
		// not supported
	}

	protected void dbSchemaCreateDmn() {
		// not supported
	}

	protected void dbSchemaDropDmnHistory() {
		// not supported
	}

	protected void dbSchemaDropCmmnHistory() {
		// not supported
	}

	protected void dbSchemaDropDmn() {
		// not supported
	}

	public boolean isCmmnTablePresent() {
		return false; // not supported
	}

	public boolean isCmmnHistoryTablePresent() {
		return false; // not supported
	}

	public boolean isDmnHistoryTablePresent() {
		return false; // not supported
	}

	public boolean isDmnTablePresent() {
		return false; // not supported
	}
}

