/*
 * Copyright 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.ebs.gss.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A cache filter that properly caches GWT-generated files.
 *
 * @author past
 *
 */
public class CacheFilter implements Filter {
 	public void doFilter( ServletRequest request, ServletResponse response,
 			FilterChain filterChain) throws IOException, ServletException {
 		HttpServletRequest httpRequest = (HttpServletRequest)request;
 		String requestURI = httpRequest.getRequestURI();
		if (requestURI.contains(".nocache.")) {
 			HttpServletResponse httpResponse = (HttpServletResponse)response;
 			httpResponse.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
 			httpResponse.setHeader("Pragma", "no-cache");
 			httpResponse.setHeader("Cache-control", "no-cache, must-revalidate");
 		} else if (requestURI.contains(".cache.")) {
 			long today = new Date().getTime();
 			HttpServletResponse httpResponse = (HttpServletResponse)response;
 			httpResponse.setDateHeader("Expires", today+31536000000L);
 		} else {
 			long today = new Date().getTime();
 			HttpServletResponse httpResponse = (HttpServletResponse)response;
 			httpResponse.setDateHeader("Expires", today+3456000000L);
 		}
 		filterChain.doFilter(request, response);
 	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) {
	}
}
