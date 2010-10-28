/*
 * $Id$
 * Created on 28 Oct 2010
 */

package com.ingenotech.tools;

import com.ingenotech.annotations.Version;

/**
 * Read the @Version annotation on a class.
 */
public class VersionReader {

	private String version;
	
	public VersionReader(Class<?> clazz) {
		Version va = clazz.getAnnotation(Version.class);
		if (va != null) {
			this.version = va.version();
		}
	}
	
	public String getVersion() {
		return this.version;
	}
}
