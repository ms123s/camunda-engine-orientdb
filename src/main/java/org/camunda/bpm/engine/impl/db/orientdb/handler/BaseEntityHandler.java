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
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
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
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.SingleQueryVariableValueCondition;
import org.camunda.bpm.engine.impl.QueryVariableValue;
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
import org.camunda.bpm.engine.impl.db.orientdb.SingleExpression;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
public abstract class BaseEntityHandler {

	private final static Logger LOG = Logger.getLogger(BaseEntityHandler.class.getName());
	protected Class entityClass;
	private List<Map<String, Object>> entityMetadata = new ArrayList<Map<String, Object>>();
	private Map<String, Map> metaByFieldMap = new HashMap<String, Map>();

	public BaseEntityHandler(ODatabaseSession g, Class ec) {
		this.entityClass = ec;
		this.entityMetadata = this.buildMetadata(ec);
		this.modifyMetadata();
		this.buildMetaFieldMap();
		//LOG.info("--> "+ this.entityClass.getSimpleName() );
		for (Map<String, Object> m : this.entityMetadata) {
			//LOG.info("  - "+ m );
		}
		createClassAndProperties(g);
	}

	private void buildMetaFieldMap() {
		for (Map<String, Object> m : entityMetadata) {
			this.metaByFieldMap.put((String) m.get("name"), m);
		}
	}

	public List<Map<String, Object>> getMetadata() {
		return this.entityMetadata;
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
	public String postProcessQueryLiteral(String q, String statement, List<CParameter> parameterList) {
		return q;
	}

	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
	}
	public String getCacheName(Object entity, String entityName) {
		String id = getValue(entity, "getId");
		if( id != null){
			return id+entityName;
		}
		return null;
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
					debug("byString3: " + byString);
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
		debug("getCParameterList.Object: " + rb.toString());

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
					debug("getter(" + getter + "," + b + "):" + val);
					parameterList.add(new CParameter((String) m.get("name"), EQ, val));
				} else {
					//LOG.info("getter(" + getter + "," + b + "):null");
				}
			}
