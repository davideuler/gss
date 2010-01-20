/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'loading' indicator widget implementation.
 */
public class LoadingIndicator extends PopupPanel {

	/**
	 * The widget's constructor that creates an inner div that displays the
	 * images specified in the stylesheet.
	 */
	public LoadingIndicator() {
		final HTML inner = new HTML("<div id='loading-area' class='hidden'><p>Loading...</p></div>");
		addStyleName("gss-loading");
		setWidget(inner);
	}
}
