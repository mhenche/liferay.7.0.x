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

package com.liferay.wsrp.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import com.liferay.wsrp.exception.NoSuchConsumerPortletException;
import com.liferay.wsrp.model.WSRPConsumerPortlet;
import com.liferay.wsrp.service.WSRPConsumerPortletLocalServiceUtil;
import com.liferay.wsrp.service.persistence.WSRPConsumerPortletPersistence;
import com.liferay.wsrp.service.persistence.WSRPConsumerPortletUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.junit.runner.RunWith;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @generated
 */
@RunWith(Arquillian.class)
public class WSRPConsumerPortletPersistenceTest {
	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule = new AggregateTestRule(new LiferayIntegrationTestRule(),
			PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(Propagation.REQUIRED,
				"com.liferay.wsrp.service"));

	@Before
	public void setUp() {
		_persistence = WSRPConsumerPortletUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<WSRPConsumerPortlet> iterator = _wsrpConsumerPortlets.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		WSRPConsumerPortlet wsrpConsumerPortlet = _persistence.create(pk);

		Assert.assertNotNull(wsrpConsumerPortlet);

		Assert.assertEquals(wsrpConsumerPortlet.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		_persistence.remove(newWSRPConsumerPortlet);

		WSRPConsumerPortlet existingWSRPConsumerPortlet = _persistence.fetchByPrimaryKey(newWSRPConsumerPortlet.getPrimaryKey());

		Assert.assertNull(existingWSRPConsumerPortlet);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addWSRPConsumerPortlet();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		WSRPConsumerPortlet newWSRPConsumerPortlet = _persistence.create(pk);

		newWSRPConsumerPortlet.setUuid(RandomTestUtil.randomString());

		newWSRPConsumerPortlet.setCompanyId(RandomTestUtil.nextLong());

		newWSRPConsumerPortlet.setCreateDate(RandomTestUtil.nextDate());

		newWSRPConsumerPortlet.setModifiedDate(RandomTestUtil.nextDate());

		newWSRPConsumerPortlet.setWsrpConsumerId(RandomTestUtil.nextLong());

		newWSRPConsumerPortlet.setName(RandomTestUtil.randomString());

		newWSRPConsumerPortlet.setPortletHandle(RandomTestUtil.randomString());

		newWSRPConsumerPortlet.setLastPublishDate(RandomTestUtil.nextDate());

		_wsrpConsumerPortlets.add(_persistence.update(newWSRPConsumerPortlet));

		WSRPConsumerPortlet existingWSRPConsumerPortlet = _persistence.findByPrimaryKey(newWSRPConsumerPortlet.getPrimaryKey());

		Assert.assertEquals(existingWSRPConsumerPortlet.getUuid(),
			newWSRPConsumerPortlet.getUuid());
		Assert.assertEquals(existingWSRPConsumerPortlet.getWsrpConsumerPortletId(),
			newWSRPConsumerPortlet.getWsrpConsumerPortletId());
		Assert.assertEquals(existingWSRPConsumerPortlet.getCompanyId(),
			newWSRPConsumerPortlet.getCompanyId());
		Assert.assertEquals(Time.getShortTimestamp(
				existingWSRPConsumerPortlet.getCreateDate()),
			Time.getShortTimestamp(newWSRPConsumerPortlet.getCreateDate()));
		Assert.assertEquals(Time.getShortTimestamp(
				existingWSRPConsumerPortlet.getModifiedDate()),
			Time.getShortTimestamp(newWSRPConsumerPortlet.getModifiedDate()));
		Assert.assertEquals(existingWSRPConsumerPortlet.getWsrpConsumerId(),
			newWSRPConsumerPortlet.getWsrpConsumerId());
		Assert.assertEquals(existingWSRPConsumerPortlet.getName(),
			newWSRPConsumerPortlet.getName());
		Assert.assertEquals(existingWSRPConsumerPortlet.getPortletHandle(),
			newWSRPConsumerPortlet.getPortletHandle());
		Assert.assertEquals(Time.getShortTimestamp(
				existingWSRPConsumerPortlet.getLastPublishDate()),
			Time.getShortTimestamp(newWSRPConsumerPortlet.getLastPublishDate()));
	}

	@Test
	public void testCountByUuid() throws Exception {
		_persistence.countByUuid("");

		_persistence.countByUuid("null");

		_persistence.countByUuid((String)null);
	}

	@Test
	public void testCountByUuid_C() throws Exception {
		_persistence.countByUuid_C("", RandomTestUtil.nextLong());

		_persistence.countByUuid_C("null", 0L);

		_persistence.countByUuid_C((String)null, 0L);
	}

	@Test
	public void testCountByWsrpConsumerId() throws Exception {
		_persistence.countByWsrpConsumerId(RandomTestUtil.nextLong());

		_persistence.countByWsrpConsumerId(0L);
	}

	@Test
	public void testCountByW_P() throws Exception {
		_persistence.countByW_P(RandomTestUtil.nextLong(), "");

		_persistence.countByW_P(0L, "null");

		_persistence.countByW_P(0L, (String)null);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		WSRPConsumerPortlet existingWSRPConsumerPortlet = _persistence.findByPrimaryKey(newWSRPConsumerPortlet.getPrimaryKey());

		Assert.assertEquals(existingWSRPConsumerPortlet, newWSRPConsumerPortlet);
	}

	@Test(expected = NoSuchConsumerPortletException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			getOrderByComparator());
	}

