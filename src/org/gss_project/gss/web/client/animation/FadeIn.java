/*
 * Copyright 2008, 2009, 2010 Electronic Business Systems Ltd.
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
package org.gss_project.gss.web.client.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author kman
 *
 */
public class FadeIn extends Animation {
	Widget widget;
	int initialOpacity = 100;
	double currOpacity = 100;

	public FadeIn(Widget aWidget){
		widget = aWidget;
	}

	@Override
	protected void onUpdate(double progress) {
		if(currOpacity > 0.0){
			progress = 1.0 - progress;
			currOpacity = initialOpacity * progress;
			DOM.setStyleAttribute(widget.getElement(), "opacity", ""+new Double(1d - currOpacity / 100d));
			//required for ie to work
			//Disabled because IE has bugs rendering non-opaque objects
			//int opacityToSet = new Double(currOpacity).intValue();
			//DOM.setStyleAttribute(widget.getElement(), "filter", "alpha(opacity=" + (initialOpacity - opacityToSet) + ")");
		}
	}

}
