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
import java.util.List;
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
import org.camunda.bpm.engine.impl.cfg.orientdb.VariableListener;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.camunda.bpm.engine.impl.db.DbEntity;
import java.util.logging.Logger;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Manfred Sattler
 */
public class OrientdbSessionFactory implements SessionFactory {
	private final static Logger LOG = Logger.getLogger(OrientdbSessionFactory.class.getName());

	private ODatabasePool databasePool;
	private static Map<Class, BaseEntityHandler> entityHandlerMap;
	private static Map<String, Class> entityClassMap;
	private static Map<Class, Class> entityReplaceMap;
	private List<VariableListener> variableListeners;

	public OrientdbSessionFactory(ODatabasePool f, List<VariableListener> vl, String history) {
		this.databasePool = f;
		this.variableListeners = vl;
		ODatabaseSession databaseSession = null;
		try{
			databaseSession = this.databasePool.acquire();

			debug("OrientGraphFactory:"+this.variableListeners);

			initHandler(databaseSession);
			initEntityClasses();
		}catch(Exception e){
			LOG.info("OrientGraphFactory():init:"+e);
			throw new RuntimeException("OrientGraphFactory.init", e);
		}finally{
			databaseSession.close();
		}
		
	}

	private void initEntityClasses() {
		entityClassMap = new HashMap<String, Class>();
		for (Class c : entityHandlerMap.keySet()) {
			entityClassMap.put(c.getSimpleName(), c);
		}
		initEntityReplace();
	}

	private void initEntityReplace() {
		entityReplaceMap = new HashMap<Class, Class>();
		entityReplaceMap.put(HistoricProcessInstanceEventEntity.class, HistoricProcessInstanceEntity.class);
		entityReplaceMap.put(HistoricActivityInstanceEventEntity.class, HistoricActivityInstanceEntity.class);
		entityReplaceMap.put(HistoricTaskInstanceEventEntity.class, HistoricTaskInstanceEntity.class);
		entityReplaceMap.put(HistoricFormPropertyEventEntityHandler.class, HistoricFormPropertyEntityHandler.class); //@@@MS Handler???
		entityReplaceMap.put(HistoricIdentityLinkLogEventEntityHandler.class, HistoricIdentityLinkLogEntityHandler.class); //@@@MS Handler???
		entityReplaceMap.put(HistoricIncidentEventEntityHandler.class, HistoricIncidentEntityHandler.class); //@@@MS Handler???
		entityReplaceMap.put(HistoricJobLogEventEntityHandler.class, HistoricJobLogEventHandler.class); //@@@MS Handler???
	}

