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
import java.util.List;
import java.util.Map;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.OCommandSQL;

/**
 * @author Manfred Sattler
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class BaseEntityHandler {

	private final static Logger LOG = Logger.getLogger(BaseEntityHandler.class.getName());
	protected OrientGraph orientGraph;
	protected Class entityClass;
	private	List<Map<String,Object>> entityFieldList = new ArrayList<Map<String,Object>>();

	public BaseEntityHandler(OrientGraph g, Class ec) {
		this.entityClass = ec;
		this.orientGraph = g;
		this.entityFieldList = this.getSimpleFields(ec);
		this.modifyFieldList( this.entityFieldList );
		LOG.info( this.entityClass.getSimpleName() + ":"+ this.entityFieldList);
		createClassAndProperties();
	}

	public void modifyFieldList( List<Map<String,Object>> fl){
	}

	private List<Map<String,Object>> getSimpleFields(Class clazz){
		List<Map<String,Object>> fieldList = new ArrayList<Map<String,Object>>();
		try{
			Field[] fields = clazz.getDeclaredFields();
			for( Field f : fields){
				if( !Modifier.isStatic(f.getModifiers()) && isPrimitiveOrPrimitiveWrapperOrString(f.getType())){
					System.err.println("\tyyy.field("+f.getType().getSimpleName()+"):"+f.getName());
					Map<String,Object> map = new HashMap<String,Object>();
					map.put( "type", f.getType());
					map.put( "name", f.getName());
					map.put( "otype", OType.getTypeByClass(f.getType()));
					fieldList.add( map);
				}
			}
			return fieldList;
		}catch(Exception e){
			//System.err.println("yyy.Exception("+name+"):"+e.getMessage());
		}
		return null;
	}

	private void createClassAndProperties(){
		try{
			String entityName = this.entityClass.getSimpleName();
			OSchemaProxy schema = this.orientGraph.getRawGraph().getMetadata().getSchema();
			if( schema.getClass(entityName) != null){
				return;
			}
			executeUpdate(this.orientGraph, "CREATE CLASS "+entityName+" EXTENDS V");
			for( Map<String,Object> f : this.entityFieldList){
				String pname = (String)f.get("name");
				String ptype = (String)((OType)f.get("otype")).toString();
				LOG.info("executeUpdate:"+("CREATE PROPERTY "+entityName+"."+pname+" "+ptype));
				executeUpdate(this.orientGraph, "CREATE PROPERTY "+entityName+"."+pname+" "+ptype);
			}
			//m_orientdbService.executeUpdate(orientGraph, "CREATE INDEX History.key ON History ( key ) NOTUNIQUE");
		}catch( Exception e){
			LOG.throwing("BaseEntityHandler", "createClassAndProperties",e);
			e.printStackTrace();
		}
	}
	private void executeUpdate(OrientGraph graph, String sql, Object... args) {
		OCommandRequest update = new OCommandSQL(sql);
		graph.command(update).execute(args);
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

	public static boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
		return (type.isPrimitive() && type != void.class) ||
			type == Double.class || type == Float.class || type == Long.class ||
			type == Integer.class || type == Short.class || type == Character.class ||
			type == Byte.class || type == Boolean.class || type == String.class;
	}

	protected Map getMetaData( Class clazz){
		String name = clazz.getSimpleName();	
		String ename = name.substring(0, name.length()-6);
		List fields = getSimpleFields( clazz);
		//		boolean b = hasQuery( ename);
		return null;
	}
}