	protected OrderByComparator<WSRPConsumerPortlet> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create("WSRP_WSRPConsumerPortlet",
			"uuid", true, "wsrpConsumerPortletId", true, "companyId", true,
			"createDate", true, "modifiedDate", true, "wsrpConsumerId", true,
			"name", true, "portletHandle", true, "lastPublishDate", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		WSRPConsumerPortlet existingWSRPConsumerPortlet = _persistence.fetchByPrimaryKey(newWSRPConsumerPortlet.getPrimaryKey());

		Assert.assertEquals(existingWSRPConsumerPortlet, newWSRPConsumerPortlet);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		WSRPConsumerPortlet missingWSRPConsumerPortlet = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingWSRPConsumerPortlet);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet1 = addWSRPConsumerPortlet();
		WSRPConsumerPortlet newWSRPConsumerPortlet2 = addWSRPConsumerPortlet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newWSRPConsumerPortlet1.getPrimaryKey());
		primaryKeys.add(newWSRPConsumerPortlet2.getPrimaryKey());

		Map<Serializable, WSRPConsumerPortlet> wsrpConsumerPortlets = _persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, wsrpConsumerPortlets.size());
		Assert.assertEquals(newWSRPConsumerPortlet1,
			wsrpConsumerPortlets.get(newWSRPConsumerPortlet1.getPrimaryKey()));
		Assert.assertEquals(newWSRPConsumerPortlet2,
			wsrpConsumerPortlets.get(newWSRPConsumerPortlet2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {
		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, WSRPConsumerPortlet> wsrpConsumerPortlets = _persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(wsrpConsumerPortlets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newWSRPConsumerPortlet.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, WSRPConsumerPortlet> wsrpConsumerPortlets = _persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, wsrpConsumerPortlets.size());
		Assert.assertEquals(newWSRPConsumerPortlet,
			wsrpConsumerPortlets.get(newWSRPConsumerPortlet.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys()
		throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, WSRPConsumerPortlet> wsrpConsumerPortlets = _persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(wsrpConsumerPortlets.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey()
		throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newWSRPConsumerPortlet.getPrimaryKey());

		Map<Serializable, WSRPConsumerPortlet> wsrpConsumerPortlets = _persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, wsrpConsumerPortlets.size());
		Assert.assertEquals(newWSRPConsumerPortlet,
			wsrpConsumerPortlets.get(newWSRPConsumerPortlet.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery = WSRPConsumerPortletLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(new ActionableDynamicQuery.PerformActionMethod<WSRPConsumerPortlet>() {
				@Override
				public void performAction(
					WSRPConsumerPortlet wsrpConsumerPortlet) {
					Assert.assertNotNull(wsrpConsumerPortlet);

					count.increment();
				}
			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting()
		throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(WSRPConsumerPortlet.class,
				_dynamicQueryClassLoader);

		dynamicQuery.add(RestrictionsFactoryUtil.eq("wsrpConsumerPortletId",
				newWSRPConsumerPortlet.getWsrpConsumerPortletId()));

		List<WSRPConsumerPortlet> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		WSRPConsumerPortlet existingWSRPConsumerPortlet = result.get(0);

		Assert.assertEquals(existingWSRPConsumerPortlet, newWSRPConsumerPortlet);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(WSRPConsumerPortlet.class,
				_dynamicQueryClassLoader);

		dynamicQuery.add(RestrictionsFactoryUtil.eq("wsrpConsumerPortletId",
				RandomTestUtil.nextLong()));

		List<WSRPConsumerPortlet> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting()
		throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(WSRPConsumerPortlet.class,
				_dynamicQueryClassLoader);

		dynamicQuery.setProjection(ProjectionFactoryUtil.property(
				"wsrpConsumerPortletId"));

		Object newWsrpConsumerPortletId = newWSRPConsumerPortlet.getWsrpConsumerPortletId();

		dynamicQuery.add(RestrictionsFactoryUtil.in("wsrpConsumerPortletId",
				new Object[] { newWsrpConsumerPortletId }));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingWsrpConsumerPortletId = result.get(0);

		Assert.assertEquals(existingWsrpConsumerPortletId,
			newWsrpConsumerPortletId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(WSRPConsumerPortlet.class,
				_dynamicQueryClassLoader);

		dynamicQuery.setProjection(ProjectionFactoryUtil.property(
				"wsrpConsumerPortletId"));

		dynamicQuery.add(RestrictionsFactoryUtil.in("wsrpConsumerPortletId",
				new Object[] { RandomTestUtil.nextLong() }));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		WSRPConsumerPortlet newWSRPConsumerPortlet = addWSRPConsumerPortlet();

		_persistence.clearCache();

		WSRPConsumerPortlet existingWSRPConsumerPortlet = _persistence.findByPrimaryKey(newWSRPConsumerPortlet.getPrimaryKey());

		Assert.assertEquals(Long.valueOf(
				existingWSRPConsumerPortlet.getWsrpConsumerId()),
			ReflectionTestUtil.<Long>invoke(existingWSRPConsumerPortlet,
				"getOriginalWsrpConsumerId", new Class<?>[0]));
		Assert.assertTrue(Objects.equals(
				existingWSRPConsumerPortlet.getPortletHandle(),
				ReflectionTestUtil.invoke(existingWSRPConsumerPortlet,
					"getOriginalPortletHandle", new Class<?>[0])));
	}

	protected WSRPConsumerPortlet addWSRPConsumerPortlet()
		throws Exception {
		long pk = RandomTestUtil.nextLong();

		WSRPConsumerPortlet wsrpConsumerPortlet = _persistence.create(pk);

		wsrpConsumerPortlet.setUuid(RandomTestUtil.randomString());

		wsrpConsumerPortlet.setCompanyId(RandomTestUtil.nextLong());

		wsrpConsumerPortlet.setCreateDate(RandomTestUtil.nextDate());

		wsrpConsumerPortlet.setModifiedDate(RandomTestUtil.nextDate());

		wsrpConsumerPortlet.setWsrpConsumerId(RandomTestUtil.nextLong());

		wsrpConsumerPortlet.setName(RandomTestUtil.randomString());

		wsrpConsumerPortlet.setPortletHandle(RandomTestUtil.randomString());

		wsrpConsumerPortlet.setLastPublishDate(RandomTestUtil.nextDate());

		_wsrpConsumerPortlets.add(_persistence.update(wsrpConsumerPortlet));

		return wsrpConsumerPortlet;
	}

	private List<WSRPConsumerPortlet> _wsrpConsumerPortlets = new ArrayList<WSRPConsumerPortlet>();
	private WSRPConsumerPortletPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;
}