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

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import org.camunda.bpm.engine.impl.db.orientdb.handler.*;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

/**
 * @author Manfred Sattler
 */
public class OrientdbSessionFactory implements SessionFactory {

	private OrientGraphFactory graphFactory;
	private static Map<Class, BaseEntityHandler> entityHandlerMap;

	public OrientdbSessionFactory(OrientGraphFactory f) {
		this.graphFactory = f;
		initHandler();
	}

	private void initHandler() {
		OrientGraph orientGraph = this.graphFactory.getTx();
		entityHandlerMap = new HashMap<Class, BaseEntityHandler>();
		entityHandlerMap.put(TaskEntity.class, new TaskEntityHandler(orientGraph));
		entityHandlerMap.put(ProcessDefinitionEntity.class, new ProcessDefinitionEntityHandler(orientGraph));
		entityHandlerMap.put(ExecutionEntity.class, new ExecutionEntityHandler(orientGraph));
		entityHandlerMap.put(PropertyEntity.class, new PropertyEntityHandler(orientGraph));
		entityHandlerMap.put(VariableInstanceEntity.class, new VariableInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(ResourceEntity.class, new ResourceEntityHandler(orientGraph));
		entityHandlerMap.put(ByteArrayEntity.class, new ByteArrayEntityHandler(orientGraph));
		entityHandlerMap.put(DeploymentEntity.class, new DeploymentEntityHandler(orientGraph));
		orientGraph.shutdown();
	}

	public static BaseEntityHandler getEntityHandler(Class entityClass) {
		return entityHandlerMap.get(entityClass);
	}

	public Class<?> getSessionType() {
		return OrientdbPersistenceSession.class;
	}

	public Session openSession() {
		return new OrientdbPersistenceSession(graphFactory.getTx(), true);
	}

}

