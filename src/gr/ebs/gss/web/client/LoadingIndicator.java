/*
 * Copyright 2007, 2008, 2009, 2010 Electronic Business Systems Ltd.
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
package gr.ebs.gss.web.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * The 'loading' indicator widget implementation.
 */
public class LoadingIndicator extends Composite {

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle {
		@Source("gr/ebs/gss/resources/ajax-loader.gif")
		ImageResource loading();
	}

	/**
	 * The widget's constructor that creates a spinning indicator image.
	 */
	public LoadingIndicator(Images images) {
		HTML inner = new HTML(AbstractImagePrototype.create(images.loading()).getHTML());
		initWidget(inner);
	}
}
