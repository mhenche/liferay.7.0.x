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

package com.liferay.adaptive.media.document.library.web.internal.optimizer.test;

import com.liferay.adaptive.media.image.configuration.AMImageConfigurationEntry;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationHelper;
import com.liferay.adaptive.media.image.optimizer.AMImageOptimizer;
import com.liferay.adaptive.media.image.service.AMImageEntryLocalServiceUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLTrashLocalServiceUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Sergio González
 */
@RunWith(Arquillian.class)
@Sync
public class DLAMImageOptimizerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_amImageConfigurationHelper = _getService(
			AMImageConfigurationHelper.class);

		_amImageOptimizer = _getService(
			AMImageOptimizer.class, "(adaptive.media.key=document-library)");

		_company1 = CompanyTestUtil.addCompany();

		_user1 = UserTestUtil.getAdminUser(_company1.getCompanyId());

		_group1 = GroupTestUtil.addGroup(
			_company1.getCompanyId(), _user1.getUserId(),
			GroupConstants.DEFAULT_PARENT_GROUP_ID);

		_company2 = CompanyTestUtil.addCompany();

		_user2 = UserTestUtil.getAdminUser(_company2.getCompanyId());

		_group2 = GroupTestUtil.addGroup(
			_company2.getCompanyId(), _user2.getUserId(),
			GroupConstants.DEFAULT_PARENT_GROUP_ID);
	}

	@After
	public void tearDown() throws Exception {
		_deleteAllAMImageConfigurationEntries(_company1.getCompanyId());
		_deleteAllAMImageConfigurationEntries(_company2.getCompanyId());
	}

	@Test
	public void testDLAMImageOptimizerOptimizesEveryAMImageConfigurationEntryInSpecificCompany()
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group1.getGroupId(), _user1.getUserId());

		DLAppLocalServiceUtil.addFileEntry(
			_user1.getUserId(), _group1.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);

		AMImageConfigurationEntry amImageConfigurationEntry1 =
			_addAMImageConfigurationEntry(_company1.getCompanyId());
		AMImageConfigurationEntry amImageConfigurationEntry2 =
			_addAMImageConfigurationEntry(_company1.getCompanyId());

		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));

		_amImageOptimizer.optimize(_company1.getCompanyId());

		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));
	}

	@Test
	public void testDLAMImageOptimizerOptimizesEveryAMImageConfigurationEntryOnlyInSpecificCompany()
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group1.getGroupId(), _user1.getUserId());

		DLAppLocalServiceUtil.addFileEntry(
			_user1.getUserId(), _group1.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);

		serviceContext = ServiceContextTestUtil.getServiceContext(
			_group2.getGroupId(), _user2.getUserId());

		DLAppLocalServiceUtil.addFileEntry(
			_user2.getUserId(), _group2.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);

		AMImageConfigurationEntry amImageConfigurationEntry1 =
			_addAMImageConfigurationEntry(_company1.getCompanyId());
		AMImageConfigurationEntry amImageConfigurationEntry2 =
			_addAMImageConfigurationEntry(_company2.getCompanyId());

		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company2.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));

		_amImageOptimizer.optimize(_company1.getCompanyId());

		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company2.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));

		_amImageOptimizer.optimize(_company2.getCompanyId());

		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company2.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));
	}

	@Test
	public void testDLAMImageOptimizerOptimizesForImagesNotInTrash()
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group1.getGroupId(), _user1.getUserId());

		DLAppLocalServiceUtil.addFileEntry(
			_user1.getUserId(), _group1.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);

		FileEntry fileEntry2 = DLAppLocalServiceUtil.addFileEntry(
			_user1.getUserId(), _group1.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);

		DLTrashLocalServiceUtil.moveFileEntryToTrash(
			_user1.getUserId(), _group1.getGroupId(),
			fileEntry2.getFileEntryId());

		AMImageConfigurationEntry amImageConfigurationEntry1 =
			_addAMImageConfigurationEntry(_company1.getCompanyId());

		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));

		_amImageOptimizer.optimize(
			_company1.getCompanyId(), amImageConfigurationEntry1.getUUID());

		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));

		DLTrashLocalServiceUtil.moveFileEntryFromTrash(
			_user1.getUserId(), _group1.getGroupId(),
			fileEntry2.getFileEntryId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, serviceContext);

		_amImageOptimizer.optimize(
			_company1.getCompanyId(), amImageConfigurationEntry1.getUUID());

		Assert.assertEquals(
			2,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
	}

	@Test
	public void testDLAMImageOptimizerOptimizesForSpecificAMImageConfigurationEntry()
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group1.getGroupId(), _user1.getUserId());

		DLAppLocalServiceUtil.addFileEntry(
			_user1.getUserId(), _group1.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);

		AMImageConfigurationEntry amImageConfigurationEntry1 =
			_addAMImageConfigurationEntry(_company1.getCompanyId());
		AMImageConfigurationEntry amImageConfigurationEntry2 =
			_addAMImageConfigurationEntry(_company1.getCompanyId());

		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));

		_amImageOptimizer.optimize(
			_company1.getCompanyId(), amImageConfigurationEntry1.getUUID());

		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));

		_amImageOptimizer.optimize(
			_company1.getCompanyId(), amImageConfigurationEntry2.getUUID());

		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry2.getUUID()));
	}

	@Test
	public void testDLAMImageOptimizerOptimizesImagesWithSupportedMimeTypes()
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group1.getGroupId(), _user1.getUserId());

		DLAppLocalServiceUtil.addFileEntry(
			_user1.getUserId(), _group1.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);

		DLAppLocalServiceUtil.addFileEntry(
			_user1.getUserId(), _group1.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(),
			ContentTypes.APPLICATION_OCTET_STREAM, RandomTestUtil.randomBytes(),
			serviceContext);

		AMImageConfigurationEntry amImageConfigurationEntry1 =
			_addAMImageConfigurationEntry(_company1.getCompanyId());

		Assert.assertEquals(
			0,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));

		_amImageOptimizer.optimize(
			_company1.getCompanyId(), amImageConfigurationEntry1.getUUID());

		Assert.assertEquals(
			1,
			AMImageEntryLocalServiceUtil.getAMImageEntriesCount(
				_company1.getCompanyId(),
				amImageConfigurationEntry1.getUUID()));
	}

	private AMImageConfigurationEntry _addAMImageConfigurationEntry(
			long companyId)
		throws Exception {

		String amImageConfigurationEntry1Name = RandomTestUtil.randomString();

		Map<String, String> properties = new HashMap<>();

		properties.put(
			"max-height", String.valueOf(RandomTestUtil.randomLong()));
		properties.put(
			"max-width", String.valueOf(RandomTestUtil.randomLong()));

		return _amImageConfigurationHelper.addAMImageConfigurationEntry(
			companyId, amImageConfigurationEntry1Name, StringPool.BLANK,
			amImageConfigurationEntry1Name, properties);
	}

	private void _deleteAllAMImageConfigurationEntries(long companyId)
		throws Exception {

		Collection<AMImageConfigurationEntry> amImageConfigurationEntries =
			_amImageConfigurationHelper.getAMImageConfigurationEntries(
				companyId, amImageConfigurationEntry -> true);

		for (AMImageConfigurationEntry amImageConfigurationEntry :
				amImageConfigurationEntries) {

			_amImageConfigurationHelper.forceDeleteAMImageConfigurationEntry(
				companyId, amImageConfigurationEntry.getUUID());
		}
	}

	private byte[] _getImageBytes() throws Exception {
		return FileUtil.getBytes(
			DLAMImageOptimizerTest.class,
			"/com/liferay/adaptive/media/document/library/web/internal" +
				"/optimizer/test/dependencies/image.jpg");
	}

	private <T> T _getService(Class<T> clazz) {
		try {
			Registry registry = RegistryUtil.getRegistry();

			return registry.getService(clazz);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private <T> T _getService(Class<T> clazz, String filter) {
		try {
			Registry registry = RegistryUtil.getRegistry();

			Collection<T> services = registry.getServices(clazz, filter);

			return services.iterator().next();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private AMImageConfigurationHelper _amImageConfigurationHelper;
	private AMImageOptimizer _amImageOptimizer;

	@DeleteAfterTestRun
	private Company _company1;

	@DeleteAfterTestRun
	private Company _company2;

	private Group _group1;
	private Group _group2;
	private User _user1;
	private User _user2;

}