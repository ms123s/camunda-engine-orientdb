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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class OrientdbPersistenceSession extends AbstractPersistenceSession {

	private final static Logger log = Logger.getLogger(OrientdbPersistenceSession.class.getName());
	private boolean isOpen = false;

	protected OrientGraph orientGraph;

	public OrientdbPersistenceSession(OrientGraph g, boolean openTransaction) {
		this.orientGraph = g;
		if(openTransaction) {
		}
		this.isOpen=true;
	}


	protected void insertEntity(DbEntityOperation operation) {

		//getMetaData( operation.getEntity().getClass());
		System.err.println("xxx.insertEntity1:"+operation.getEntity().getClass()+"/"+operation.getEntity());
		//String json = GroovyJsonWriter.objectToJson(operation.getEntity());
		/*String json = null;
		try{
			json = JSON.toJSONString(operation.getEntity());
		}catch( Exception e){
			json = e.getMessage();
		}

		System.err.println("xxx.insertEntity2:"+json);
		set revision to 1
		DbEntity entity = operation.getEntity();
		if (entity instanceof HasDbRevision) {
			((HasDbRevision) entity).setRevision(1);
		}*/

		// wrap as portable
//		AbstractPortableEntity<?> portable = PortableSerialization.createPortableInstance(entity);

//		getTransactionalMap(operation).put(entity.getId(), portable);
	}
	protected Map getMetaData( Class clazz){
		String name = clazz.getSimpleName();	
		String ename = name.substring(0, name.length()-6);
		System.err.println("yyy.getMetaData("+ename+"):"+clazz);
		List fields = getSimpleFields( clazz);
		//		boolean b = hasQuery( ename);
		return null;
	}

	private String[] prefixes = new String[]{ "management", "task", "filter", "identity", "history", "runtime", "repository" };
	private String[] ops = new String[]{ "In", "Like", "LessThanOrEqual", "LessThan", "GreaterThanOrEqual", "GreaterThan", "Equal", "NotEqual" };
	private boolean hasQuery(String name){
		for( String prefix : prefixes){
			try{
				Class clazz = Class.forName( "org.camunda.bpm.engine." + prefix +"." +name+ "Query" );
				Method[] meths = clazz.getDeclaredMethods();
				for( Method m : meths){
					//				System.err.println("\tyyy.method("+name+"):"+m.getName());
				}
				return true;
			}catch(Exception e){
				//System.err.println("yyy.Exception("+name+"):"+e.getMessage());
			}
		}
		return false;
	}
	private List getSimpleFields(Class clazz){
		try{
			Field[] fields = clazz.getDeclaredFields();
			for( Field f : fields){
				if( !Modifier.isStatic(f.getModifiers()) && isPrimitiveOrPrimitiveWrapperOrString(f.getType())){
					System.err.println("\tyyy.field("+f.getType().getSimpleName()+"):"+f.getName());
				}
			}
			return null;
		}catch(Exception e){
			//System.err.println("yyy.Exception("+name+"):"+e.getMessage());
		}
		return null;
	}

	public static boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
		return (type.isPrimitive() && type != void.class) ||
			type == Double.class || type == Float.class || type == Long.class ||
			type == Integer.class || type == Short.class || type == Character.class ||
			type == Byte.class || type == Boolean.class || type == String.class;
	}

	protected void deleteEntity(DbEntityOperation operation) {
		System.err.println("xxx.deleteEntity:"+operation.getEntity());
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
		System.err.println("xxx.updateEntity:"+operation.getEntity());
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

		if (log.isLoggable(Level.FINE)) {
			log.fine("executing deleteBulk " + statement);
		}

		Object parameter = operation.getParameter();

		/*DeleteStatementHandler statementHandler = HazelcastSessionFactory.getDeleteStatementHandler(statement);
		if(statementHandler != null) {
			statementHandler.execute(this, parameter);
		}
		else {
			log.log(Level.WARNING, "Delete statement '{}' currently not supported", statement);
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
		// nothing to do
	}

	protected void dbSchemaDropIdentity() {
		// TODO: implement

	}

	protected void dbSchemaDropHistory() {
		// TODO: implement

	}

	protected void dbSchemaDropEngine() {
	}

	protected void dbSchemaDropCmmn() {
		// TODO: implement
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

	public List<?> selectList(String statement, Object parameter) {

		if( parameter instanceof org.camunda.bpm.engine.impl.db.ListQueryParameterObject ){
			Object p = ((org.camunda.bpm.engine.impl.db.ListQueryParameterObject)parameter).getParameter();
			if( p !=null){
				System.err.println("xxx.selectList1("+statement+"):"+p);
			}else{
				System.err.println("xxx.selectList2("+statement+"):"+parameter);
			}
		}else{
			System.err.println("xxx.selectList3("+statement+"):"+parameter);
		}
		if(log.isLoggable(Level.FINE)) {
			log.fine("executing selectList "+statement);
		}

		/*SelectEntitiesStatementHandler statementHandler = HazelcastSessionFactory.getSelectEntitiesStatementHandler(statement);
		if(statementHandler != null) {
			List<?> result = statementHandler.execute(this, parameter);
			for (Object object : result) {
				fireEntityLoaded(object);
			}
			return result;
		}
		else {
			log.log(Level.WARNING, "SELECT many statement '{}' currently not supported:"+ statement);
			return Collections.emptyList();
		}*/
		return null;
	}

	public <T extends DbEntity> T selectById(Class<T> type, String id) {
		System.err.println("xxx.selectById("+type+"):"+id);
		/*AbstractPortableEntity<T> portable = (AbstractPortableEntity<T>) getTransactionalMap(type).get(id);
		if(portable != null) {
			T entity = portable.getEntity();
			fireEntityLoaded(entity);
			return entity;
		} else {
			return null;
		}*/
		return null;
	}

	public Object selectOne(String statement, Object parameter) {
		System.err.println("xxx.selectOne("+statement+"):"+parameter);

		/*SelectEntityStatementHandler statementHandler = HazelcastSessionFactory.getSelectEntityStatementHandler(statement);
		if(statementHandler != null) {
			DbEntity dbEntity = statementHandler.execute(this, parameter);
			if(dbEntity != null) {
				fireEntityLoaded(dbEntity);
			}
			return dbEntity;
		}
		else {
			log.log(Level.WARNING, "SELECT one statement '{}' currently not supported:"+ statement);
			return null;
		}*/
		return null;
	}

	public void lock(String statement) {
		// TODO: implement

	}

	public void commit() {
		System.err.println("commit");
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
		if( this.isOpen){
			orientGraph.shutdown();
		}
		this.isOpen=false;
	}

	public void dbSchemaCheckVersion() {
		// TODO: implement
	}

	public int executeUpdate(String s,Object o){
		return  0;
	}
	public int executeNonEmptyUpdateStmt(String s,Object o){
		return  0;
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

	protected void dbSchemaDropDmnHistory(){
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
	public boolean isDmnHistoryTablePresent(){
		return false; // not supported
	}

	public boolean isDmnTablePresent() {
		return false; // not supported
	}
}