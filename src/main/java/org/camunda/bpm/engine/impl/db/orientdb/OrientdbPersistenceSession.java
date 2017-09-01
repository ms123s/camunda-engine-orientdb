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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.NULL;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class OrientdbPersistenceSession extends AbstractPersistenceSession {

	private final static Logger LOG = Logger.getLogger(OrientdbPersistenceSession.class.getName());
	private boolean isOpen = false;
	long sessionId;
	private List<String> prefixList = new ArrayList<>(Arrays.asList("selectLatest", "select"));
	private List<String> suffixList = new ArrayList<>(Arrays.asList("CountBy", "IdsBy", "By"));
	OrientdbSessionFactory sessionFactory;
	private Map<Object, List<Vertex>> entityCache = new HashMap<Object, List<Vertex>>();

	protected OrientGraph orientGraph;

	public OrientdbPersistenceSession(OrientGraph g, OrientdbSessionFactory sf) {
		this.orientGraph = g;
		this.sessionFactory = sf;
		g.getRawGraph().activateOnCurrentThread();
		this.isOpen = true;
		sessionId = new java.util.Date().getTime();
		LOG.info("openSession:" + sessionId);
		System.err.println("openSession:" + sessionId);
		this.orientGraph.begin();
	}

	public Object selectOne(String statement, Object parameter) {
		LOG.info("selectOne:" + statement);
		String prefix = getPrefix(statement);
		String suffix = getSuffix(statement);
		String entityName = getEntityName(statement, prefix, suffix);
		Class entityClass = OrientdbSessionFactory.getEntityClass(entityName);
		LOG.info("->selectOne(" + statement + "," + entityName + "):" + parameter);

		BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass, this.orientGraph);
		boolean isCount = statement.indexOf("Count") > 0;

		List<CParameter> parameterList = getCParameterList(statement, parameter, entityHandler);
		LOG.info("  - CParameterList:" + parameterList);
		Map<String, Object> queryParams = new HashMap<String, Object>();
		OCommandRequest query = entityHandler.buildQuery(entityName, statement, parameterList, parameter, queryParams);

		Iterable<Element> result = orientGraph.command(query).execute(queryParams);
		LOG.info("  - result:" + result);
		Map<String, Object> props = null;
		int count = 0;
		for (Element elem : result) {
			count++;
			props = ((OrientVertex) elem).getProperties();
			if (!isCount) {
				break;
			}
		}
		if (count == 0) {
			if (statement.indexOf("Count") > 0) {
				LOG.info("<-selectOne(" + entityName + ").count:0");
				return new Long(0);
			} else {
				LOG.info("<-selectOne(" + entityName + ").return:null");
				return null;
			}
		}
		try {
			if (isCount) {
				LOG.info("<-selectOne.count(" + entityName + ").return:" + count);
				return new Long(count);
			}
			Class subClass = entityHandler.getSubClass(entityClass, props);
			Object entity = subClass.newInstance();
			setEntityValues(subClass, entity, props);
			dump("selectOne(" + entityName + ")", entity);
			LOG.info("<-selectOne(" + entityName + ").return:" + entity);
			fireEntityLoaded(entity);
			return entity;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectOne:" + e.getMessage());
			//			LOG.throwing("OrientdbPersistenceSession", "selectOne", e);
			e.printStackTrace();
		}
		LOG.info("<-selectOne(" + entityName + ").return:null");
		return null;
	}

	public List<?> selectList(String statement, Object parameter) {
		String prefix = getPrefix(statement);
		String suffix = getSuffix(statement);
		String entityName = getEntityName(statement, prefix, suffix);
		LOG.info("-> selectList(" + statement + "," + entityName + "):" + parameter);

		Class entityClass = OrientdbSessionFactory.getEntityClass(entityName);
		//LOG.info("  - entityClass:" + entityClass);

		Iterable<Element> result = null;
		BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass, this.orientGraph);
		Method m = getStatementMethod(entityHandler, statement, parameter);
		if( m != null ){
			result = callStatementMethod( m, entityHandler, statement, parameter );
			LOG.info(" - selectList("+statement+"):result from callStatementMethod:"+result);
		}

		if( result == null){
			List<CParameter> parameterList = getCParameterList(statement, parameter, entityHandler);
			LOG.info("  - CParameterList:" + parameterList);
			Map<String, Object> queryParams = new HashMap<String, Object>();
			OCommandRequest query = entityHandler.buildQuery(entityName, statement, parameterList, parameter, queryParams);

			result = orientGraph.command(query).execute(queryParams);
		}


		List<Map<String, Object>> propsList = new ArrayList<Map<String, Object>>();
		for (Element elem : result) {
			Map<String, Object> props = ((OrientVertex) elem).getProperties();
			propsList.add(props);
		}
		if (propsList.size() == 0) {
			LOG.info("<-selectList(" + entityName + ").return:emptyList");
			return propsList;
		}
		try {
			if (statement.indexOf("IdsBy") > 0) {
				List<String> idList = new ArrayList<String>();
				for (Map<String, Object> props : propsList) {
					idList.add((String) props.get("id"));
				}
				LOG.info("<-selectList2(" + entityName + ").return:" + idList);
				return idList;
			} else {
				List<Object> entityList = new ArrayList<Object>();
				for (Map<String, Object> props : propsList) {
					Class subClass = entityHandler.getSubClass(entityClass, props);
					Object entity = subClass.newInstance();
					setEntityValues(subClass, entity, props);
					dump("selectList(" + entityName + ")", entity);
					fireEntityLoaded(entity);
					entityList.add(entity);
				}
				LOG.info("<-selectList3(" + entityName + ").return:" + entityList);
				return entityList;
			}
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectList(" + statement + "," + entityName + "):" + e.getMessage());
			//			LOG.throwing("OrientdbPersistenceSession", "selectList", e);
			e.printStackTrace();
		}
		LOG.info("<-selectList4(" + entityName + ").return:emptyList");
		return new ArrayList();
	}

	private List<CParameter> getCParameterList(String statement, Object parameter, BaseEntityHandler handler) {
		//LOG.info("  - getParameterList(" + statement + "):" + parameter);
		if (parameter instanceof AbstractQuery) {
			return handler.getCParameterList(statement, parameter);
		} else if (parameter instanceof ListQueryParameterObject) {
			//LOG.info("   - ListQueryParameterObject");
			if (((ListQueryParameterObject) parameter).getParameter() instanceof String) {
				Object obj = ((ListQueryParameterObject) parameter).getParameter();
				LOG.info("  - String1:" + obj);
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
				LOG.info("   - Map1:" + map);
				return getCParameterListFromMap(map);
			}
		} else {
			if (parameter instanceof String) {
				Object obj = parameter;
				LOG.info("   - String2:" + obj);
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
				LOG.info("   - Map2:" + map);
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
		LOG.info("->selectById(" + entityName + "," + entityClass + ").id:" + id);
		OCommandRequest query = new OSQLSynchQuery("select  from " + entityName + " where id=?");
		LOG.info("  - query:" + query);
		Iterable<Element> result = orientGraph.command(query).execute(id);
		LOG.info("  - result:" + result);
		Map<String, Object> props = null;
		for (Element elem : result) {
			props = ((OrientVertex) elem).getProperties();
			break;
		}
		if (props == null) {
			LOG.info("<-selectById(" + entityName + ","+id+").return:null");
			return null;
		}
		try {
			BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass, this.orientGraph);
			Class subClass = entityHandler.getSubClass(entityClass, props);
			T entity = (T) subClass.newInstance();
			setEntityValues(entityClass, entity, props);
			dump("selectById(" + entityName + "," + id + ")", entity);
			LOG.info("<-selectById(" + entityName + ","+id+").return:" + entity);
			fireEntityLoaded(entity);
			return entity;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectById:" + e.getMessage());
			throw new RuntimeException("OrientdbPersistenceSession.selectById(" + entityName + ","+id+")", e);
		}
		//LOG.info("<-selectById(" + entityName + ").return:null");
		//return null;
	}

	private void setEntityValues(Class entityClass, Object entity, Map<String, Object> props) throws Exception {
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.orientGraph);
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
			//LOG.info("  - Prop(" + name + "):" + value);
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
				LOG.info("OrientdbPersistenceSession.setEntityValues.method(" + setter + ") is null in " + entityClass.getSimpleName());
			} else {
				method.invoke(entity, value);
			}
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
		LOG.info("-> insertEntity(" + entityName + ")");

		dump("insertEntity.operation:", operation);
		dump("insertEntity.entity:", entity);
		if (entityName.equals("HistoricVariableUpdateEventEntity")) {
			this.sessionFactory.fireEvent((HistoricVariableUpdateEventEntity) entity);
		}
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.orientGraph);

		if (entity instanceof HasDbRevision) {
			((HasDbRevision) entity).setRevision(1);
		}

		try {
			Vertex v = this.orientGraph.addVertex("class:" + entityName);
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
				if (value != null /*name.equals("id")*/) {
					//LOG.info("- Field(" + name + "):" + value);
				}
				if (name.equals("id"))
					id = value;
				if (name.equals("name"))
					n = value;
				v.setProperty(name, value);
			}
			if (id != null) {
				List<Vertex> vl = entityCache.get(id+entityName);
				if( vl == null){
					vl = new ArrayList<Vertex>();
					entityCache.put(id+entityName, vl);
				}
				vl.add(v);
			}
			handler.insertAdditional(v, entity, entityCache);
			LOG.info("<- insertEntity(" + entityName + "," + n + "," + id + "):ok");
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
//			return;
		}
		if (entityName.equals("EventSubscriptionEntity")) {
//			return;
		}
		if (entityName.equals("VariableInstanceEntity")) {
//			return;
		}
		dump("deleteEntity.operation:", operation);
		dump("deleteEntity.entity:", entity);
		String id = getValue(entity, "getId");
		String name = null;
		try {
			name = getValue(entity, "getName");
		} catch (Exception e) {
		}
		LOG.info("-> deleteEntity(" + entityName + "):" + id);
		OCommandRequest del = new OCommandSQL("Delete vertex " + entityName + " where id=?");
		orientGraph.command(del).execute(id);
		LOG.info("<- deleteEntity(" + entityName + "," + name + "):ok");
	}

	protected void deleteBulk(DbBulkOperation operation) {
		String statement = operation.getStatement();
		Object parameter = operation.getParameter();

		if (statement.equals("deleteExceptionByteArraysByIds")) {
			LOG.info("deleteBulk(" + statement + ") not handled!");
			return;
		}
		if (statement.equals("deleteErrorDetailsByteArraysByIds")) {
			LOG.info("deleteBulk(" + statement + ") not handled!");
			return;
		}

		Class entityClass = operation.getEntityType();
		entityClass = OrientdbSessionFactory.getReplaceClass(entityClass);
		String entityName = entityClass.getSimpleName();
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.orientGraph);
		LOG.info("-> deleteBulk(" + statement + "," + entityName + ").parameter:" + parameter);
		List<CParameter> parameterList = getCParameterList(statement, parameter, handler);
		LOG.info("  - CParameterList:" + parameterList);

		if (parameterList.size() > 0) {
			if (entityName.equals("VariableInstanceEntity")) {
				//fireEventForVariableInstanceEntityDelete(entityClass, statement, parameterList, handler);
			}
			Map<String, Object> queryParams = new HashMap<String, Object>();
			OCommandRequest up = handler.buildDelete(entityName, statement, parameterList, queryParams);
			orientGraph.command(up).execute(queryParams);
		} else {
			throw new RuntimeException("OrientdbPersistenceSession.deleteBulk(" + statement + "," + entityName + "):no parameter");
		}
		LOG.info("<- deleteBulk ok");
	}

	protected void updateBulk(DbBulkOperation operation) {
		// TODO: implement

	}

	protected void updateEntity(DbEntityOperation operation) {
		Object entity = operation.getEntity();
		String entityName = entity.getClass().getSimpleName();
		if (entityName.equals("VariableInstanceEntity")) {
			//this.sessionFactory.fireEvent((VariableInstanceEntity) entity, "update");
		}
		String id = getValue(entity, "getId");
		LOG.info("-> updateEntity(" + entityName + "," + id + ")");
		updateById(entity, id);
		LOG.info("<- updateEntity(" + entityName + "):ok");
	}

	private void updateById(Object entity, String id) {
		Class entityClass = OrientdbSessionFactory.getReplaceClass(entity.getClass());
		String entityName = entityClass.getSimpleName();
		OCommandRequest query = new OSQLSynchQuery("select  from " + entityName + " where id=?");
		Iterable<Element> result = orientGraph.command(query).execute(id);
		Iterator<Element> it = result.iterator();
		if (!it.hasNext()) {
			LOG.info(" - UpdateById(" + entityName + "," + id + ").not found");
			return;
		}
		try {
			Element e = it.next();

			BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass, this.orientGraph);
			List<Map<String, Object>> entityMeta = handler.getMetadata();
			for (Map<String, Object> m : entityMeta) {
				if (m.get("namedId") != null) {
					continue;
				}
				String getter = (String) m.get("getter");
				String name = (String) m.get("name");
				Method method = entityClass.getMethod(getter);
				Object value = method.invoke(entity);
				if (value != null /*name.equals("id")*/) {
			//		LOG.info("- Field(" + name + "):" + value);
				}
				e.setProperty(name, value);
			}
			return;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.updateById(" + entityName + "):" + e.getMessage());
			throw new RuntimeException("OrientdbPersistenceSession.updateById(" + entityName + ")", e);
		}
	}

	private Object fireEventForVariableInstanceEntityDelete(Class entityClass, String statement, List<CParameter> parameterList, BaseEntityHandler handler) {
		Map<String, Object> queryParams = new HashMap<String, Object>();
		OCommandRequest query = handler.buildQuery(entityClass.getSimpleName(), statement, parameterList, null, queryParams);
		Iterable<Element> result = orientGraph.command(query).execute(queryParams);
		Map<String, Object> props = null;
		for (Element elem : result) {
			props = ((OrientVertex) elem).getProperties();
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
			LOG.info("OrientdbPersistenceSession.getVariableInstanceEntity:" + e);
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

	private void dump(String msg, Object o) {
		if (true) return;
		ReflectionToStringBuilder rb = new ReflectionToStringBuilder(o, ToStringStyle.JSON_STYLE);
		rb.setExcludeNullValues(true);
		LOG.info("   +++" + msg + ":" + rb.toString());
	}

	private Method getStatementMethod(Object o, String statement, Object parameter) {
		if( !(parameter instanceof ListQueryParameterObject)){
			LOG.info("getStatementMethod("+statement+"):Parameter noz ListQueryParameterObject");
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

	private <Any> Any callStatementMethod(Method method, Object o, String statement, Object queryParams){
		LOG.info("   - callStatementMethod("+statement+"):"+method.getName());
		try{
			Object[] params = new Object[1];
			params[0] = queryParams;
			return (Any) method.invoke(o, params);
		}catch( Exception	e){
			throw new RuntimeException("OrientdbPersistenceSession.callStatementMethod("+statement+") error:",e);
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
		LOG.info("lock:" + statement);
	}

	public void commit() {
		LOG.info("commitSession:" + sessionId);
		System.err.println("commitSession:" + sessionId);
		orientGraph.commit();
	}

	public void rollback() {
		orientGraph.rollback();
		LOG.info("rollbackSession:" + sessionId);
		System.err.println("rollbackSession:" + sessionId);
	}

	public void flush() {
		// nothing to do
	}

	public void close() {
		// nothing to do
		if (this.isOpen) {
			LOG.info("closeSession:" + sessionId);
			System.err.println("closeSession:" + sessionId);
			orientGraph.shutdown();
		}
		this.isOpen = false;
	}

	public void dbSchemaCheckVersion() {
	}

	public int executeUpdate(String s, Object o) {
		LOG.info("executeUpdate:" + s + "/" + o);
		throw new RuntimeException("OrientdbPersistenceSession.executeUpdate not implemented");
	}

	public int executeNonEmptyUpdateStmt(String s, Object o) {
		LOG.info("executeNonEmptyUpdateStmt(" + s + "):");
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
}

