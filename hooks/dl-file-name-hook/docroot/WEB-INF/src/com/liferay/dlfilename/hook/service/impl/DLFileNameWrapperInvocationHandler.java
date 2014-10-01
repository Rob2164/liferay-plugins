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

package com.liferay.dlfilename.hook.service.impl;

import com.liferay.dlfilename.hook.model.impl.DLFileNameWrapperFileEntryImpl;
import com.liferay.dlfilename.hook.model.impl.DLFileNameWrapperFileVersionImpl;
import com.liferay.dlfilename.hook.util.DLFileNameThreadLocal;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

/**
* @author Preston Crary
*/
class DLFileNameWrapperInvocationHandler implements InvocationHandler {
	public DLFileNameWrapperInvocationHandler(Object object) {
		_object = object;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable {

		Object returnValue = method.invoke(_object, args);

		if (!DLFileNameThreadLocal.isEnabled()) {
			return returnValue;
		}

		if (returnValue instanceof List) {
			List wrappedList = new ArrayList();

			for (Object value : (List)returnValue) {
				wrappedList.add(wrap(value));
			}

			return wrappedList;
		}
		else {
			return wrap(returnValue);
		}
	}

	protected Object setAssetEntryTitle(AssetEntry assetEntry) {
		String className = assetEntry.getClassName();

		try {
			if (className.equals(DLFileEntry.class.getName())) {
				FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(
					assetEntry.getClassPK());

				assetEntry.setTitle(fileEntry.getTitle());
			}
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn("Failed to set AssetEntry title", e);
			}
		}

		return assetEntry;
	}

	protected Object wrap(Object object) {
		if (object instanceof AssetEntry) {
			return setAssetEntryTitle((AssetEntry)object);
		}

		if (object instanceof FileEntry) {
			return new DLFileNameWrapperFileEntryImpl((FileEntry)object);
		}

		if (object instanceof FileVersion) {
			return new DLFileNameWrapperFileVersionImpl((FileVersion)object);
		}

		return object;
	}

	private static Log _log = LogFactoryUtil.getLog(
		DLFileNameWrapperInvocationHandler.class);

	private Object _object;

}