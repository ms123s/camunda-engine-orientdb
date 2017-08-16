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
import java.util.Set;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import org.camunda.bpm.engine.impl.db.orientdb.handler.*;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import org.camunda.bpm.engine.impl.history.event.*;
import org.camunda.bpm.engine.impl.batch.*;
import org.camunda.bpm.engine.impl.batch.history.*;
import org.camunda.bpm.engine.impl.dmn.entity.repository.*;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.*;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.*;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.camunda.bpm.engine.impl.db.DbEntity;

/**
 * @author Manfred Sattler
 */
public class OrientdbSessionFactory implements SessionFactory {

	private OrientGraphFactory graphFactory;
	private static Map<Class, BaseEntityHandler> entityHandlerMap;
	private static Map<String, Class> entityClassMap;

	public OrientdbSessionFactory(OrientGraphFactory f) {
		this.graphFactory = f;
		initHandler();
		initEntityClasses();
	}
	private void initEntityClasses() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.addClassLoader( TaskEntity.class.getClassLoader());
		cb.setScanners(new SubTypesScanner());
		cb.setUrls(ClasspathHelper.forPackage("org.camunda"));
		Reflections r = new Reflections( cb );
		Set<Class<? extends DbEntity>> classes = r.getSubTypesOf(DbEntity.class);

