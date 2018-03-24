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

package com.liferay.portal.util;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Assert;

/**
 * @author Manuel de la Peña
 */
public class LicenseUtilAixTest extends BaseLicenseUtilTestCase {

	@Override
	protected String getDependenciesFileName() {
		return "aix";
	}

	@Override
	protected Pattern getMacAddressPattern() {
		return Pattern.compile(
			"\\s((\\p{XDigit}{1,2}(\\.)){5}(\\p{XDigit}{1,2}))(?:\\s|$)");
	}

	@Override
	protected void testMacAddresses(Set<String> macAddresses) {
		Assert.assertEquals(macAddresses.toString(), 2, macAddresses.size());
		Assert.assertTrue(
			macAddresses.toString(),
			macAddresses.contains("66:da:90:6b:f1:17"));
		Assert.assertTrue(
			macAddresses.toString(),
			macAddresses.contains("66:da:90:6b:f1:18"));
	}

}