//			LOG.info("CParameter("+getter+","+b+"):"+val);
		}
		for (Map<String, Object> m : md) {
			String getter = (String) m.get("getter");
			String name = (String) m.get("name");
			if (m.get("type") == String.class) {
				boolean b = hasMethod(c, getter + "Like");
				String val = null;
				if (b) {
					val = getValue(p, getter + "Like");
					if (!"getProcessDefinitionId".equals(getter) &&  val != null ) { //@@@MS HistoricProcessInstanceQueryImpl. getProcessDefinitionIdLike???
						parameterList.add(new CParameter((String) m.get("name"), LIKE, val));
						debug("getter(" + getter + "Like," + b + "):" + val);
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

		debug("getCParameterList:" + parameterList);
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
		String qstr = postProcessQueryLiteral(q.toString(), statement, parameterList);

		OSQLSynchQuery query = new OSQLSynchQuery(qstr);
		boolean hasVar = false;
		for (CParameter p : parameterList) {
			if (p.value instanceof Date) {
				queryParams.put(p.name, p.value);
			}
		}

		debug("  - oquery:" + query);
		debug("  - oquery.params:" + queryParams);
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
		debug("  - delete:" + d);
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
				debug("BaseEntityHandler.getValueByField(" + fieldName + ") obj is null");
				return null;
			}
			if (obj instanceof Map) {
				return (Any) ((Map) obj).get(fieldName);
			}
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Any) field.get(obj);
		} catch (Exception e) {
			debug("BaseEntityHandler.getValueByField:" + obj.getClass().getSimpleName() + "." + fieldName + " not found");
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

	protected void createClassAndProperties(ODatabaseSession dbSession) {
		try {
			String entityName = this.entityClass.getSimpleName();
			OSchema schema = dbSession.getMetadata().getSchema();
			debug("createClassAndProperties:" + entityName);
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
			//m_orientdbService.executeUpdate(databaseSession, "CREATE INDEX History.key ON History ( key ) NOTUNIQUE");
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
		debug("addToMeta:" + map);
		this.entityMetadata.add(map);
	}

	private void executeUpdate(ODatabaseSession graph, String sql, Object... args) {
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

	protected Iterable<OElement> queryList(String sql, Object... args) {
		debug("   - queryList:" + sql);
		ODatabaseDocumentInternal currentDatabase = ODatabaseRecordThreadLocal.instance().get();
		Iterable<OElement> iter = currentDatabase.command(new OSQLSynchQuery<>(sql)).execute(args);
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
			debug("   +++" + msg + ":null");
			return;
		}
		ReflectionToStringBuilder rb = new ReflectionToStringBuilder(o, ToStringStyle.JSON_STYLE);
		rb.setExcludeNullValues(true);
		debug("   +++" + msg + ":" + rb.toString());
	}

	private List<OVertex> getFromCache(String id, String destProperty, String destClass, Map<Object, List<OVertex>> entityCache) {
		List<OVertex> retList = new ArrayList<OVertex>();
		for ( Object key : entityCache.keySet() ) {
			if( (""+key).endsWith( destClass)){
				for( OVertex v : entityCache.get(key)){
					String vid = v.getProperty( destProperty);
					if( vid == id ){
						retList.add(v);
					}
				}
			}
		}
		return retList;
	}

	public void settingLinkReverse(Object entity, String idMethod,String destProperty, String destClass, String propertyName, OVertex v, Map<Object, List<OVertex>> entityCache) {
		String id = getValue(entity, idMethod);
		String entityName = entity.getClass().getSimpleName();
		debug(entityName + ".settingLinkReverse(" + id+"/"+v+"/"+destClass+ "):" + entityCache);
		Iterable<OVertex> result = getFromCache(id , destProperty, destClass,entityCache);
		ODatabaseDocumentInternal currentDatabase = ODatabaseRecordThreadLocal.instance().get();
		if (id != null) {
			OCommandRequest query = new OSQLSynchQuery("select from " + destClass + " where "+destProperty+"=?");
			Iterable<OVertex> result2 = currentDatabase.command(query).execute(id);
			if (result2 != null) {
				result = makeCollection(result, result2);
			}
		}
		debug(entityName + ".settingLinkReverse.resultFromCache(" + id + "):" + result);
		if (result == null) {
			debug(entityName + ".settingLinkReverse(" + id + "):not found");
			return;
		}
		for (OElement elem : result) {
			debug(destClass + "(" + elem + ").settingLinkReverse." + propertyName + "(" + elem.getClass().getName() +  "):" + elem);
			elem.setProperty(propertyName, v);
		}
	}
	public void settingLinksReverse(Object entity, String idMethod, String destClass, String propertyName, OVertex v, Map<Object, List<OVertex>> entityCache) {
		String id = getValue(entity, idMethod);
		String entityName = entity.getClass().getSimpleName();
		debug(entityName + ".insertAdditional(" + id + "):" + v);
		Iterable<OVertex> result = entityCache.get(id + destClass);
		ODatabaseDocumentInternal currentDatabase = ODatabaseRecordThreadLocal.instance().get();
		if (id != null) {
			OCommandRequest query = new OSQLSynchQuery("select from " + destClass + " where id=?");
			Iterable<OVertex> result2 = currentDatabase.command(query).execute(id);
			if (result2 != null) {
				result = makeCollection(result, result2);
			}
		}
		debug(entityName + ".resultFromCache(" + id + "):" + result);
		if (result == null) {
			debug(entityName + ".settingLinksReverse(" + id + "):not found");
			return;
		}
		for (OElement elem : result) {
			Iterable<OElement> iter = elem.getProperty(propertyName);
			if (iter == null) {
				debug(destClass + "(" + elem + ").settingLinksReverse." + propertyName + ":" + v);
				List<OElement> l = new ArrayList<OElement>();
				l.add(v);
				elem.setProperty(propertyName, l);
			} else {
				Collection<OElement> col = makeCollection(iter);
				debug(destClass + "(" + elem + ").settingLinksReverse." + propertyName + "(" + iter.getClass().getName() + "," + col + "):" + v);
				col.add(v);
				elem.setProperty(propertyName, col);
			}
		}
	}

	public void settingLink(Object entity, String idMethod, String destClass, String propertyName, OVertex v, Map<Object, List<OVertex>> entityCache) {
		String id = getValue(entity, idMethod);
		debug(entity.getClass().getSimpleName() + ".settingLink(" + idMethod+","+destClass+","+propertyName + "):"+id);
		if (id == null) {
			return;
		}
		Iterable<OVertex> result = entityCache.get(id + destClass);
		if (result == null) {
			OCommandRequest query = new OSQLSynchQuery("select from " + destClass + " where id=?");
			ODatabaseDocumentInternal currentDatabase = ODatabaseRecordThreadLocal.instance().get();
			result = currentDatabase.command(query).execute(id);
		}
		debug(entity.getClass().getSimpleName() + ".settingLink:"+result);
		if (result == null) {
			return;
		}
		Iterator<OVertex> it = result.iterator();
		if (it.hasNext()) {
			OVertex parent = it.next();
			debug(entity.getClass().getSimpleName() + ".settingLink(" + v + ").to:" + parent);
			v.setProperty(propertyName, parent);
		}
	}

	public void settingLinks(Object entity, String idMethod, OVertex v, String propertyName, String destClass, String destProperty, Map<Object, List<OVertex>> entityCache) {
		String id = getValue(entity, idMethod);
		String entityName = entity.getClass().getSimpleName();
		debug(entityName + ".settingLinks(" + id + "):" + v);
		debug(entityName + ".settingLinks(entityCache):" + entityCache);
		Iterable<OVertex> result = entityCache.get(id + destClass);
		if (id != null) {
			String sql = "select from " + destClass + " where "+destProperty+"=?";
		  debug(entityName + ".sql(" + sql + ")" );
			OCommandRequest query = new OSQLSynchQuery(sql);
			ODatabaseDocumentInternal currentDatabase = ODatabaseRecordThreadLocal.instance().get();
			Iterable<OVertex> result2 = currentDatabase.command(query).execute(id);
			if (result2 != null) {
				result = makeCollection(result, result2);
			}
		}
		debug(entityName + ".resultFromCache(" + id + "):" + result);
		if (result == null) {
			debug(entityName + ".settingLinks(" + id + "):not found");
			return;
		}
		debug(entity.getClass().getSimpleName() + ".settingLinks(" + v + ").to:" + result);
		v.setProperty(propertyName, result);
	}


	protected SingleExpression  getExpression(QueryVariableValue var, SingleQueryVariableValueCondition cond) {
		String textValue = cond.getTextValue();
		if( cond.getType().equals("string")){
			if (Stream.of("match ", "matches ", "like ", "!= ", "= ", "> ", "< ", "<= ", ">= ").anyMatch(s -> textValue.toLowerCase().startsWith(s))){
				int b = textValue.indexOf(" ");
				String op = textValue.substring(0,b).trim().toUpperCase();
				if( "MATCH".equals(op)) op = "MATCHES";
				String val = textValue.substring(b+1).trim();
				return _getExpression( op, val, "textValue");
			}
		}
		String valueField = getValueField(cond.getType());
		String value = getQuotedValue(cond);
		String op = convertOperator(var.getOperator());
		return new SingleExpression( op, value, valueField);
	}

	protected SingleExpression  _getExpression(String op, String val, String valueField) {
		debug("_getExpression("+op+","+val+")");
		val = val.trim();
		if( "MATCHES".equals(op) || "LIKE".equals(op)){
			if( val.startsWith("'") && val.endsWith("'")){
				return new SingleExpression( op, val, "textValue");
			}
			if( val.startsWith("\"") && val.endsWith("\"")){
				int l = val.length();
				return new SingleExpression( op, "'" + val.substring(1,l-2)+"'", "textValue");
			}
			return new SingleExpression( op, "'" + val + "'", "textValue");
		}
		if( val.startsWith("'") && val.endsWith("'")){
			return new SingleExpression( op, val, "textValue");
		}else if( val.startsWith("\"") && val.endsWith("\"")){
			int l = val.length();
			return new SingleExpression( op, "'" + val.substring(1,l-2)+"'", "textValue");
		}else if( val.indexOf(".") >=0){
			try{
				Object v = Double.parseDouble( val);
				return new SingleExpression( op, v.toString(), "doubleValue");
			}catch( Exception e){
				debug("_getExpression.Exception:"+e);
				return new SingleExpression( op,"'"+ val+"'", "textValue");
			}
		}else if( val.toLowerCase().equals("true") ){
			return new SingleExpression( op, "1", "longValue");
		}else if( val.toLowerCase().equals("false")){
			return new SingleExpression( op, "0", "longValue");
		}else {
			try{
				Object v = Integer.parseInt( val);
				return new SingleExpression( op, v.toString(), "longValue");
			}catch( Exception e){
				debug("_getExpression.Exception:"+e);
				return new SingleExpression( op,"'"+ val+"'", "textValue");
			}
		}
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
			debug("ExecutionEntityHandler.warning:can operator(" + operator + ") not convert");
			return "=";
		}
	}

	private void debug(String msg){
		//LOG.fine(msg);
		com.jcabi.log.Logger.debug(this,msg);
	}
}

