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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.lang.reflect.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.github.raymanrt.orientqb.query.Clause;
import com.github.raymanrt.orientqb.query.Projection;
import com.github.raymanrt.orientqb.query.Query;
import static com.github.raymanrt.orientqb.query.Clause.and;
import static com.github.raymanrt.orientqb.query.Clause.clause;
import static com.github.raymanrt.orientqb.query.Clause.not;
import static com.github.raymanrt.orientqb.query.Clause.or;
import static com.github.raymanrt.orientqb.query.Operator.EQ;
import static com.github.raymanrt.orientqb.query.Operator.NULL;
import static com.github.raymanrt.orientqb.query.Parameter.parameter;
import static com.github.raymanrt.orientqb.query.Projection.ALL;
import static com.github.raymanrt.orientqb.query.Projection.projection;
import static com.github.raymanrt.orientqb.query.Variable.variable;
import org.camunda.bpm.engine.impl.db.orientdb.CParameter;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class BaseEntityHandler {

	private final static Logger LOG = Logger.getLogger(BaseEntityHandler.class.getName());
	protected OrientGraph orientGraph;
	protected Class entityClass;
	private List<Map<String, Object>> entityMetadata = new ArrayList<Map<String, Object>>();
	private Map<String,Map> metaByFieldMap = new HashMap<String,Map>();

	public BaseEntityHandler(OrientGraph g, Class ec) {
		this.entityClass = ec;
		this.orientGraph = g;
		this.entityMetadata = this.buildMetadata(ec);
		this.modifyMetadata();
		this.buildMetaFieldMap();
		//LOG.info("--> "+ this.entityClass.getSimpleName() );
		for( Map<String, Object> m : this.entityMetadata){
			//LOG.info("  - "+ m );
		}
		createClassAndProperties();
	}

	private void buildMetaFieldMap(){
		for( Map<String, Object> m : entityMetadata){
			this.metaByFieldMap.put( (String)m.get("name"), m);
		}
	}

	public List<Map<String, Object>> getMetadata(){
		return this.entityMetadata;
	}
	public void modifyMetadata() {
	}

	public void modifyParameterList(String statement, List<CParameter> parameterList) {
	}
	public void postProcessQuery(Query q, String statement, List<CParameter> parameterList) {
		if( statement.indexOf("Latest") > 0 && this.metaByFieldMap.get("version") != null){
			q.orderBy("version");
		}
	}

	public CParameter getParameter(List<CParameter> parameterList, String name) {
		for (CParameter p : parameterList){
			if( p.name.equals(name)){
				return p;
			}
		}
		return null;
	}

	public void checkParameterList(List<CParameter> parameterList) {
		for (CParameter p : parameterList){
			if( this.metaByFieldMap.get(p.name) == null){
				throw new RuntimeException("BaseEntityHandler.checkParameterList("+this.entityClass.getSimpleName()+","+p.name+") not found");
			}
			LOG.info("checkParameterMap("+p.name+") ok!");
		}
	}
	public List<CParameter> getParameterList(Object p) {
		throw new RuntimeException("Parameter for "+p.getClass()+ " not handled");
	}

	public OCommandRequest buildQuery( String entityName, String statement, List<CParameter> parameterList){
		modifyParameterList( statement, parameterList );
		checkParameterList( parameterList );

		List<Clause> clauseList = new ArrayList<Clause>();
		for (CParameter p : parameterList){
			Clause c = null;
			if( p.value == null){
				c = projection(p.name).isNull();
			} else if( p.value instanceof Date){
				c = clause(p.name, p.op, parameter(p.name));
			}else{
				c = clause(p.name, p.op, p.value);
			}
			clauseList.add( c );
		}
		Clause w = and( clauseList.toArray(new Clause[clauseList.size()])  );
		Query q = new Query().from(entityName).where(w);

		postProcessQuery( q, statement, parameterList );

		OCommandRequest query = new OSQLSynchQuery( q.toString() );
		OCommandContext qcontext = query.getContext();
		boolean hasVar = false;
		for (CParameter p : parameterList){
			if( p.value instanceof Date){
				qcontext.setVariable(p.name, p.value);
				hasVar = true;
			}
		}
		LOG.info("  - query:" + q);
		if( hasVar){
			LOG.info("  - query.params:" + qcontext);
		}
		return query;
	}


	private List<String> excludeList = new ArrayList<>(Arrays.asList("hashCode"));
	private List<Map<String, Object>> buildMetadata(Class clazz) {
		List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			Class returnType = m.getReturnType();
			String name = m.getName();
			String baseName = getBaseName(name);
			String setter = getSetter( clazz, baseName);
			String prefix = getGetterPrefix(name);
			if (setter!=null && !excludeList.contains(name) && !Modifier.isStatic(m.getModifiers()) && prefix != null && isPrimitiveOrPrimitiveWrapperOrString(returnType)) {
				if( prefix.equals("get") && (returnType == boolean.class || returnType == Boolean.class)){
					continue;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("type", returnType);
				map.put("name", baseName);
				map.put("prefix", prefix);
				map.put("getter", name);
				map.put("setter", setter);
				map.put("otype", OType.getTypeByClass(returnType));
				if( !containsGetter(name,fieldList)){
//					LOG.info("fieldList("+this.entityClass.getSimpleName()+").add:"+map);
					fieldList.add(map);
				}
			}
		}
		return fieldList;
	}
	protected <Any> Any getValue(Object obj, String methodName) {
		try{
			Method method = obj.getClass().getMethod(methodName, (Class[])null);
			return (Any) method.invoke(obj);
		}catch( Exception e){
			throw new RuntimeException("BaseEntityHandler.getValue:"+obj.getClass().getSimpleName()+"."+methodName);
		}
	}

	protected boolean hasMethod(Class c, String methodName) {
		try{
			Method method = c.getMethod(methodName, (Class[])null);
			return method != null;
		}catch( Exception e){
			return false;
		}
	}

	private String getSetter(Class clazz, String baseName) {
		Method[] methods = clazz.getMethods();
		String setter = "set"+firstToUpper(baseName);
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

	private String getBaseName( String methodName ){
		if( methodName.startsWith("get")){
			return firstToLower(methodName.substring(3));
		}
		if( methodName.startsWith("has")){
			return firstToLower(methodName.substring(3));
		}
		if( methodName.startsWith("is")){
			return firstToLower(methodName.substring(2));
		}
		return firstToLower(methodName);
	}

	private String[] getterPrefixes = new String[] { "is", "has", "get" };
	private String getGetterPrefix( String mName){
		for( String pre : getterPrefixes){
			if( mName.startsWith( pre)){
				return pre;
			}
		}
		return null;
	}

	private void createClassAndProperties() {
		try {
			String entityName = this.entityClass.getSimpleName();
			OSchemaProxy schema = this.orientGraph.getRawGraph().getMetadata().getSchema();
			if (schema.getClass(entityName) != null) {
				return;
			}
			executeUpdate(this.orientGraph, "CREATE CLASS " + entityName + " EXTENDS V, ORestricted");
			for (Map<String, Object> f : this.entityMetadata) {
				String pname = (String) f.get("name");
				String ptype = (String) ((OType) f.get("otype")).toString();
				String sql = "CREATE PROPERTY " + entityName + "." + pname + " " + ptype;
				LOG.info("executeUpdate:" + sql);
				executeUpdate(this.orientGraph, sql);
			}
			//m_orientdbService.executeUpdate(orientGraph, "CREATE INDEX History.key ON History ( key ) NOTUNIQUE");
		} catch (Exception e) {
			LOG.throwing("BaseEntityHandler", "createClassAndProperties", e);
			e.printStackTrace();
		}
	}

	protected void removeByGetter( String getter){
		for (Iterator<Map<String,Object>> iter = this.entityMetadata.listIterator(); iter.hasNext(); ) {
			Map<String,Object> m = iter.next();
			if ( getter.equals(m.get("getter"))) {
				iter.remove();
			}
		}
	}

	protected void setSetterByGetter( String getter, String setter){
		for (Iterator<Map<String,Object>> iter = this.entityMetadata.listIterator(); iter.hasNext(); ) {
			Map<String,Object> m = iter.next();
			if ( getter.equals(m.get("getter"))) {
				m.put("setter", setter);
			}
		}
	}
	protected boolean containsGetter( String getter, List<Map<String,Object>> list){
		for (Iterator<Map<String,Object>> iter = list.listIterator(); iter.hasNext(); ) {
			Map<String,Object> m = iter.next();
			if ( getter.equals(m.get("getter"))) {
				return true;
			}
		}
		return false;
	}

	private void executeUpdate(OrientGraph graph, String sql, Object... args) {
		OCommandRequest update = new OCommandSQL(sql);
		graph.command(update).execute(args);
	}

	protected boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
		return (type.isPrimitive() && type != void.class) || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class || type == java.util.Date.class || type == byte[].class;
	}

	protected String firstToLower( String s){
		char c[] = s.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}
	protected String firstToUpper( String s){
		char c[] = s.toCharArray();
		c[0] = Character.toUpperCase(c[0]);
		return new String(c);
	}

/*	private String[] prefixes = new String[] { "management", "task", "filter", "identity", "history", "runtime", "repository" };
	private String[] ops = new String[] { "In", "Like", "LessThanOrEqual", "LessThan", "GreaterThanOrEqual", "GreaterThan", "Equal", "NotEqual" };

	private boolean hasQuery(String name) {
		for (String prefix : prefixes) {
			try {
				Class clazz = Class.forName("org.camunda.bpm.engine." + prefix + "." + name + "Query");
				Method[] meths = clazz.getDeclaredMethods();
				for (Method m : meths) {
					//				System.err.println("\tyyy.method("+name+"):"+m.getName());
				}
				return true;
			} catch (Exception e) {
				//System.err.println("yyy.Exception("+name+"):"+e.getMessage());
			}
		}
		return false;
	}


	protected Map getMetaData(Class clazz) {
		String name = clazz.getSimpleName();
		String ename = name.substring(0, name.length() - 6);
		List fields = getSimpleFields(clazz);
		//		boolean b = hasQuery( ename);
		return null;
	}*/

	private List<Map<String, Object>> getSimpleFields(Class clazz) {
		List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			if (!Modifier.isStatic(f.getModifiers()) && isPrimitiveOrPrimitiveWrapperOrString(f.getType())) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("type", f.getType());
				map.put("name", f.getName());
				map.put("otype", OType.getTypeByClass(f.getType()));
				fieldList.add(map);
			}
		}
		return fieldList;
	}
}

