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

package org.camunda.bpm.engine.impl.db.orientdb.handler;

import com.github.raymanrt.orientqb.delete.Delete;
import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.Projection;
import com.github.raymanrt.orientqb.query.Query;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.Vertex;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.SingleQueryVariableValueCondition;
import static com.github.raymanrt.orientqb.query.Clause.and;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.not;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.GT;
import static com.github.raymanrt.orientqb.query.Operator.LIKE;
import static com.github.raymanrt.orientqb.query.Operator.LT;
import static com.github.raymanrt.orientqb.query.Operator.NULL;
import static com.github.raymanrt.orientqb.query.Parameter.parameter;
import static com.github.raymanrt.orientqb.query.Projection.ALL;
import static com.github.raymanrt.orientqb.query.Projection.projection;
import static com.github.raymanrt.orientqb.query.Variable.variable;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class BaseEntityHandler {

	private final static Logger LOG = Logger.getLogger(BaseEntityHandler.class.getName());
	protected OrientGraph orientGraph;
	protected Class entityClass;
	private List<Map<String, Object>> entityMetadata = new ArrayList<Map<String, Object>>();
	private Map<String, Map> metaByFieldMap = new HashMap<String, Map>();

	public BaseEntityHandler(OrientGraph g, Class ec) {
		this.entityClass = ec;
		this.orientGraph = g;
		this.entityMetadata = this.buildMetadata(ec);
		this.modifyMetadata();
		this.buildMetaFieldMap();
		//LOG.info("--> "+ this.entityClass.getSimpleName() );
		for (Map<String, Object> m : this.entityMetadata) {
			//LOG.info("  - "+ m );
		}
		createClassAndProperties();
	}

	private void buildMetaFieldMap() {
		for (Map<String, Object> m : entityMetadata) {
			this.metaByFieldMap.put((String) m.get("name"), m);
		}
	}

	public List<Map<String, Object>> getMetadata() {
		return this.entityMetadata;
	}

	public void setOrientGraph(OrientGraph orientGraph) {
		this.orientGraph = orientGraph;
	}

	public void modifyMetadata() {
	}

	public void createAdditionalProperties(OSchema schema, OClass oClass) {
	}

	public void modifyCParameterList(String statement, List<CParameter> parameterList) {
	}

	public void addToClauseList(List<Clause> clauseList, String statement, Object parameter, Map<String, Object> variables) {
	}

	public void postProcessQuery(Query q, String statement, List<CParameter> parameterList) {
	}

	public void insertAdditional(Vertex v, Object entity, Map<Object, List<Vertex>> entityCache) {
	}

	public Class getSubClass(Class entityClass, Map<String, Object> properties) {
		return entityClass;
	}

	public String getKeyForLatestGrouping(){
		return "key";
	}

	public CParameter getCParameter(List<CParameter> parameterList, String name) {
		for (CParameter p : parameterList) {
			if (p.name.equals(name)) {
				return p;
			}
		}
		return null;
	}

	public List<CParameter> getCParameterList(String statement, Object p) {
		if (p instanceof String) {
			int index = statement.indexOf("By");
			String byString = null;
			if (index > 0) {
				byString = statement.substring(index + 2);
				byString = firstToLower(byString);
			}
			if (byString != null) {
				if (this.metaByFieldMap.get(byString) == null) {
					byString = byString + "Id";
					LOG.info("byString3: " + byString);
					if (this.metaByFieldMap.get(byString) == null) {
						byString = null;
					}
				}
				if (byString != null) {
					List<CParameter> parameterList = new ArrayList<CParameter>();
					parameterList.add(new CParameter(byString, EQ, p));
					return parameterList;
				}
			}
			throw new RuntimeException("getCParameterList(" + statement + "," + this.entityClass.getSimpleName() + ",String) cannot be handled here:" + p);
		}
		ReflectionToStringBuilder rb = new ReflectionToStringBuilder(p, ToStringStyle.JSON_STYLE);
		rb.setExcludeNullValues(true);
		LOG.info("getCParameterList.Object: " + rb.toString());

		List<CParameter> parameterList = new ArrayList<CParameter>();
		List<Map<String, Object>> md = getMetadata();
		Class c = p.getClass();
		for (Map<String, Object> m : md) {
			String getter = (String) m.get("getter");
			boolean b = hasMethod(c, getter);
			Object val = null;
			if (b) {
				val = getValue(p, getter);
				if (val != null) {
					LOG.info("getter(" + getter + "," + b + "):" + val);
					parameterList.add(new CParameter((String) m.get("name"), EQ, val));
				} else {
					//LOG.info("getter(" + getter + "," + b + "):null");
				}
			}
		}
		for (Map<String, Object> m : md) {
			String getter = (String) m.get("getter");
			String name = (String) m.get("name");
			if (m.get("type") == String.class) {
				boolean b = hasMethod(c, getter + "Like");
				String val = null;
				if (b) {
					val = getValue(p, getter + "Like");
					if (val != null && !val.startsWith("null:")) { //@@@MS HistoricProcessInstanceQueryImpl???
						parameterList.add(new CParameter((String) m.get("name"), LIKE, val));
						LOG.info("getter(" + getter + "Like," + b + "):" + val);
					} else {
						//LOG.info("getter(" + getter + "," + b + ")Like:null");
					}
				}
			}
		}
		boolean b = hasMethod(c, "isLatest");
		if (b) {
			Boolean val = getValue(p, "isLatest");
			if (val != null && val == true) {
				parameterList.add(new CParameter("_isLatest", EQ, ""));
			}
		}

		LOG.info("getCParameterList:" + parameterList);
		return parameterList;
	}

	public void checkParameterList(List<CParameter> parameterList) {
		for (CParameter p : parameterList) {
			if (p.noCheck == false && this.metaByFieldMap.get(p.name) == null && !p.name.equals("_isLatest")) {
				throw new RuntimeException("BaseEntityHandler.checkParameterList(" + this.entityClass.getSimpleName() + "," + p.name + ") not found");
			}
			//			LOG.info("checked(" + entityClass.getSimpleName() + "." + p.name + ")");
		}
	}

	public OCommandRequest buildQuery(String entityName, String statement, List<CParameter> parameterList, Object parameter, Map<String, Object> queryParams) {
		modifyCParameterList(statement, parameterList);
		checkParameterList(parameterList);

		List<Clause> clauseList = new ArrayList<Clause>();
		boolean isLatest = false;
		for (CParameter p : parameterList) {
			Clause c = null;
			if (p.value == null) {
				c = projection(p.name).isNull();
			} else if (p.name.equals("_isLatest")) {
				isLatest = true;
			} else if (p.value instanceof Date) {
				c = clause(p.name, p.op, parameter(p.name));
			} else {
				c = clause(p.name, p.op, p.value);
			}
			if (c != null) {
				clauseList.add(c);
			}
		}
		if (statement.indexOf("Latest") > 0) {
			isLatest = true;
		}
		addToClauseList(clauseList, statement, parameter, queryParams);
		Clause w = and(clauseList.toArray(new Clause[clauseList.size()]));
		Query q = new Query().from(entityName).where(w);
		if (isLatest && this.metaByFieldMap.get("version") != null) {
			//q.limit(1);
			q.orderByDesc("version");
			queryParams.put("_isLatest", new Boolean(true));
		}

		postProcessQuery(q, statement, parameterList);

		OSQLSynchQuery query = new OSQLSynchQuery(q.toString());
		boolean hasVar = false;
		for (CParameter p : parameterList) {
			if (p.value instanceof Date) {
				queryParams.put(p.name, p.value);
			}
		}

		LOG.info("  - oquery:" + query);
		LOG.info("  - oquery.params:" + queryParams);
		return query;
	}

	public OCommandRequest buildDelete(String entityName, String statement, List<CParameter> parameterList, Map<String, Object> queryParams) {
		modifyCParameterList(statement, parameterList);
		checkParameterList(parameterList);

		List<Clause> clauseList = new ArrayList<Clause>();
		for (CParameter p : parameterList) {
			Clause c = null;
			if (p.value == null) {
				c = projection(p.name).isNull();
			} else if (p.value instanceof Date) {
				c = clause(p.name, p.op, parameter(p.name));
			} else {
				c = clause(p.name, p.op, p.value);
			}
			if (c != null) {
				clauseList.add(c);
			}
		}
		Clause w = and(clauseList.toArray(new Clause[clauseList.size()]));
		Delete q = new Delete().from(entityName).where(w);

		//postProcessQuery(q, statement, parameterList);

		String d = q.toString().replace("DELETE ", "DELETE VERTEX ");
		LOG.info("  - delete:" + d);
		OCommandRequest update = new OCommandSQL(d);
		return update;
	}

	private List<String> excludeList = new ArrayList<>(Arrays.asList("hashCode"));

	private List<Map<String, Object>> buildMetadata(Class clazz) {
		List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
		Method[] methods = clazz.getMethods();
		boolean hasId = false;
		boolean hasNamedId = false;
		String idName = getIdNameFromClassName(clazz);
		for (Method m : methods) {
			if (m.getParameterTypes().length > 0) {
				continue;
			}
			Class returnType = m.getReturnType();
			String name = m.getName();
			String baseName = getBaseName(name);
			String setter = getSetter(clazz, baseName);
			String prefix = getGetterPrefix(name);
			if (prefix != null && prefix.equals("get") && (returnType == boolean.class || returnType == Boolean.class)) {
				continue;
			}
			if (setter != null && !excludeList.contains(name) && !Modifier.isStatic(m.getModifiers()) && prefix != null && isPrimitiveOrPrimitiveWrapperOrString(returnType)) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("type", returnType);
				map.put("name", baseName);
				map.put("prefix", prefix);
				map.put("getter", name);
				map.put("setter", setter);
				map.put("otype", OType.getTypeByClass(returnType));
				if (!containsGetter(name, fieldList)) {
					//					LOG.info("fieldList("+this.entityClass.getSimpleName()+").add:"+map);
					if ("id".equals(baseName.toLowerCase())) {
						hasId = true;
					}
					if (idName.equals(baseName.toLowerCase())) {
						hasNamedId = true;
					}
					fieldList.add(map);
				}
			}
		}
		if (hasId && !hasNamedId) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("type", String.class);
			map.put("name", "id");
			map.put("namedId", true);
			map.put("prefix", "get");
			map.put("getter", "get" + firstToUpper(idName));
			map.put("setter", "set" + firstToUpper(idName));
			map.put("otype", OType.getTypeByClass(String.class));
			//LOG.info("Adding namedId to "+clazz.getSimpleName()+":"+map);
			fieldList.add(map);
		}
		return fieldList;
	}

	protected <Any> Any getValueFromMap(Map<String, Object> map, String key) {
		if (map == null || map.get(key) == null)
			return null;
		return (Any) map.get(key);
	}

	protected <Any> Any getValue(Object obj, String methodName) {
		try {
			Method method = obj.getClass().getMethod(methodName, (Class[]) null);
			return (Any) method.invoke(obj);
		} catch (Exception e) {
			//			LOG.info("BaseEntityHandler.getValue:" + obj.getClass().getSimpleName() + "." + methodName + " not found");
			return null;
		}
	}

	protected <Any> Any getValueByField(Object obj, String fieldName) {
		try {
			if (obj == null) {
				LOG.info("BaseEntityHandler.getValueByField(" + fieldName + ") obj is null");
				return null;
			}
			if (obj instanceof Map) {
				return (Any) ((Map) obj).get(fieldName);
			}
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Any) field.get(obj);
		} catch (Exception e) {
			LOG.info("BaseEntityHandler.getValueByField:" + obj.getClass().getSimpleName() + "." + fieldName + " not found");
			return null;
		}
	}

	protected boolean hasMethod(Class c, String methodName) {
		try {
			Method method = c.getMethod(methodName, (Class[]) null);
			return method != null;
		} catch (Exception e) {
			return false;
		}
	}

	private String getSetter(Class clazz, String baseName) {
		Method[] methods = clazz.getMethods();
		String setter = "set" + firstToUpper(baseName);
		for (Method m : methods) {
			Class returnType = m.getReturnType();
			Class[] parameterTypes = m.getParameterTypes();
			String name = m.getName();
			if (name.equals(setter) && !Modifier.isStatic(m.getModifiers()) && returnType.equals(Void.TYPE) && parameterTypes.length == 1 && isPrimitiveOrPrimitiveWrapperOrString(parameterTypes[0])) {
				return setter;
			}
		}
		return null;
	}

	private String getBaseName(String methodName) {
		if (methodName.startsWith("get")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("has")) {
			return firstToLower(methodName.substring(3));
		}
		if (methodName.startsWith("is")) {
			return firstToLower(methodName.substring(2));
		}
		return firstToLower(methodName);
	}

	private String[] getterPrefixes = new String[] { "is", "has", "get" };

	private String getGetterPrefix(String mName) {
		for (String pre : getterPrefixes) {
			if (mName.startsWith(pre)) {
				return pre;
			}
		}
		return null;
	}

	private List<String> notRestrictedList = new ArrayList<>(Arrays.asList("IdentityLinkEntity", "DeploymentEntity", "ProcessDefinitionEntity", "PropertyEntity", "ResourceEntity"));

	protected void createClassAndProperties() {
		try {
			String entityName = this.entityClass.getSimpleName();
			OSchemaProxy schema = this.orientGraph.getRawGraph().getMetadata().getSchema();
			LOG.info("createClassAndProperties:" + entityName);
			OClass oClass = getOrCreateClass(schema, entityName);
			for (Map<String, Object> f : this.entityMetadata) {
				String pName = (String) f.get("name");
				if (f.get("namedId") != null) {
					continue;
				}
				OType oType = (OType) f.get("otype");
				getOrCreateProperty(oClass, pName, oType);
			}
			getOrCreateProperty(oClass, "dbRevision", OType.INTEGER);
			createAdditionalProperties(schema, oClass);
			//m_orientdbService.executeUpdate(orientGraph, "CREATE INDEX History.key ON History ( key ) NOTUNIQUE");
		} catch (Exception e) {
			throw new RuntimeException("BaseEntityHandler.createClassAndProperties", e);
		}
	}

	protected OClass getOrCreateClass(OSchema schema, String className) {
		OClass oClass = schema.getClass(className);
		if (oClass == null) {
			oClass = schema.createClass(className);
			if (notRestrictedList.contains(className)) {
				setSuperClasses(schema, oClass, false);
			} else {
				setSuperClasses(schema, oClass, true);
			}
		}
		return oClass;
	}

	protected void setSuperClasses(OSchema schema, OClass oClass, boolean restricted) {
		List<OClass> superList = new ArrayList<OClass>();
		superList.add(schema.getClass("V"));
		if (restricted) {
			superList.add(schema.getClass("ORestricted"));
		}
		oClass.setSuperClasses(superList);
	}

	protected OProperty getOrCreateProperty(OClass oClass, String propertyName, OType oType) {
		OProperty prop = oClass.getProperty(propertyName);
		if (prop == null) {
			prop = oClass.createProperty(propertyName, oType);
		}
		return prop;
	}

	protected OProperty getOrCreateLinkedProperty(OClass oClass, String propertyName, OType type, OClass linkedClass) {
		OProperty prop = oClass.getProperty(propertyName);
		if (prop == null) {
			prop = oClass.createProperty(propertyName, type, linkedClass);
		}
		return prop;
	}

	protected void removeByGetter(String getter) {
		for (Iterator<Map<String, Object>> iter = this.entityMetadata.listIterator(); iter.hasNext();) {
			Map<String, Object> m = iter.next();
			if (getter.equals(m.get("getter"))) {
				iter.remove();
			}
		}
	}

	protected void setSetterByGetter(String getter, String setter) {
		for (Iterator<Map<String, Object>> iter = this.entityMetadata.listIterator(); iter.hasNext();) {
			Map<String, Object> m = iter.next();
			if (getter.equals(m.get("getter"))) {
				m.put("setter", setter);
			}
		}
	}

	protected boolean containsGetter(String getter, List<Map<String, Object>> list) {
		for (Iterator<Map<String, Object>> iter = list.listIterator(); iter.hasNext();) {
			Map<String, Object> m = iter.next();
			if (getter.equals(m.get("getter"))) {
				return true;
			}
		}
		return false;
	}

	protected void addToMeta(String name, String getter, String setter, Class type) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("name", name);
		map.put("prefix", getGetterPrefix(getter));
		map.put("getter", getter);
		map.put("setter", setter);
		map.put("otype", OType.getTypeByClass(type));
		LOG.info("addToMeta:" + map);
		this.entityMetadata.add(map);
	}

	private void executeUpdate(OrientGraph graph, String sql, Object... args) {
		OCommandRequest update = new OCommandSQL(sql);
		graph.command(update).execute(args);
	}

	protected boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
		return (type.isPrimitive() && type != void.class) || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class || type == java.util.Date.class || type == byte[].class;
	}

	protected String firstToLower(String s) {
		char c[] = s.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}

	protected String firstToUpper(String s) {
		char c[] = s.toCharArray();
		c[0] = Character.toUpperCase(c[0]);
		return new String(c);
	}

	protected <E> Collection<E> makeCollection(Iterable<E> iter) {
		Collection<E> list = new ArrayList<E>();
		for (E item : iter) {
			list.add(item);
		}
		return list;
	}

	protected <T> Collection<T> makeCollection(Iterable<T> it1, Iterable<T> it2) {
		Collection<T> result = new ArrayList<T>();
		if (it1 != null) {
			for (T it : it1) {
				result.add(it);
			}
		}
		if (it2 != null) {
			for (T it : it2) {
				result.add(it);
			}
		}
		return result;
	}

	protected Iterable<Element> queryList(String sql, Object... args) {
		LOG.info("   - queryList:" + sql);
		Iterable<Element> iter = this.orientGraph.command(new OSQLSynchQuery<>(sql)).execute(args);
		return iter;
	}

	private String getIdNameFromClassName(Class c) {
		String name = c.getSimpleName();
		int len = name.length();
		String base = c.getSimpleName().substring(0, len - "Entity".length());
		return firstToLower(base) + "Id";
	}

	protected void dump(String msg, Object o) {
		if (o == null) {
			LOG.info("   +++" + msg + ":null");
			return;
		}
		ReflectionToStringBuilder rb = new ReflectionToStringBuilder(o, ToStringStyle.JSON_STYLE);
		rb.setExcludeNullValues(true);
		LOG.info("   +++" + msg + ":" + rb.toString());
	}

	public void settingLinksReverse(Object entity, String idMethod, String destClass, String propertyName, Vertex v, Map<Object, List<Vertex>> entityCache) {
		String id = getValue(entity, idMethod);
		String entityName = entity.getClass().getSimpleName();
		LOG.info(entityName + ".insertAdditional(" + id + "):" + v);
		Iterable<Vertex> result = entityCache.get(id + destClass);
		if (id != null) {
			OCommandRequest query = new OSQLSynchQuery("select from " + destClass + " where id=?");
			Iterable<Vertex> result2 = orientGraph.command(query).execute(id);
			if (result2 != null) {
				result = makeCollection(result, result2);
			}
		}
		LOG.info(entityName + ".resultFromCache(" + id + "):" + result);
		if (result == null) {
			LOG.info(entityName + ".settingLinksReverse(" + id + "):not found");
			return;
		}
		for (Element elem : result) {
			Iterable<Element> iter = elem.getProperty(propertyName);
			if (iter == null) {
				LOG.info(destClass + "(" + elem + ").settingLinksReverse." + propertyName + ":" + v);
				List<Element> l = new ArrayList<Element>();
				l.add(v);
				elem.setProperty(propertyName, l);
			} else {
				Collection<Element> col = makeCollection(iter);
				LOG.info(destClass + "(" + elem + ").settingLinksReverse." + propertyName + "(" + iter.getClass().getName() + "," + col + "):" + v);
				col.add(v);
				elem.setProperty(propertyName, col);
			}
		}
	}

	public void settingLink(Object entity, String idMethod, String destClass, String propertyName, Vertex v, Map<Object, List<Vertex>> entityCache) {
		String id = getValue(entity, idMethod);
		if (id == null) {
			return;
		}
		Iterable<Vertex> result = entityCache.get(id + destClass);
		if (result == null) {
			OCommandRequest query = new OSQLSynchQuery("select from " + destClass + " where id=?");
			result = orientGraph.command(query).execute(id);
		}
		if (result == null) {
			return;
		}
		Iterator<Vertex> it = result.iterator();
		if (it.hasNext()) {
			Vertex parent = it.next();
			LOG.info(entity.getClass().getSimpleName() + ".settingLink(" + v + ").to:" + parent);
			v.setProperty(propertyName, parent);
		}
	}

	public void settingLinks(Object entity, String idMethod, Vertex v, String propertyName, String destClass, String destProperty, Map<Object, List<Vertex>> entityCache) {
		String id = getValue(entity, idMethod);
		String entityName = entity.getClass().getSimpleName();
		LOG.info(entityName + ".settingLinks(" + id + "):" + v);
		Iterable<Vertex> result = entityCache.get(id + destClass);
		if (id != null) {
			String sql = "select from " + destClass + " where "+destProperty+"=?";
		  LOG.info(entityName + ".sql(" + sql + ")" );
			OCommandRequest query = new OSQLSynchQuery(sql);
			Iterable<Vertex> result2 = orientGraph.command(query).execute(id);
			if (result2 != null) {
				result = makeCollection(result, result2);
			}
		}
		LOG.info(entityName + ".resultFromCache(" + id + "):" + result);
		if (result == null) {
			LOG.info(entityName + ".settingLinks(" + id + "):not found");
			return;
		}
		LOG.info(entity.getClass().getSimpleName() + ".settingLinks(" + v + ").to:" + result);
		v.setProperty(propertyName, result);
	}

	protected String getQuotedValue(SingleQueryVariableValueCondition cond) {
		switch (cond.getType()) {
		case "string":
			return "'" + cond.getTextValue() + "'";
		case "long":
		case "integer":
		case "boolean":
			return String.valueOf(cond.getLongValue());
		case "double":
			return String.valueOf(cond.getDoubleValue());
		default:
			return "unknow value";
		}
	}

	protected String getValueField(String type) {
		switch (type) {
		case "string":
			return "textValue";
		case "long":
		case "integer":
		case "boolean":
			return "longValue";
		case "double":
			return "doubleValue";
		default:
			return "textValue";
		}
	}

	protected String convertOperator(QueryOperator operator) {
		switch (operator) {
		case GREATER_THAN:
			return ">";
		case GREATER_THAN_OR_EQUAL:
			return ">=";
		case LESS_THAN:
			return "<";
		case LESS_THAN_OR_EQUAL:
			return "<=";
		case LIKE:
			return "LIKE";
		case NOT_EQUALS:
			return "!=";
		case EQUALS:
			return "=";
		default:
			LOG.info("ExecutionEntityHandler.warning:can operator(" + operator + ") not convert");
			return "=";
		}
	}
}

