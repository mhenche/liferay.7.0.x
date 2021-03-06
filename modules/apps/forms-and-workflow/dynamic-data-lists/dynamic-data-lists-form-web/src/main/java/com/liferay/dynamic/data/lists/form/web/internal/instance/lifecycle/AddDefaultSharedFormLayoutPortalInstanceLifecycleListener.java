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

package com.liferay.dynamic.data.lists.form.web.internal.instance.lifecycle;

import com.liferay.dynamic.data.lists.form.web.internal.layout.type.constants.DDLFormPortletLayoutTypeConstants;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Leonardo Barros
 */
@Component(
	immediate = true,
	service = {
		AddDefaultSharedFormLayoutPortalInstanceLifecycleListener.class,
		PortalInstanceLifecycleListener.class
	}
)
public class AddDefaultSharedFormLayoutPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	public String getFormLayoutURL(
		ThemeDisplay themeDisplay, boolean privateLayout) {

		StringBundler sb = new StringBundler(3);

		sb.append(themeDisplay.getPortalURL());

		Group group = themeDisplay.getSiteGroup();

		sb.append(group.getPathFriendlyURL(privateLayout, themeDisplay));

		sb.append("/forms/shared/-/form/");

		return sb.toString();
	}

	public boolean isSharedLayout(ThemeDisplay themeDisplay) {
		Layout layout = themeDisplay.getLayout();

		String type = layout.getType();

		return type.equals(DDLFormPortletLayoutTypeConstants.LAYOUT_TYPE);
	}

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		Group group = _groupLocalService.fetchFriendlyURLGroup(
			company.getCompanyId(), "/forms");

		if (group == null) {
			group = addFormsGroup(company.getCompanyId());
		}

		Layout layout = _layoutLocalService.fetchLayoutByFriendlyURL(
			group.getGroupId(), false, "/shared");

		if (layout == null) {
			layout = addSharedLayout(
				company.getCompanyId(), group.getGroupId());
		}

		verifyLayout(layout);
	}

	protected Group addFormsGroup(long companyId) throws PortalException {
		long defaultUserId = _userLocalService.getDefaultUserId(companyId);

		Map<Locale, String> nameMap = new HashMap<>();

		nameMap.put(LocaleUtil.getDefault(), GroupConstants.FORMS);

		return _groupLocalService.addGroup(
			defaultUserId, GroupConstants.DEFAULT_PARENT_GROUP_ID, null, 0,
			GroupConstants.DEFAULT_LIVE_GROUP_ID, nameMap, null,
			GroupConstants.TYPE_SITE_PRIVATE, true,
			GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION,
			GroupConstants.FORMS_FRIENDLY_URL, false, false, true, null);
	}

	protected Layout addSharedLayout(long companyId, long groupId)
		throws PortalException {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGuestPermissions(true);
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAttribute(
			"layout.instanceable.allowed", Boolean.TRUE);
		serviceContext.setAttribute("layoutUpdateable", Boolean.FALSE);

		serviceContext.setScopeGroupId(groupId);

		long defaultUserId = _userLocalService.getDefaultUserId(companyId);

		serviceContext.setUserId(defaultUserId);

		return _layoutLocalService.addLayout(
			defaultUserId, groupId, false,
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, "Shared",
			StringPool.BLANK, StringPool.BLANK,
			DDLFormPortletLayoutTypeConstants.LAYOUT_TYPE, true, "/shared",
			serviceContext);
	}

	@Reference(unbind = "-")
	protected void setGroupLocalService(GroupLocalService groupLocalService) {
		_groupLocalService = groupLocalService;
	}

	@Reference(unbind = "-")
	protected void setLayoutLocalService(
		LayoutLocalService layoutLocalService) {

		_layoutLocalService = layoutLocalService;
	}

	@Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED, unbind = "-")
	protected void setModuleServiceLifecycle(
		ModuleServiceLifecycle moduleServiceLifecycle) {
	}

	@Reference(unbind = "-")
	protected void setUserLocalService(UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	protected void verifyLayout(Layout layout) {
		if (StringUtil.equals(
				layout.getType(),
				DDLFormPortletLayoutTypeConstants.LAYOUT_TYPE)) {

			return;
		}

		layout.setType(DDLFormPortletLayoutTypeConstants.LAYOUT_TYPE);

		_layoutLocalService.updateLayout(layout);
	}

	private GroupLocalService _groupLocalService;
	private LayoutLocalService _layoutLocalService;
	private UserLocalService _userLocalService;

}