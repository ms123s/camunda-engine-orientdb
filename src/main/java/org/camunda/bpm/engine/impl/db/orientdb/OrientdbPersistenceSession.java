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
import java.util.Arrays;
import java.lang.Iterable;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.db.AbstractPersistenceSession;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbBulkOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.db.orientdb.handler.*;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.AbstractQuery;

import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.NULL;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
public class OrientdbPersistenceSession extends AbstractPersistenceSession {

	private final static Logger LOG = Logger.getLogger(OrientdbPersistenceSession.class.getName());
	private boolean isOpen = false;
	long sessionId;
	private List<String> prefixList = new ArrayList<>(Arrays.asList("selectLatest", "select"));
	private List<String> suffixList = new ArrayList<>(Arrays.asList("CountBy", "IdsBy", "By"));
	OrientdbSessionFactory sessionFactory;
	private Map<Object, List<OVertex>> entityCache = new HashMap<Object, List<OVertex>>();

	protected ODatabaseSession databaseSession;

	public OrientdbPersistenceSession(ODatabaseSession g, OrientdbSessionFactory sf) {
		this.databaseSession = g;
		this.sessionFactory = sf;
		g.activateOnCurrentThread();
		this.isOpen = true;
		sessionId = new java.util.Date().getTime();
		debug("openSession:" + sessionId);
		this.databaseSession.begin();
	}

	public Object selectOne(String statement, Object parameter) {
		debug("selectOne:" + statement);
		String prefix = getPrefix(statement);
		String suffix = getSuffix(statement);
		String entityName = getEntityName(statement, prefix, suffix);
		Class entityClass = OrientdbSessionFactory.getEntityClass(entityName);
		debug("->selectOne(" + statement + "," + entityName + "):" + parameter);

		BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass, this.databaseSession);
		boolean isCount = statement.indexOf("Count") > 0;

