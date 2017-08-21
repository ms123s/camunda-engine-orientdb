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

	protected OrientGraph orientGraph;

	public OrientdbPersistenceSession(OrientGraph g, boolean openTransaction) {
		this.orientGraph = g;
		g.getRawGraph().activateOnCurrentThread();
		this.isOpen = true;
		sessionId = new java.util.Date().getTime();
		LOG.info("openSession:" + sessionId);
	}

	public Object selectOne(String statement, Object parameter) {
		LOG.info("selectOne:" + statement);
		String prefix = getPrefix(statement);
		String suffix = getSuffix(statement);
		String entityName = getEntityName(statement, prefix, suffix);
		Class entityClass = OrientdbSessionFactory.getEntityClass(entityName);
		LOG.info("->selectOne(" + statement + "," + entityName + "):" + parameter);

		BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass);

		List<CParameter> parameterList = getCParameterList(statement, parameter, entityHandler);
		LOG.info("  - CParameterList:" + parameterList);
		OCommandRequest query = entityHandler.buildQuery(entityName, statement, parameterList);

		Iterable<Element> result = orientGraph.command(query).execute();
		LOG.info("  - result:" + result);
		Map<String, Object> props = null;
		for (Element elem : result) {
			props = ((OrientVertex) elem).getProperties();
			break;
		}
		if (props == null) {
			if (statement.indexOf("Count") > 0) {
				LOG.info("<-selectOne(" + entityName + ").count:0");
				return new Long(0);
			} else {
				LOG.info("<-selectOne(" + entityName + ").return:null");
				return null;
			}
		}
		try {
			if (statement.indexOf("Count") > 0) {
				int count = 0;
				for (Element elem : result) {
					count++;
				}
				LOG.info("<-selectOne.count(" + entityName + ").return:" + count);
				return new Long(count);
			}
			Object entity = entityClass.newInstance();
			setEntityValues(entityClass, entity, props);
			dump("selectOne(" + entityName + ")", entity);
			LOG.info("<-selectOne(" + entityName + ").return:" + entity);
			return entity;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectOne:" + e.getMessage());
			LOG.throwing("OrientdbPersistenceSession", "selectOne", e);
			e.printStackTrace();
		}
		LOG.info("<-selectOne(" + entityName + ").return:null");
		return null;
	}

	public List<?> selectList(String statement, Object parameter) {
		LOG.info("selectList:" + statement);
		String prefix = getPrefix(statement);
		String suffix = getSuffix(statement);
		String entityName = getEntityName(statement, prefix, suffix);
		LOG.info("selectList(" + statement + "," + entityName + "):" + parameter);

		Class entityClass = OrientdbSessionFactory.getEntityClass(entityName);
		LOG.info("  - entityClass:" + entityClass);

		BaseEntityHandler entityHandler = OrientdbSessionFactory.getEntityHandler(entityClass);

		List<CParameter> parameterList = getCParameterList(statement, parameter, entityHandler);
		LOG.info("  - CParameterList:" + parameterList);
		OCommandRequest query = entityHandler.buildQuery(entityName, statement, parameterList);

		Iterable<Element> result = orientGraph.command(query).execute();
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
					Object entity = entityClass.newInstance();
					setEntityValues(entityClass, entity, props);
					dump("selectList(" + entityName + ")", entity);
					entityList.add(entity);
				}
				LOG.info("<-selectList3(" + entityName + ").return:" + entityList);
				return entityList;
			}
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectList:" + e.getMessage());
			LOG.throwing("OrientdbPersistenceSession", "selectList", e);
			e.printStackTrace();
		}
		LOG.info("<-selectList4(" + entityName + ").return:null");
		return new ArrayList();
	}

	private List<CParameter> getCParameterList(String statement, Object parameter, BaseEntityHandler handler) {
		LOG.info("  - getParameterList(" + statement + "):" + parameter);
		if (parameter instanceof AbstractQuery) {
			return handler.getCParameterList(statement, parameter);
		} else if (parameter instanceof ListQueryParameterObject) {
			LOG.info("   - ListQueryParameterObject");
			if (((ListQueryParameterObject) parameter).getParameter() instanceof String) {
				Object obj = ((ListQueryParameterObject) parameter).getParameter();
				LOG.info("   - String1:" + obj);
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
		LOG.info("->selectById(" + entityName + ").id:" + id);
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
			LOG.info("<-selectById(" + entityName + ").return:null");
			return null;
		}
		try {
			T entity = (T) entityClass.newInstance();
			setEntityValues(entityClass, entity, props);
			dump("selectById(" + entityName + "," + id + ")", entity);
			LOG.info("<-selectById(" + entityName + ").return:" + entity);
			return entity;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.selectById:" + e.getMessage());
			LOG.throwing("OrientdbPersistenceSession", "selectById", e);
			e.printStackTrace();
		}
		LOG.info("<-selectById(" + entityName + ").return:null");
		return null;
	}

	private void setEntityValues(Class entityClass, Object entity, Map<String, Object> props) throws Exception {
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass);
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

			Method method = entityClass.getMethod(setter, args);
			method.invoke(entity, value);
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
		} else if (statement.startsWith("selectNextJobsToExecute")) {
			name = "Job";
		} else if (statement.startsWith("selectVariablesBy")) {
			name = "VariableInstance";
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
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass);

		if (entity instanceof HasDbRevision) {
			((HasDbRevision) entity).setRevision(1);
		}

		try {
			Vertex v = this.orientGraph.addVertex("class:" + entityName);
			List<Map<String, Object>> entityMeta = handler.getMetadata();
			for (Map<String, Object> m : entityMeta) {
				if (m.get("namedId") != null) {
					continue;
				}
				String getter = (String) m.get("getter");
				String name = (String) m.get("name");
				Method method = entityClass.getMethod(getter);
				Object value = method.invoke(entity);
				//if( name.equals("id")){
				LOG.info("- Field(" + name + "):" + value);
				//}
				v.setProperty(name, value);
			}
			LOG.info("<- insertEntity(" + entityName + "):ok");
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.insertEntity:" + e.getMessage());
			LOG.throwing("OrientdbPersistenceSession", "insertEntity", e);
			e.printStackTrace();
		}
	}

	protected void deleteEntity(DbEntityOperation operation) {
		Object entity = operation.getEntity();
		Class entityClass = OrientdbSessionFactory.getReplaceClass(entity.getClass());
		String entityName = entity.getClass().getSimpleName();
		String id = getValue(entity, "getId");
		LOG.info("-> deleteEntity(" + entityName + "):" + id);
		OCommandRequest del = new OCommandSQL("Delete vertex " + entityName + " where id=?");
		orientGraph.command(del).execute(id);
		LOG.info("<- deleteEntity(" + entityName + "):ok");
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
		BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass);
		LOG.info("-> deleteBulk(" + statement + "," + entityName + ").parameter:" + parameter);
		List<CParameter> parameterList = getCParameterList(statement, parameter, handler);
		LOG.info("  - CParameterList:" + parameterList);

		if (parameterList.size() > 0) {
			OCommandRequest up = handler.buildDelete(entityName, statement, parameterList);
			orientGraph.command(up).execute();
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
		String id = getValue(entity, "getId");
		LOG.info("-> updateEntity(" + entityName + "," + id + ")");
		updateById(entity, id);
		LOG.info("<- updateEntity(" + entityName + "):ok");
	}

	private void updateById(Object entity, String id) {
		Class entityClass = OrientdbSessionFactory.getReplaceClass(entity.getClass());
		String entityName = entity.getClass().getSimpleName();
		OCommandRequest query = new OSQLSynchQuery("select  from " + entityName + " where id=?");
		Iterable<Element> result = orientGraph.command(query).execute(id);
		Iterator<Element> it = result.iterator();
		if (!it.hasNext()) {
			LOG.info(" - UpdateById(" + entityName + "," + id + ").not found");
			return;
		}
		try {
			Element e = it.next();

			BaseEntityHandler handler = OrientdbSessionFactory.getEntityHandler(entityClass);
			List<Map<String, Object>> entityMeta = handler.getMetadata();
			for (Map<String, Object> m : entityMeta) {
				if (m.get("namedId") != null) {
					continue;
				}
				String getter = (String) m.get("getter");
				String name = (String) m.get("name");
				Method method = entityClass.getMethod(getter);
				Object value = method.invoke(entity);
				//if( name.equals("id")){
				LOG.info("- Field(" + name + "):" + value);
				//}
				e.setProperty(name, value);
			}
			return;
		} catch (Exception e) {
			LOG.info("OrientdbPersistenceSession.updateById:" + e.getMessage());
			LOG.throwing("OrientdbPersistenceSession", "updateById", e);
			e.printStackTrace();
		}
		return;
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
		ReflectionToStringBuilder rb = new ReflectionToStringBuilder(o, ToStringStyle.JSON_STYLE);
		rb.setExcludeNullValues(true);
		LOG.info("+++" + msg + ":\n" + rb.toString());
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
		LOG.info("executeUpdate:" + s + "/" + o);
		return 0;
	}

	public int executeNonEmptyUpdateStmt(String s, Object o) {
		LOG.info("executeNonEmptyUpdateStmt:" + s + "/" + o);
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

