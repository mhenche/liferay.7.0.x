/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.kaleo.runtime.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.transaction.Isolation;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.spring.extender.service.ServiceReference;
import com.liferay.portal.workflow.kaleo.KaleoWorkflowModelConverter;
import com.liferay.portal.workflow.kaleo.definition.ExecutionType;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.KaleoTask;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.TaskManager;
import com.liferay.portal.workflow.kaleo.runtime.action.KaleoActionExecutor;
import com.liferay.portal.workflow.kaleo.runtime.notification.NotificationHelper;
import com.liferay.portal.workflow.kaleo.runtime.util.WorkflowContextUtil;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Michael C. Han
 */
@Transactional(
	isolation = Isolation.PORTAL, propagation = Propagation.REQUIRED,
	rollbackFor = {Exception.class}
)
public class DefaultTaskManagerImpl
	extends BaseKaleoBean implements TaskManager {

	@Override
	public WorkflowTask assignWorkflowTaskToRole(
			long workflowTaskInstanceId, long roleId, String comment,
			Date dueDate, Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws WorkflowException {

		try {
			return assignWorkflowTask(
				workflowTaskInstanceId, Role.class.getName(), roleId, comment,
				dueDate, workflowContext, serviceContext);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	@Override
	public WorkflowTask assignWorkflowTaskToUser(
			long workflowTaskInstanceId, long assigneeUserId, String comment,
			Date dueDate, Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws WorkflowException {

		try {
			return assignWorkflowTask(
				workflowTaskInstanceId, User.class.getName(), assigneeUserId,
				comment, dueDate, workflowContext, serviceContext);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	@Override
	public WorkflowTask completeWorkflowTask(
			long workflowTaskInstanceId, String transitionName, String comment,
			Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws WorkflowException {

		try {
			return doCompleteWorkflowTask(
				workflowTaskInstanceId, transitionName, comment,
				workflowContext, serviceContext);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	@Override
	public WorkflowTask updateDueDate(
			long workflowTaskInstanceId, String comment, Date dueDate,
			ServiceContext serviceContext)
		throws WorkflowException {

		try {
			KaleoTaskInstanceToken kaleoTaskInstanceToken =
				kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceToken(
					workflowTaskInstanceId);

			if (kaleoTaskInstanceToken.isCompleted()) {
				throw new WorkflowException(
					"Cannot update due date for completed task " +
						workflowTaskInstanceId);
			}

			if (dueDate != null) {
				kaleoTaskInstanceTokenLocalService.updateDueDate(
					workflowTaskInstanceId, dueDate, serviceContext);
			}

			Map<String, Serializable> workflowContext =
				WorkflowContextUtil.convert(
					kaleoTaskInstanceToken.getWorkflowContext());

			kaleoLogLocalService.addTaskUpdateKaleoLog(
				kaleoTaskInstanceToken, comment, workflowContext,
				serviceContext);

			return _kaleoWorkflowModelConverter.toWorkflowTask(
				kaleoTaskInstanceToken, workflowContext);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	protected WorkflowTask assignWorkflowTask(
			long workflowTaskInstanceId, String assigneeClassName,
			long assigneeClassPK, String comment, Date dueDate,
			Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws Exception {

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceToken(
				workflowTaskInstanceId);

		List<KaleoTaskAssignmentInstance> previousTaskAssignmentInstances =
			kaleoTaskInstanceToken.getKaleoTaskAssignmentInstances();

		workflowContext = updateWorkflowContext(
			workflowContext, kaleoTaskInstanceToken);

		if (kaleoTaskInstanceToken.isCompleted()) {
			throw new WorkflowException(
				"Cannot reassign a completed task " + workflowTaskInstanceId);
		}

		if (dueDate != null) {
			kaleoTaskInstanceTokenLocalService.updateDueDate(
				workflowTaskInstanceId, dueDate, serviceContext);
		}

		kaleoTaskInstanceToken =
			kaleoTaskInstanceTokenLocalService.assignKaleoTaskInstanceToken(
				kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(),
				assigneeClassName, assigneeClassPK, workflowContext,
				serviceContext);

		workflowContext.put(WorkflowConstants.CONTEXT_TASK_COMMENTS, comment);

		ExecutionContext executionContext = new ExecutionContext(
			kaleoTaskInstanceToken.getKaleoInstanceToken(),
			kaleoTaskInstanceToken, workflowContext, serviceContext);

		KaleoTask kaleoTask = kaleoTaskInstanceToken.getKaleoTask();

		_kaleoActionExecutor.executeKaleoActions(
			KaleoNode.class.getName(), kaleoTask.getKaleoNodeId(),
			ExecutionType.ON_ASSIGNMENT, executionContext);

		boolean selfAssignment = false;

		if (assigneeClassName.equals(User.class.getName()) &&
			(assigneeClassPK == serviceContext.getUserId())) {

			selfAssignment = true;
		}

		if (!selfAssignment) {
			_notificationHelper.sendKaleoNotifications(
				KaleoNode.class.getName(), kaleoTask.getKaleoNodeId(),
				ExecutionType.ON_ASSIGNMENT, executionContext);
		}

		kaleoLogLocalService.addTaskAssignmentKaleoLog(
			previousTaskAssignmentInstances, kaleoTaskInstanceToken, comment,
			workflowContext, serviceContext);

		return _kaleoWorkflowModelConverter.toWorkflowTask(
			kaleoTaskInstanceToken, workflowContext);
	}

	protected WorkflowTask doCompleteWorkflowTask(
			long workflowTaskInstanceId, String transitionName, String comment,
			Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws Exception {

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceToken(
				workflowTaskInstanceId);

		if (Validator.isNotNull(transitionName)) {

			// Validate that the transition actually exists before moving
			// forward

			KaleoTask kaleoTask = kaleoTaskInstanceToken.getKaleoTask();

			KaleoNode currentKaleoNode = kaleoTask.getKaleoNode();

			currentKaleoNode.getKaleoTransition(transitionName);
		}

		workflowContext = updateWorkflowContext(
			workflowContext, kaleoTaskInstanceToken);

		if (kaleoTaskInstanceToken.isCompleted()) {
			throw new WorkflowException(
				StringBundler.concat(
					"Cannot complete an already completed task ",
					String.valueOf(workflowTaskInstanceId), " for user ",
					String.valueOf(serviceContext.getUserId())));
		}

		serviceContext.setScopeGroupId(kaleoTaskInstanceToken.getGroupId());

		kaleoTaskInstanceToken =
			kaleoTaskInstanceTokenLocalService.completeKaleoTaskInstanceToken(
				kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(),
				serviceContext);

		kaleoLogLocalService.addTaskCompletionKaleoLog(
			kaleoTaskInstanceToken, comment, workflowContext, serviceContext);

		return _kaleoWorkflowModelConverter.toWorkflowTask(
			kaleoTaskInstanceToken, workflowContext);
	}

	protected Map<String, Serializable> updateWorkflowContext(
			Map<String, Serializable> workflowContext,
			KaleoTaskInstanceToken kaleoTaskInstanceToken)
		throws PortalException {

		KaleoInstance kaleoInstance =
			kaleoInstanceLocalService.getKaleoInstance(
				kaleoTaskInstanceToken.getKaleoInstanceId());

		if (workflowContext == null) {
			workflowContext = WorkflowContextUtil.convert(
				kaleoInstance.getWorkflowContext());
		}
		else {
			Map<String, Serializable> storedWorkflowContext =
				WorkflowContextUtil.convert(kaleoInstance.getWorkflowContext());

			for (Map.Entry<String, Serializable> entry :
					storedWorkflowContext.entrySet()) {

				String key = entry.getKey();

				if (!workflowContext.containsKey(key)) {
					workflowContext.put(key, entry.getValue());
				}
			}
		}

		return workflowContext;
	}

	@ServiceReference(type = KaleoActionExecutor.class)
	private KaleoActionExecutor _kaleoActionExecutor;

	@ServiceReference(type = KaleoWorkflowModelConverter.class)
	private KaleoWorkflowModelConverter _kaleoWorkflowModelConverter;

	@ServiceReference(type = NotificationHelper.class)
	private NotificationHelper _notificationHelper;

}