	private void initHandler(ODatabaseSession databaseSession) {
		entityHandlerMap = new HashMap<Class, BaseEntityHandler>();
		entityHandlerMap.put(HistoricDecisionInputInstanceEntity.class, new HistoricDecisionInputInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricDetailEventEntity.class, new HistoricDetailEventEntityHandler(databaseSession));
		entityHandlerMap.put(EventSubscriptionEntity.class, new EventSubscriptionEntityHandler(databaseSession));
		entityHandlerMap.put(ExecutionEntity.class, new ExecutionEntityHandler(databaseSession));
		entityHandlerMap.put(GroupEntity.class, new GroupEntityHandler(databaseSession));
		entityHandlerMap.put(AttachmentEntity.class, new AttachmentEntityHandler(databaseSession));
		entityHandlerMap.put(AuthorizationEntity.class, new AuthorizationEntityHandler(databaseSession));
		entityHandlerMap.put(MeterLogEntity.class, new MeterLogEntityHandler(databaseSession));
		entityHandlerMap.put(MetricIntervalEntity.class, new MetricIntervalEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricDecisionInstanceEntity.class, new HistoricDecisionInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricVariableUpdateEventEntity.class, new HistoricVariableUpdateEventEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricExternalTaskLogEntity.class, new HistoricExternalTaskLogEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricCaseActivityInstanceEventEntity.class, new HistoricCaseActivityInstanceEventEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricTaskInstanceEntity.class, new HistoricTaskInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(UserOperationLogEntryEventEntity.class, new UserOperationLogEntryEventEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricActivityInstanceEntity.class, new HistoricActivityInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricBatchEntity.class, new HistoricBatchEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricFormPropertyEventEntity.class, new HistoricFormPropertyEventEntityHandler(databaseSession));
		entityHandlerMap.put(ExternalTaskEntity.class, new ExternalTaskEntityHandler(databaseSession));
		entityHandlerMap.put(ProcessDefinitionStatisticsEntity.class, new ProcessDefinitionStatisticsEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricCaseInstanceEntity.class, new HistoricCaseInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricDetailVariableInstanceUpdateEntity.class, new HistoricDetailVariableInstanceUpdateEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricIncidentEventEntity.class, new HistoricIncidentEventEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricJobLogEvent.class, new HistoricJobLogEventHandler(databaseSession));
		entityHandlerMap.put(DecisionDefinitionEntity.class, new DecisionDefinitionEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricFormPropertyEntity.class, new HistoricFormPropertyEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricCaseInstanceEventEntity.class, new HistoricCaseInstanceEventEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricVariableInstanceEntity.class, new HistoricVariableInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(VariableInstanceEntity.class, new VariableInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricTaskInstanceEventEntity.class, new HistoricTaskInstanceEventEntityHandler(databaseSession));
		entityHandlerMap.put(IncidentEntity.class, new IncidentEntityHandler(databaseSession));
		entityHandlerMap.put(BatchStatisticsEntity.class, new BatchStatisticsEntityHandler(databaseSession));
		entityHandlerMap.put(MembershipEntity.class, new MembershipEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricScopeInstanceEvent.class, new HistoricScopeInstanceEventHandler(databaseSession));
		entityHandlerMap.put(BatchEntity.class, new BatchEntityHandler(databaseSession));
		entityHandlerMap.put(CommentEntity.class, new CommentEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricDecisionOutputInstanceEntity.class, new HistoricDecisionOutputInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(DeploymentStatisticsEntity.class, new DeploymentStatisticsEntityHandler(databaseSession));
		entityHandlerMap.put(PropertyEntity.class, new PropertyEntityHandler(databaseSession));
		entityHandlerMap.put(ResourceEntity.class, new ResourceEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricDecisionEvaluationEvent.class, new HistoricDecisionEvaluationEventHandler(databaseSession));
		entityHandlerMap.put(ByteArrayEntity.class, new ByteArrayEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricProcessInstanceEntity.class, new HistoricProcessInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(EverLivingJobEntity.class, new EverLivingJobEntityHandler(databaseSession));
		entityHandlerMap.put(IdentityInfoEntity.class, new IdentityInfoEntityHandler(databaseSession));
		entityHandlerMap.put(DeploymentEntity.class, new DeploymentEntityHandler(databaseSession));
		entityHandlerMap.put(JobEntity.class, new JobEntityHandler(databaseSession));
		entityHandlerMap.put(IdentityLinkEntity.class, new IdentityLinkEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricActivityInstanceEventEntity.class, new HistoricActivityInstanceEventEntityHandler(databaseSession));
		entityHandlerMap.put(UserEntity.class, new UserEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricCaseActivityInstanceEntity.class, new HistoricCaseActivityInstanceEntityHandler(databaseSession));
		entityHandlerMap.put(TaskEntity.class, new TaskEntityHandler(databaseSession));
		entityHandlerMap.put(ProcessDefinitionEntity.class, new ProcessDefinitionEntityHandler(databaseSession));
		entityHandlerMap.put(CaseDefinitionEntity.class, new CaseDefinitionEntityHandler(databaseSession));
		entityHandlerMap.put(TenantEntity.class, new TenantEntityHandler(databaseSession));
		entityHandlerMap.put(TenantMembershipEntity.class, new TenantMembershipEntityHandler(databaseSession));
		entityHandlerMap.put(CaseSentryPartEntity.class, new CaseSentryPartEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricJobLogEventEntity.class, new HistoricJobLogEventEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricProcessInstanceEventEntity.class, new HistoricProcessInstanceEventEntityHandler(databaseSession));
		entityHandlerMap.put(CaseExecutionEntity.class, new CaseExecutionEntityHandler(databaseSession));
		entityHandlerMap.put(HistoryEvent.class, new HistoryEventHandler(databaseSession));
		entityHandlerMap.put(HistoricIdentityLinkLogEntity.class, new HistoricIdentityLinkLogEntityHandler(databaseSession));
		entityHandlerMap.put(MessageEntity.class, new MessageEntityHandler(databaseSession));
		entityHandlerMap.put(JobDefinitionEntity.class, new JobDefinitionEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricIdentityLinkLogEventEntity.class, new HistoricIdentityLinkLogEventEntityHandler(databaseSession));
		entityHandlerMap.put(HistoricIncidentEntity.class, new HistoricIncidentEntityHandler(databaseSession));
		entityHandlerMap.put(FilterEntity.class, new FilterEntityHandler(databaseSession));
		entityHandlerMap.put(DecisionRequirementsDefinitionEntity.class, new DecisionRequirementsDefinitionEntityHandler(databaseSession));
		entityHandlerMap.put(TimerEntity.class, new TimerEntityHandler(databaseSession));
	}

	public static BaseEntityHandler getEntityHandler(Class entityClass, ODatabaseSession databaseSession) {
		BaseEntityHandler bh = entityHandlerMap.get(entityClass);
		if( bh == null){
			throw new RuntimeException("OrientdbSessionFactory.getEntityHandler:entityClass("+entityClass+"):not found");
		}
		return bh;
	}

	public static Class getEntityClass(String entityName) {
		return entityClassMap.get(entityName);
	}

	public static Class getReplaceClass(Class entity) {
		Class ret = entityReplaceMap.get(entity);
		return ret == null ? entity : ret;
	}

	public Class<?> getSessionType() {
		return OrientdbPersistenceSession.class;
	}

	public Session openSession() {
		return new OrientdbPersistenceSession(databasePool.acquire(), this);
	}

	private static void dump(String msg, Object o) {
		ReflectionToStringBuilder rb = new ReflectionToStringBuilder(o, ToStringStyle.JSON_STYLE);
		rb.setExcludeNullValues(true);
		//debug("+++" + msg + ":\n" + rb.toString());
	}

	@SuppressWarnings({ "unchecked" })
	public void fireEvent(final HistoricVariableUpdateEventEntity hv) {
		Map<String,Object> properties = new HashMap<String,Object>();
		ObjectValueCopy.copyProperties( hv, properties);
		debug("variable.fireEvent:"+ properties);
		if (variableListeners != null) {
			for (VariableListener variableListener : variableListeners) {
				try {
					variableListener.notify(properties);
				} catch (Exception e) {
					LOG.info("OrientdbSessionFactory.fireEvent:" + e);
				}
			}
		}
	}
	private void debug(String msg){
		//LOG.info(msg);
	}
}

