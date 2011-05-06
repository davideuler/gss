/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * This helper class was extracted verbatim from Tomcat's DefaultServlet.java
 * file, in order to extend its scope to public.
 */
package org.gss_project.gss.server.rest;

public class Range {

	public long start;
	public long end;
	public long length;

	/**
	 * Validate range.
	 */
	public boolean validate() {
		if (end >= length)
			end = length - 1;
		return start >= 0 && end >= 0 && start <= end
					&& length > 0;
	}

	public void recycle() {
		start = 0;
		end = 0;
		length = 0;
	}

}