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

package com.liferay.portlet.blogs.service.permission;

import com.liferay.blogs.kernel.model.BlogsEntry;
import com.liferay.blogs.kernel.service.BlogsEntryLocalServiceUtil;
import com.liferay.exportimport.kernel.staging.permission.StagingPermissionUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.BaseModelPermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.spring.osgi.OSGiBeanProperties;
import com.liferay.portal.kernel.util.HashUtil;
import com.liferay.portal.kernel.workflow.permission.WorkflowPermissionUtil;

import java.util.Map;
import java.util.Objects;

/**
 * @author Brian Wing Shun Chan
 */
@OSGiBeanProperties(
	property = {"model.class.name=com.liferay.blogs.kernel.model.BlogsEntry"}
)
public class BlogsEntryPermission implements BaseModelPermissionChecker {

	public static void check(
			PermissionChecker permissionChecker, BlogsEntry entry,
			String actionId)
		throws PortalException {

		if (!contains(permissionChecker, entry, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, BlogsEntry.class.getName(),
				entry.getEntryId(), actionId);
		}
	}

	public static void check(
			PermissionChecker permissionChecker, long entryId, String actionId)
		throws PortalException {

		if (!contains(permissionChecker, entryId, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, BlogsEntry.class.getName(), entryId,
				actionId);
		}
	}

	public static boolean contains(
		PermissionChecker permissionChecker, BlogsEntry entry,
		String actionId) {

		Map<Object, Object> permissionChecksMap =
			permissionChecker.getPermissionChecksMap();

		PermissionCacheKey permissionCacheKey = new PermissionCacheKey(
			entry.getEntryId(), actionId);

		Boolean contains = (Boolean)permissionChecksMap.get(permissionCacheKey);

		if (contains == null) {
			contains = _contains(permissionChecker, entry, actionId);

			permissionChecksMap.put(permissionCacheKey, contains);
		}

		return contains;
	}

	public static boolean contains(
			PermissionChecker permissionChecker, long entryId, String actionId)
		throws PortalException {

		Map<Object, Object> permissionChecksMap =
			permissionChecker.getPermissionChecksMap();

		PermissionCacheKey permissionCacheKey = new PermissionCacheKey(
			entryId, actionId);

		Boolean contains = (Boolean)permissionChecksMap.get(permissionCacheKey);

		if (contains == null) {
			BlogsEntryCacheKey blogsEntryCacheKey = new BlogsEntryCacheKey(
				entryId);

			BlogsEntry entry = (BlogsEntry)permissionChecksMap.get(
				blogsEntryCacheKey);

			if (entry == null) {
				entry = BlogsEntryLocalServiceUtil.getEntry(entryId);

				permissionChecksMap.put(blogsEntryCacheKey, entry);
			}

			contains = _contains(permissionChecker, entry, actionId);

			permissionChecksMap.put(permissionCacheKey, contains);
		}

		return contains;
	}

	@Override
	public void checkBaseModel(
			PermissionChecker permissionChecker, long groupId, long primaryKey,
			String actionId)
		throws PortalException {

		check(permissionChecker, primaryKey, actionId);
	}

	private static boolean _contains(
		PermissionChecker permissionChecker, BlogsEntry entry,
		String actionId) {

		String portletId = PortletProviderUtil.getPortletId(
			BlogsEntry.class.getName(), PortletProvider.Action.EDIT);

		Boolean hasPermission = StagingPermissionUtil.hasPermission(
			permissionChecker, entry.getGroupId(), BlogsEntry.class.getName(),
			entry.getEntryId(), portletId, actionId);

		if (hasPermission != null) {
			return hasPermission.booleanValue();
		}

		if (entry.isDraft() || entry.isScheduled()) {
			if (actionId.equals(ActionKeys.VIEW) &&
				!contains(permissionChecker, entry, ActionKeys.UPDATE)) {

				return false;
			}
		}
		else if (entry.isPending()) {
			hasPermission = WorkflowPermissionUtil.hasPermission(
				permissionChecker, entry.getGroupId(),
				BlogsEntry.class.getName(), entry.getEntryId(), actionId);

			if (hasPermission != null) {
				return hasPermission.booleanValue();
			}
		}

		if (permissionChecker.hasOwnerPermission(
				entry.getCompanyId(), BlogsEntry.class.getName(),
				entry.getEntryId(), entry.getUserId(), actionId)) {

			return true;
		}

		return permissionChecker.hasPermission(
			entry.getGroupId(), BlogsEntry.class.getName(), entry.getEntryId(),
			actionId);
	}

	private static class BlogsEntryCacheKey {

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof BlogsEntryCacheKey)) {
				return false;
			}

			BlogsEntryCacheKey blogsEntryCacheKey = (BlogsEntryCacheKey)obj;

			if (_entryId == blogsEntryCacheKey._entryId) {
				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return (int)_entryId;
		}

		private BlogsEntryCacheKey(long entryId) {
			_entryId = entryId;
		}

		private final long _entryId;

	}

	private static class PermissionCacheKey {

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof PermissionCacheKey)) {
				return false;
			}

			PermissionCacheKey permissionCacheKey = (PermissionCacheKey)obj;

			if ((_entryId == permissionCacheKey._entryId) &&
				Objects.equals(_actionId, permissionCacheKey._actionId)) {

				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			int hash = HashUtil.hash(0, _entryId);

			return HashUtil.hash(hash, _actionId);
		}

		private PermissionCacheKey(long entryId, String actionId) {
			_entryId = entryId;
			_actionId = actionId;
		}

		private final String _actionId;
		private final long _entryId;

	}

}