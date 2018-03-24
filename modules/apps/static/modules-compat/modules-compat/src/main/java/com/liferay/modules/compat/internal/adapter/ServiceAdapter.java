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

package com.liferay.modules.compat.internal.adapter;

import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.io.Closeable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Shuyang Zhou
 */
public class ServiceAdapter<F, T> implements Closeable {

	public ServiceAdapter(
		BundleContext bundleContext, Class<F> fromClass, Class<T> toClass) {

		_bundleContext = bundleContext;

		_fromServiceTracker = new ServiceTracker<>(
			_bundleContext, fromClass,
			new AdaptorServiceTrackerCustomizer<>(_bundleContext, toClass));

		_fromServiceTracker.open();

		_toServiceTracker = new ServiceTracker<>(
			_bundleContext, toClass,
			new AdaptorServiceTrackerCustomizer<>(_bundleContext, fromClass));

		_toServiceTracker.open();
	}

	@Override
	public void close() {
		_fromServiceTracker.close();

		_toServiceTracker.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(ServiceAdapter.class);

	private final BundleContext _bundleContext;
	private final ServiceTracker<F, ServiceRegistration<T>> _fromServiceTracker;
	private final ServiceTracker<T, ServiceRegistration<F>> _toServiceTracker;

	private static class AdaptorServiceTrackerCustomizer<F, T>
		implements ServiceTrackerCustomizer<F, ServiceRegistration<T>> {

		@Override
		public ServiceRegistration<T> addingService(
			ServiceReference<F> serviceReference) {

			F fromService = _bundleContext.getService(serviceReference);

			if ((fromService == null) || _adaptingInProcess.get()) {
				return null;
			}

			_adaptingInProcess.set(true);

			Bundle bundle = FrameworkUtil.getBundle(_clazz);

			BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);

			ClassLoader classLoader = bundleWiring.getClassLoader();

			try {
				return _bundleContext.registerService(
					_clazz,
					(T)ProxyUtil.newProxyInstance(
						classLoader,
						new Class<?>[] {
							classLoader.loadClass(_clazz.getName())
						},
						new DelegateInvocationHandler(fromService)),
					_getProperties(serviceReference));
			}
			catch (ClassNotFoundException cnfe) {
				_log.error("Unable to refresh class " + _clazz, cnfe);

				return null;
			}
			finally {
				_adaptingInProcess.set(false);
			}
		}

		@Override
		public void modifiedService(
			ServiceReference<F> serviceReference,
			ServiceRegistration<T> service) {

			service.setProperties(_getProperties(serviceReference));
		}

		@Override
		public void removedService(
			ServiceReference<F> serviceReference,
			ServiceRegistration<T> serviceRegistration) {

			_bundleContext.ungetService(serviceReference);

			if (_adaptingInProcess.get()) {
				return;
			}

			_adaptingInProcess.set(true);

			serviceRegistration.unregister();

			_adaptingInProcess.set(false);
		}

		private AdaptorServiceTrackerCustomizer(
			BundleContext bundleContext, Class<T> clazz) {

			_bundleContext = bundleContext;
			_clazz = clazz;
		}

		private Dictionary<String, Object> _getProperties(
			ServiceReference<?> serviceReference) {

			Dictionary<String, Object> dictionary = new HashMapDictionary<>();

			for (String key : serviceReference.getPropertyKeys()) {
				if (key.startsWith("component.") || key.equals("objectClass") ||
					key.startsWith("service.")) {

					continue;
				}

				dictionary.put(key, serviceReference.getProperty(key));
			}

			return dictionary;
		}

		private static final ThreadLocal<Boolean> _adaptingInProcess =
			new CentralizedThreadLocal<>(
				AdaptorServiceTrackerCustomizer.class.getName() +
					"._adaptingInProcess",
				() -> false);

		private final BundleContext _bundleContext;
		private final Class<T> _clazz;

	}

	private static class DelegateInvocationHandler
		implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
			throws ReflectiveOperationException {

			Class<?> delegateClass = _delegateObject.getClass();

			try {
				method = delegateClass.getMethod(
					method.getName(), method.getParameterTypes());
			}
			catch (NoSuchMethodException nsme) {
				_log.error(
					StringBundler.concat(
						"Unable to delegate ", String.valueOf(method),
						" on class ", String.valueOf(delegateClass)));

				Class<?> returnType = method.getReturnType();

				if (returnType.isPrimitive()) {
					if (returnType.equals(boolean.class)) {
						return false;
					}

					return 0;
				}

				return null;
			}

			return method.invoke(_delegateObject, args);
		}

		private DelegateInvocationHandler(Object delegateObject) {
			_delegateObject = delegateObject;
		}

		private final Object _delegateObject;

	}

}