		entityClassMap = new HashMap<String, Class>();
		for( Class c : classes){
			entityClassMap.put( c.getSimpleName(), c );
		}
	}

	private void initHandler() {
		OrientGraph orientGraph = this.graphFactory.getTx();
		entityHandlerMap = new HashMap<Class, BaseEntityHandler>();
		entityHandlerMap.put(HistoricDecisionInputInstanceEntity.class, new HistoricDecisionInputInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricDetailEventEntity.class, new HistoricDetailEventEntityHandler(orientGraph));
		entityHandlerMap.put(EventSubscriptionEntity.class, new EventSubscriptionEntityHandler(orientGraph));
		entityHandlerMap.put(GroupEntity.class, new GroupEntityHandler(orientGraph));
		entityHandlerMap.put(AttachmentEntity.class, new AttachmentEntityHandler(orientGraph));
		entityHandlerMap.put(ExecutionEntity.class, new ExecutionEntityHandler(orientGraph));
		entityHandlerMap.put(AuthorizationEntity.class, new AuthorizationEntityHandler(orientGraph));
		entityHandlerMap.put(MeterLogEntity.class, new MeterLogEntityHandler(orientGraph));
		entityHandlerMap.put(MetricIntervalEntity.class, new MetricIntervalEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricDecisionInstanceEntity.class, new HistoricDecisionInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricVariableUpdateEventEntity.class, new HistoricVariableUpdateEventEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricExternalTaskLogEntity.class, new HistoricExternalTaskLogEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricCaseActivityInstanceEventEntity.class, new HistoricCaseActivityInstanceEventEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricTaskInstanceEntity.class, new HistoricTaskInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(UserOperationLogEntryEventEntity.class, new UserOperationLogEntryEventEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricActivityInstanceEntity.class, new HistoricActivityInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricBatchEntity.class, new HistoricBatchEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricFormPropertyEventEntity.class, new HistoricFormPropertyEventEntityHandler(orientGraph));
		entityHandlerMap.put(ExternalTaskEntity.class, new ExternalTaskEntityHandler(orientGraph));
		entityHandlerMap.put(TaskEntity.class, new TaskEntityHandler(orientGraph));
		entityHandlerMap.put(ProcessDefinitionStatisticsEntity.class, new ProcessDefinitionStatisticsEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricCaseInstanceEntity.class, new HistoricCaseInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricDetailVariableInstanceUpdateEntity.class, new HistoricDetailVariableInstanceUpdateEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricIncidentEventEntity.class, new HistoricIncidentEventEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricJobLogEvent.class, new HistoricJobLogEventHandler(orientGraph));
		entityHandlerMap.put(DecisionDefinitionEntity.class, new DecisionDefinitionEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricFormPropertyEntity.class, new HistoricFormPropertyEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricCaseInstanceEventEntity.class, new HistoricCaseInstanceEventEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricVariableInstanceEntity.class, new HistoricVariableInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(VariableInstanceEntity.class, new VariableInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricTaskInstanceEventEntity.class, new HistoricTaskInstanceEventEntityHandler(orientGraph));
		entityHandlerMap.put(IncidentEntity.class, new IncidentEntityHandler(orientGraph));
		entityHandlerMap.put(BatchStatisticsEntity.class, new BatchStatisticsEntityHandler(orientGraph));
		entityHandlerMap.put(MembershipEntity.class, new MembershipEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricScopeInstanceEvent.class, new HistoricScopeInstanceEventHandler(orientGraph));
		entityHandlerMap.put(BatchEntity.class, new BatchEntityHandler(orientGraph));
		entityHandlerMap.put(CommentEntity.class, new CommentEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricDecisionOutputInstanceEntity.class, new HistoricDecisionOutputInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(DeploymentStatisticsEntity.class, new DeploymentStatisticsEntityHandler(orientGraph));
		entityHandlerMap.put(PropertyEntity.class, new PropertyEntityHandler(orientGraph));
		entityHandlerMap.put(ResourceEntity.class, new ResourceEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricDecisionEvaluationEvent.class, new HistoricDecisionEvaluationEventHandler(orientGraph));
		entityHandlerMap.put(ByteArrayEntity.class, new ByteArrayEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricProcessInstanceEntity.class, new HistoricProcessInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(EverLivingJobEntity.class, new EverLivingJobEntityHandler(orientGraph));
		entityHandlerMap.put(IdentityInfoEntity.class, new IdentityInfoEntityHandler(orientGraph));
		entityHandlerMap.put(DeploymentEntity.class, new DeploymentEntityHandler(orientGraph));
		entityHandlerMap.put(JobEntity.class, new JobEntityHandler(orientGraph));
		entityHandlerMap.put(IdentityLinkEntity.class, new IdentityLinkEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricActivityInstanceEventEntity.class, new HistoricActivityInstanceEventEntityHandler(orientGraph));
		entityHandlerMap.put(UserEntity.class, new UserEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricCaseActivityInstanceEntity.class, new HistoricCaseActivityInstanceEntityHandler(orientGraph));
		entityHandlerMap.put(ProcessDefinitionEntity.class, new ProcessDefinitionEntityHandler(orientGraph));
		entityHandlerMap.put(CaseDefinitionEntity.class, new CaseDefinitionEntityHandler(orientGraph));
		entityHandlerMap.put(TenantEntity.class, new TenantEntityHandler(orientGraph));
		entityHandlerMap.put(TenantMembershipEntity.class, new TenantMembershipEntityHandler(orientGraph));
		entityHandlerMap.put(CaseSentryPartEntity.class, new CaseSentryPartEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricJobLogEventEntity.class, new HistoricJobLogEventEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricProcessInstanceEventEntity.class, new HistoricProcessInstanceEventEntityHandler(orientGraph));
		entityHandlerMap.put(CaseExecutionEntity.class, new CaseExecutionEntityHandler(orientGraph));
		entityHandlerMap.put(HistoryEvent.class, new HistoryEventHandler(orientGraph));
		entityHandlerMap.put(HistoricIdentityLinkLogEntity.class, new HistoricIdentityLinkLogEntityHandler(orientGraph));
		entityHandlerMap.put(MessageEntity.class, new MessageEntityHandler(orientGraph));
		entityHandlerMap.put(JobDefinitionEntity.class, new JobDefinitionEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricIdentityLinkLogEventEntity.class, new HistoricIdentityLinkLogEventEntityHandler(orientGraph));
		entityHandlerMap.put(HistoricIncidentEntity.class, new HistoricIncidentEntityHandler(orientGraph));
		entityHandlerMap.put(FilterEntity.class, new FilterEntityHandler(orientGraph));
		entityHandlerMap.put(DecisionRequirementsDefinitionEntity.class, new DecisionRequirementsDefinitionEntityHandler(orientGraph));
		entityHandlerMap.put(TimerEntity.class, new TimerEntityHandler(orientGraph));
		orientGraph.shutdown();
	}

	public static BaseEntityHandler getEntityHandler(Class entityClass) {
		return entityHandlerMap.get(entityClass);
	}

	public static Class getEntityClass(String  entityName) {
		return entityClassMap.get(entityName);
	}

	public Class<?> getSessionType() {
		return OrientdbPersistenceSession.class;
	}

	public Session openSession() {
		return new OrientdbPersistenceSession(graphFactory.getTx(), true);
	}

}