		List<CParameter> parameterList = getCParameterList(statement, parameter, entityHandler);
		Map<String, Object> queryParams = new HashMap<String, Object>();
		String query = entityHandler.buildQuery(entityName, statement, parameterList, parameter, queryParams);
    OResultSet rs = databaseSession.command(query,queryParams);
		debug("  - result:" + rs);
		Map<String, Object> props = null;
		int count = 0;
    while( rs.hasNext()){
      OElement elem = rs.next().toElement();
			count++;
			props = getProperties(elem);
			if (!isCount) {
				break;
			}
		}
		if (count == 0) {
			if (statement.indexOf("Count") > 0) {
				debug("<-selectOne(" + entityName + ").count:0");
				return new Long(0);
			} else {
				debug("<-selectOne(" + entityName + ").return:null");
				return null;
			}
		}
		try {
			if (isCount) {
				debug("<-selectOne.count(" + entityName + ").return:" + count);
				return new Long(count);
			}
			Class subClass = entityHandler.getSubClass(entityClass, props);
			Object entity = subClass.newInstance();
			setEntityValues(subClass, entity, props);
			dump("selectOne(" + entityName + ")", entity);
			debug("<-selectOne(" + entityName + ").return:" + entity);
			fireEntityLoaded(entity);
			return entity;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectOne:" + e.getMessage());
			//			LOG.throwing("OrientdbPersistenceSession", "selectOne", e);
			e.printStackTrace();
		}
		debug("<-selectOne(" + entityName + ").return:null");
		return null;
	}

	public List<?> selectList(String statement, Object parameter) {
		String prefix = getPrefix(statement);
		String suffix = getSuffix(statement);
		String entityName = getEntityName(statement, prefix, suffix);
		debug("-> selectList(" + statement + "," + entityName + "):" + parameter);

		Class entityClass = OrientdbSessionFactory.getEntityClass(entityName);
		//LOG.info("  - entityClass:" + entityClass);

		Iterable<OElement> result = null;
		BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass, this.databaseSession);
		Method m = getStatementMethod(entityHandler, statement, parameter);
		if (m != null) {
			result = callStatementMethod(m, entityHandler, statement, parameter);
			debug(" - selectList(" + statement + "):result from callStatementMethod:" + result);
		}

		boolean isLatest = false;
		if (result == null) {
			List<CParameter> parameterList = getCParameterList(statement, parameter, entityHandler);
			Map<String, Object> queryParams = new HashMap<String, Object>();
			String query = entityHandler.buildQuery(entityName, statement, parameterList, parameter, queryParams);
			isLatest = getBoolean(queryParams.remove("_isLatest"));
      OResultSet rs = databaseSession.command(query,queryParams);
      List<OElement> l = new ArrayList();
      while( rs.hasNext()){
        OElement e = rs.next().toElement();
        l.add(e);
      }
      result = l;
		}

		List<Map<String, Object>> propsList = new ArrayList<Map<String, Object>>();
		for (OElement elem : result) {
			Map<String, Object> props = getProperties(elem);
			propsList.add(props);
		}
		if (propsList.size() == 0) {
			debug("<-selectList(" + entityName + ").return:emptyList");
			return propsList;
		}
		try {
			Map<Object,Object> uniqueMap = new HashMap<Object,Object>();
			if (statement.indexOf("IdsBy") > 0) {
				List<String> idList = new ArrayList<String>();
				for (Map<String, Object> props : propsList) {
					Object id = props.get("id");
					if( isLatest && uniqueMap.get(id) != null){
						continue;
					}
					uniqueMap.put( id, "" );
					idList.add((String) props.get("id"));
				}
				debug("<-selectList2(" + entityName + ").return:" + idList);
				return idList;
			} else {
				
				List<Object> entityList = new ArrayList<Object>();
				for (Map<String, Object> props : propsList) {
					Class subClass = entityHandler.getSubClass(entityClass, props);
					Object entity = subClass.newInstance();
					setEntityValues(subClass, entity, props);
					dump("selectList(" + entityName + ")", entity);
					Object id = props.get(entityHandler.getKeyForLatestGrouping());
					if( isLatest && uniqueMap.get(id) != null){
						continue;
					}
					uniqueMap.put( id, "" );
					fireEntityLoaded(entity);
					entityList.add(entity);
					if( isSingleResult(parameter)){
						break;
					}
				}
				debug("<-selectList3(" + entityName + ").return:" + entityList);
				return entityList;
			}
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectList(" + statement + "," + entityName + "):" + e.getMessage());
			//			LOG.throwing("OrientdbPersistenceSession", "selectList", e);
			e.printStackTrace();
		}
		debug("<-selectList4(" + entityName + ").return:emptyList");
		return new ArrayList();
	}

	private boolean isSingleResult( Object parameter){
		if (parameter instanceof AbstractQuery) {
			Object res = getValueByField( parameter,"resultType");
			if( "SINGLE_RESULT".equals(String.valueOf(res))){
				return true;
			}
		}
		return false;
	}
	private List<CParameter> getCParameterList(String statement, Object parameter, BaseEntityHandler handler) {
		//LOG.info("  - getParameterList(" + statement + "):" + parameter);
		if (parameter instanceof AbstractQuery) {
			return handler.getCParameterList(statement, parameter);
		} else if (parameter instanceof ListQueryParameterObject) {
			//LOG.info("   - ListQueryParameterObject");
			if (((ListQueryParameterObject) parameter).getParameter() instanceof String) {
				Object obj = ((ListQueryParameterObject) parameter).getParameter();
				if (statement.endsWith("ByKey")) {
					List<CParameter> parameterList = new ArrayList<CParameter>();
					CParameter p = new CParameter("key", EQ, obj);
					parameterList.add(p);
					return parameterList;
				} else {
					return handler.getCParameterList(statement, (String) obj);
				}
			} else {
				Map<String, Object> map = (Map<String, Object>) ((ListQueryParameterObject) parameter).getParameter();
				return getCParameterListFromMap(map);
			}
		} else {
			if (parameter instanceof String) {
				Object obj = parameter;
				debug("   - String2:" + obj);
				List<CParameter> parameterList = new ArrayList<CParameter>();
				if (statement.endsWith("ByKey")) {
					parameterList.add(new CParameter("key", EQ, obj));
				} else if (statement.endsWith("ByProcDef")) {
					parameterList.add(new CParameter("processDefId", EQ, obj));
				} else if (statement.endsWith("ById")) {
					parameterList.add(new CParameter("id", EQ, obj));
				} else if (statement.endsWith("ByTaskId")) {
					parameterList.add(new CParameter("taskId", EQ, obj));
				} else if (statement.endsWith("ByDeploymentId")) {
					parameterList.add(new CParameter("deploymentId", EQ, obj));
				} else if (statement.endsWith("ByProcessDefinitionId")) {
					parameterList.add(new CParameter("processDefinitionId", EQ, obj));
				} else if (statement.endsWith("deleteHistoricProcessInstance")) { //@@@MS check
					parameterList.add(new CParameter("processInstanceId", EQ, obj));
				} else if (statement.endsWith("ByProcessInstanceId")) {
					parameterList.add(new CParameter("processInstanceId", EQ, obj));
				} else if (statement.endsWith("deleteDeployment")) {
					parameterList.add(new CParameter("id", EQ, obj));
				}
				return parameterList;
			} else {
				Map<String, Object> map = (Map<String, Object>) parameter;
				debug("   - Map2:" + map);
				return getCParameterListFromMap(map);
			}
		}
	}

	private List<CParameter> getCParameterListFromMap(Map<String, Object> map) {
		List<CParameter> parameterList = new ArrayList<CParameter>();
		Set<String> keySet = map.keySet();
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			Map<String, Object> param = new HashMap<String, Object>();
			String key = iterator.next();
			CParameter p = new CParameter(key, EQ, map.get(key));
			parameterList.add(p);
		}
		return parameterList;
	}

	public <T extends DbEntity> T selectById(Class<T> entityClass, String id) {
		entityClass = OrientdbSessionFactory.getReplaceClass(entityClass);
		String entityName = entityClass.getSimpleName();
		debug("->selectById(" + entityName + "," + entityClass + ").id:" + id);
		OCommandRequest query = new OSQLSynchQuery("select  from " + entityName + " where id=?");
		debug("  - query:" + query);
		Iterable<OElement> result = databaseSession.command(query).execute(id);
		debug("  - result:" + result);
		Map<String, Object> props = null;
		for (OElement elem : result) {
			debug(" selectById.elem" + entityName + "," + id + "):elem:" + elem);
			props = getProperties(elem);
			break;
		}
		if (props == null) {
			debug("<-selectById(" + entityName + "," + id + ").return:null");
			return null;
		}
		try {
			BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass, this.databaseSession);
			Class subClass = entityHandler.getSubClass(entityClass, props);
			T entity = (T) subClass.newInstance();
			setEntityValues(entityClass, entity, props);
			dump("selectById(" + entityName + "," + id + ")", entity);
			debug("<-selectById(" + entityName + "," + id + ").return:" + entity);
			fireEntityLoaded(entity);
			return entity;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectById:" + e.getMessage());
			throw new RuntimeException("OrientdbPersistenceSession.selectById(" + entityName + "," + id + ")", e);
		}
	}

	private Map<String,Object> getProperties( OElement elem ){
		Map<String,Object> map = new HashMap<String,Object>();
		for( String name : elem.getPropertyNames()){
			map.put( name, elem.getProperty(name));
		}
		return map;
	}

	private void setEntityValues(Class entityClass, Object entity, Map<String, Object> props) throws Exception {
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.databaseSession);
		List<Map<String, Object>> entityMeta = handler.getMetadata();
		for (Map<String, Object> m : entityMeta) {
			String name = (String) m.get("name");
			Object value = props.get(name);
			if (value == null) {
				continue;
			}
			if (m.get("namedId") != null) {
				continue;
			}
			String setter = (String) m.get("setter");
			if (setter == null) {
				continue;
			}
			//debug("  - Prop(" + name + "):" + value);
			Class type = (Class) m.get("type");
			Class[] args = new Class[1];
			args[0] = type;

			Method method = null;
			try {
				method = entityClass.getMethod(setter, args);
			} catch (Exception e) {
				method = entityClass.getDeclaredMethod(setter, args);
				method.setAccessible(true);
			}
			if (method == null) {
				debug("OrientdbPersistenceSession.setEntityValues.method(" + setter + ") is null in " + entityClass.getSimpleName());
			} else {
				method.invoke(entity, value);
			}
		}
		String entityName = entityClass.getSimpleName();
		Integer rev = (Integer)props.get("dbRevision");
		if( rev != null && rev > 0){
			setField(entity, "revision", rev);
		}
	}

	private String getPrefix(String statement) {
		for (String pre : this.prefixList) {
			if (statement.startsWith(pre)) {
				return pre;
			}
		}
		throw new RuntimeException("OrientdbPersistenceSession.getPrefix(" + statement + "):not found");
	}

	private String getSuffix(String statement) {
		for (String suff : this.suffixList) {
			if (statement.indexOf(suff) > 0) {
				return suff;
			}
		}
		return "";
		//throw new RuntimeException("OrientdbPersistenceSession.getSuffix(" + statement + "):not found");
	}

	private String getEntityName(String statement, String prefix, String suffix) {
		int start = prefix.length();
		int end = suffix.equals("") ? statement.length() : statement.indexOf(suffix);
		String name = statement.substring(start, end);
		if (statement.startsWith("selectProcessInstance")) {
			name = "Execution";
		} else if (statement.startsWith("selectHistoricVariables")) {
			name = "HistoricVariableInstance";
		} else if (statement.startsWith("selectHistoricDetail")) {
			name = "HistoricDetailEvent";
		} else if (statement.startsWith("selectMessageStartEventSubscription")) {
			name = "EventSubscription";
		} else if (statement.startsWith("selectNextJobsToExecute")) {
			name = "Job";
		} else if (statement.startsWith("selectExternalTasksForTopics")) {
			name = "ExternalTask";
		} else if (statement.startsWith("selectVariablesBy")) {
			name = "VariableInstance";
		} else if (statement.startsWith("selectUserOperationLogEntries")) {
			name = "UserOperationLogEntryEvent";
		} else if (!name.endsWith("Statistics") && name.endsWith("s")) {
			name = name.substring(0, name.length() - 1);
		}
		return name + "Entity";
	}

	protected void insertEntity(DbEntityOperation operation) {

		DbEntity entity = operation.getEntity();
		Class entityClass = OrientdbSessionFactory.getReplaceClass(entity.getClass());
		String entityName = entityClass.getSimpleName();
		debug("-> insertEntity(" + entityName + ")");

		dump("insertEntity.operation:", operation);
		dump("insertEntity.entity:", entity);
		if (entityName.equals("HistoricVariableUpdateEventEntity")) {
			this.sessionFactory.fireEvent((HistoricVariableUpdateEventEntity) entity);
		}
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.databaseSession);

		if (entity instanceof HasDbRevision) {
			((HasDbRevision) entity).setRevision(1);
		}
		try {
			OVertex v = this.databaseSession.newVertex(entityName);
			List<Map<String, Object>> entityMeta = handler.getMetadata();
			Object id = null;
			Object n = null;
			for (Map<String, Object> m : entityMeta) {
				if (m.get("namedId") != null) {
					continue;
				}
				String getter = (String) m.get("getter");
				String name = (String) m.get("name");
				Method method = entityClass.getMethod(getter);
				Object value = method.invoke(entity);
				if (value != null && false) {
					if( name.equals("bytes")){
						try{
							debug("- Field(" + name + "):" + new ObjectInputStream(new ByteArrayInputStream((byte[])value)).readObject());
						}catch(Exception e){
							debug("- Field(" + name + ")not deser:" + value);
						}
					}else{
						debug("- Field(" + name + "):" + value);
					}
				}
				if (name.equals("id"))
					id = value;
				if (name.equals("name"))
					n = value;
				v.setProperty(name, value);
			}
			if (entity instanceof HasDbRevision) {
				v.setProperty("dbRevision", 1);
			}
			String cacheName = handler.getCacheName(entity, entityName);
			if (cacheName != null) {
				List<OVertex> vl = this.entityCache.get(cacheName);
				if (vl == null) {
					vl = new ArrayList<OVertex>();
					entityCache.put(cacheName, vl);
				}
				vl.add(v);
			}
			handler.insertAdditional(v, entity, this.entityCache);
			this.databaseSession.save(v);
			debug("<- insertEntity(" + entityName + "," + n + "," + id + "):ok");
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.insertEntity(" + entityName + "):" + e.getMessage());
			System.err.println("OrientdbPersistenceSession.insertEntity(" + entityName + "):" + e);
			throw new RuntimeException("OrientdbPersistenceSession.insertEntity(" + entityName + ")", e);
		}
	}

	protected void deleteEntity(DbEntityOperation operation) {
		Object entity = operation.getEntity();
		Class entityClass = OrientdbSessionFactory.getReplaceClass(entity.getClass());
		String entityName = entity.getClass().getSimpleName();
		if (entityName.equals("ExecutionEntity")) {
			//return;
		}
		if (entityName.equals("EventSubscriptionEntity")) {
			//return;
		}
		if (entityName.equals("VariableInstanceEntity")) {
			//return;
		}
		String id = getValue(entity, "getId");
		String name = null;
		try {
			name = getValue(entity, "getName");
		} catch (Exception e) {
		}
		debug("-> deleteEntity(" + entityName + "):" + id);
		OCommandRequest del = new OCommandSQL("Delete vertex " + entityName + " where id=?");
		try {
			String sql = "SELECT FROM " + entityName + " where id='" + id + "'";
			OVertex vertex = selectBySql(sql);
			debug("deleteEntity(" + entityName + "," + name + "):vertex:" + vertex);
			if (vertex == null) {
				operation.setFailed(true);
				info("<- deleteEntity(" + entityName + "," + name + ").failed:vertex is not found");
				return;
			}
			databaseSession.command(del).execute(id);
		} catch (Exception e) {
			if (entity instanceof HasDbRevision) {
				operation.setFailed(true);
				info("<- deleteEntity(" + entityName + "," + name + ").failed:" + e);
				return;
			}
		}
		debug("<- deleteEntity(" + entityName + "," + name + "):ok");
	}

	protected void deleteBulk(DbBulkOperation operation) {
		String statement = operation.getStatement();
		Object parameter = operation.getParameter();

		if (statement.equals("deleteExceptionByteArraysByIds")) {
			debug("deleteBulk(" + statement + ") not handled!");
			return;
		}
		if (statement.equals("deleteErrorDetailsByteArraysByIds")) {
			debug("deleteBulk(" + statement + ") not handled!");
			return;
		}

		Class entityClass = operation.getEntityType();
		entityClass = OrientdbSessionFactory.getReplaceClass(entityClass);
		String entityName = entityClass.getSimpleName();
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.databaseSession);
		debug("-> deleteBulk(" + statement + "," + entityName + ").parameter:" + parameter);
		List<CParameter> parameterList = getCParameterList(statement, parameter, handler);

		if (parameterList.size() > 0) {
			Map<String, Object> queryParams = new HashMap<String, Object>();
			OCommandRequest up = handler.buildDelete(entityName, statement, parameterList, queryParams);

			databaseSession.command(up).execute(queryParams);
		} else {
			throw new RuntimeException("OrientdbPersistenceSession.deleteBulk(" + statement + "," + entityName + "):no parameter");
		}
		debug("<- deleteBulk(" + statement + ") ok");
	}

	protected void updateBulk(DbBulkOperation operation) {
	}

	protected void updateEntity(DbEntityOperation operation) {
		DbEntity entity = operation.getEntity();
		String entityName = entity.getClass().getSimpleName();
		String id = getValue(entity, "getId");
		String name = getValueSilent(entity, "getName");
		debug("-> updateEntity(" + entityName + "," + id +","+name + ")");

		if (entity instanceof HasDbRevision) {
			HasDbRevision updatedRevision = (HasDbRevision) entity;
			int oldRevision = updatedRevision.getRevision();
			Integer dbRevision = updateById(entity, id, operation, oldRevision,updatedRevision.getRevisionNext());
			if (dbRevision == null || dbRevision != oldRevision) {
				operation.setFailed(true);
				info("<- updateEntity(" + entityName+","+id + ").failed:revisions:" + dbRevision + "/" + oldRevision);
				return;
			}
		} else {
			updateById(entity, id, operation, 1, -1);
		}
		debug("<- updateEntity(" + entityName+","+name + "):ok");
	}

	private Integer updateById(Object entity, String id, DbEntityOperation operation, int oldRevision,int revision) {
		Class entityClass = OrientdbSessionFactory.getReplaceClass(entity.getClass());
		String entityName = entityClass.getSimpleName();
		OCommandRequest query = new OSQLSynchQuery("select  from " + entityName + " where id=?");
		Iterable<OElement> result = databaseSession.command(query).execute(id);
		Iterator<OElement> it = result.iterator();
		if (!it.hasNext()) {
			debug(" - UpdateById(" + entityName + "," + id + ").not found");
			return -1;
		}
		try {
			OElement e = it.next();

			BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.databaseSession);
			List<Map<String, Object>> entityMeta = handler.getMetadata();
			for (Map<String, Object> m : entityMeta) {
				if (m.get("namedId") != null) {
					continue;
				}
				String getter = (String) m.get("getter");
				String name = (String) m.get("name");
				Method method = entityClass.getMethod(getter);
				Object value = method.invoke(entity);
				if (value != null && false) {
					if( name.equals("bytes")){
						try{
							debug("- Field(" + name + "):" + new ObjectInputStream(new ByteArrayInputStream((byte[])value)).readObject());
						}catch(Exception ex){
							debug("- Field(" + name + ")not deser:" + value);
						}
					}else{
						debug("- Field(" + name + "):" + value);
					}
				}
				e.setProperty(name, value);
			}
			Integer oldRev = e.getProperty("dbRevision");
			if (entity instanceof HasDbRevision) {
				e.setProperty("dbRevision", revision);
				HasDbRevision r = (HasDbRevision) entity;
				r.setRevision(revision);
				if( revision!= -1){
					debug("    updateById(" + entityName + "," + id + "):new:"+revision+"/old:"+oldRevision+"/oldGet:"+oldRev+"/"+r.getRevision());
				}
			}
			this.databaseSession.save(e);
			return oldRev;

		} catch (OConcurrentModificationException e) {
			info("OConcurrentModificationException.updateById(" + entityName + ").failed:" + e.getMessage());
			operation.setFailed(true);
			return -1;
		} catch (Exception e) {
			info("OrientdbPersistenceSession.updateById(" + entityName + ").failed:" + e.getMessage());
			throw new RuntimeException("OrientdbPersistenceSession.updateById(" + entityName + ")", e);
		}
	}

	private OVertex selectBySql(String sql) {
		OCommandRequest query = new OSQLSynchQuery(sql);
		Iterable<OVertex> result = databaseSession.command(query).execute();
		Iterator<OVertex> it = result.iterator();
		if (it.hasNext()) {
			OVertex v = it.next();
			return v;
		}
		return null;
	}

	private Object fireEventForVariableInstanceEntityDelete(Class entityClass, String statement, List<CParameter> parameterList, BaseEntityHandler handler) {
		Map<String, Object> queryParams = new HashMap<String, Object>();
		String query = handler.buildQuery(entityClass.getSimpleName(), statement, parameterList, null, queryParams);
    OResultSet rs = databaseSession.command(query,queryParams);
		Map<String, Object> props = null;
    while( rs.hasNext()){
      OElement elem = rs.next().toElement();
			props = getProperties(elem);
			break;
		}

		try {
			if (props != null) {
				Object entity = entityClass.newInstance();
				setEntityValues(entityClass, entity, props);
				//this.sessionFactory.fireEvent((VariableInstanceEntity) entity, "delete");
				return entity;
			}
		} catch (Exception e) {
			info("OrientdbPersistenceSession.getVariableInstanceEntity.failed:" + e);
		}
		return null;
	}

	private <Any> Any getValue(Object obj, String methodName) {
		try {
			Method method = obj.getClass().getMethod(methodName, (Class[]) null);
			return (Any) method.invoke(obj);
		} catch (Exception e) {
			throw new RuntimeException("OrientdbPersistenceSession.getValue:" + obj.getClass().getSimpleName() + "." + methodName);
		}
	}
	private <Any> Any getValueSilent(Object obj, String methodName) {
		try {
			Method method = obj.getClass().getMethod(methodName, (Class[]) null);
			return (Any) method.invoke(obj);
		} catch (Exception e) {
			return null;
		}
	}

	private <Any> Any getValueByField(Object obj, String fieldName) {
		try {
			if (obj == null) {
				debug("OrientdbPersistenceSession.getValueByField(" + fieldName + ") obj is null");
				return null;
			}
			if (obj instanceof Map) {
				return (Any) ((Map) obj).get(fieldName);
			}
			Field field = getField(obj.getClass(),fieldName);
			field.setAccessible(true);
			return (Any) field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("OrientdbPersistenceSession.getValueByField:" + obj.getClass().getSimpleName() + "." + fieldName + " not found");
			return null;
		}
	}

	private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	private void dump(String msg, Object o) {
		if (true)
			return;
		ReflectionToStringBuilder rb = new ReflectionToStringBuilder(o, ToStringStyle.JSON_STYLE);
		rb.setExcludeNullValues(true);
		debug("   +++" + msg + ":" + rb.toString());
	}

	private Method getStatementMethod(Object o, String statement, Object parameter) {
		if (!(parameter instanceof ListQueryParameterObject)) {
			debug("getStatementMethod(" + statement + "):Parameter noz ListQueryParameterObject");
			return null;
		}
		try {
			Class[] params = new Class[1];
			params[0] = ListQueryParameterObject.class;
			Method method = o.getClass().getMethod(statement, params);
			return method;
		} catch (Exception e) {
			return null;
		}
	}

	private <Any> Any callStatementMethod(Method method, Object o, String statement, Object queryParams) {
		debug("   - callStatementMethod(" + statement + "):" + method.getName());
		try {
			Object[] params = new Object[1];
			params[0] = queryParams;
			return (Any) method.invoke(o, params);
		} catch (Exception e) {
			throw new RuntimeException("OrientdbPersistenceSession.callStatementMethod(" + statement + ") error:", e);
		}
	}

	private boolean getBoolean( Object obj){
		if( obj != null && obj instanceof Boolean ){
			return (Boolean)obj;
		}
		return false;
	}

	private boolean setField(Object targetObject, String fieldName, Object fieldValue) {
		Field field;
		try {
			field = targetObject.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			field = null;
		}
		Class superClass = targetObject.getClass().getSuperclass();
		while (field == null && superClass != null) {
			try {
				field = superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				superClass = superClass.getSuperclass();
			}
		}
		if (field == null) {
			return false;
		}
		field.setAccessible(true);
		try {
			field.set(targetObject, fieldValue);
			return true;
		} catch (IllegalAccessException e) {
			return false;
		}
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
		debug("lock:" + statement);
	}

	public void commit() {
		debug("commitSession:" + sessionId);
		try {
			databaseSession.commit();
		} catch (Exception e) {
      error(this, "commit("+sessionId+").error:%[exception]s",e);
		}
		for (Object key : this.entityCache.keySet()) {
			List<OVertex> l = this.entityCache.get(key);
			for (OVertex v : l) {
				debug("insertedEntity(" + sessionId + "):" + v);
			}
		}
	}

	public void rollback() {
		databaseSession.rollback();
		LOG.info("rollbackSession:" + sessionId);
	}

	public void flush() {
		// nothing to do
	}

	public void close() {
		// nothing to do
		if (this.isOpen) {
			debug("closeSession:" + sessionId);
			databaseSession.close();
		}
		this.isOpen = false;
	}

	public void dbSchemaCheckVersion() {
	}

	public int executeUpdate(String s, Object o) {
		debug("executeUpdate:" + s + "/" + o);
		throw new RuntimeException("OrientdbPersistenceSession.executeUpdate not implemented");
	}

	public int executeNonEmptyUpdateStmt(String s, Object o) {
		debug("executeNonEmptyUpdateStmt(" + s + "):");
		dump("executeNonEmptyUpdateStmt", o);
		//	throw new RuntimeException("OrientdbPersistenceSession.executeNonEmptyUpdateStmt not implemented");
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
	private void info(String msg){
		com.jcabi.log.Logger.info(this,msg);
	}
	private void debug(String msg){
		com.jcabi.log.Logger.debug(this,msg);
	}
}

