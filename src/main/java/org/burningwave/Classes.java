/*
 * This file is part of Burningwave Common.
 *
 * Author: Roberto Gentili
 *
 * Hosted at: https://github.com/burningwave/common
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Roberto Gentili
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.burningwave;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class Classes {
	public static final Classes INSTANCE;

	static {
		INSTANCE = new Classes();
	}

	Constructor<?>[] emptyConstructorsArray;
	Method[] emptyMethodsArray;
	Field[] emtpyFieldsArray;


	private Classes() {
		emtpyFieldsArray = new Field[]{};
		emptyMethodsArray = new Method[]{};
		emptyConstructorsArray = new Constructor<?>[]{};
	}

	public Class<?> getClassOrWrapper(Class<?> cls) {
		if (cls.isPrimitive()) {
			if (cls == short.class) {
				return Short.class;
			} else if (cls == int.class) {
				return Integer.class;
			} else if (cls == long.class) {
				return Long.class;
			} else if (cls == float.class) {
				return Float.class;
			} else if (cls == double.class) {
				return Double.class;
			} else if (cls == boolean.class) {
				return Boolean.class;
			} else if (cls == byte.class) {
				return Byte.class;
			} else if (cls == char.class) {
				return Character.class;
			}
		}
		return cls;
	}

	public boolean isAssignableFrom(Class<?> clsOne, Class<?> clsTwo) {
		return getClassOrWrapper(clsOne).isAssignableFrom(getClassOrWrapper(clsTwo));
	}

	public <T> Class<T> retrieveFrom(Object object) {
		return object != null ? (Class<T>)object.getClass() : null;
	}

	public Class<?>[] retrieveFrom(Object... objects) {
		Class<?>[] classes = null;
		if (objects != null) {
			classes = new Class[objects.length];
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] != null) {
					classes[i] = retrieveFrom(objects[i]);
				}
			}
		} else {
			classes = new Class[]{null};
		}
		return classes;
	}

	public String retrievePackageName(String className) {
		String packageName = null;
		if (className.contains(("."))) {
			packageName = className.substring(0, className.lastIndexOf("."));
		}
		return packageName;
	}

	public String retrieveSimpleName(String className) {
		String classSimpleName = null;
		if (className.contains(("."))) {
			classSimpleName = className.substring(className.lastIndexOf(".")+1);
		} else {
			classSimpleName = className;
		}
		if (classSimpleName.contains("$")) {
			classSimpleName = classSimpleName.substring(classSimpleName.lastIndexOf("$")+1);
		}
		return classSimpleName;
	}

	public boolean isPrimitive(Object object) {
		return object instanceof String ||
			object instanceof Short ||
			object instanceof Integer ||
			object instanceof Long ||
			object instanceof Float ||
			object instanceof Double ||
			object instanceof Boolean ||
			object instanceof Byte ||
			object instanceof Character;
	}

}
