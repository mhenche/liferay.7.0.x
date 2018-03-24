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

package com.liferay.shopping.upgrade;

import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.shopping.upgrade.v1_0_0.UpgradeCompanyId;
import com.liferay.shopping.upgrade.v1_0_0.UpgradeKernelPackage;
import com.liferay.shopping.upgrade.v1_0_0.UpgradePortletId;
import com.liferay.shopping.upgrade.v1_0_0.UpgradeShopping;
import com.liferay.shopping.upgrade.v1_0_0.UpgradeShoppingPreferences;
import com.liferay.shopping.upgrade.v1_0_1.UpgradeShoppingEmailAddress;

import org.osgi.service.component.annotations.Component;

/**
 * @author Peter Fellwock
 * @author Manuel de la Peña
 */
@Component(immediate = true, service = UpgradeStepRegistrator.class)
public class ShoppingServiceUpgrade implements UpgradeStepRegistrator {

	@Override
	public void register(Registry registry) {
		registry.register(
			"com.liferay.shopping.service", "0.0.1", "0.0.2",
			new UpgradeKernelPackage(), new UpgradePortletId());

		registry.register(
			"com.liferay.shopping.service", "0.0.2", "1.0.0",
			new UpgradeCompanyId(), new UpgradeShopping(),
			new UpgradeShoppingPreferences());

		registry.register(
			"com.liferay.shopping.service", "1.0.0", "1.0.1",
			new UpgradeShoppingEmailAddress());
	}

}