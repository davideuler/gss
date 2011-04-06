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
package gr.ebs.gss.web.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A component that contains the search form.
 */
public class Search extends Composite implements FocusHandler,BlurHandler {

	/**
	 * The text hint that is displayed in the empty search box.
	 */
	private static final String TEXT_HINT = "Search for files...";

	/**
	 * Specifies the images that will be bundled for this Composite.
	 */
	public interface Images extends ClientBundle {
		@Source("gr/ebs/gss/resources/search_16.png")
		ImageResource searchButton();
	}

	/**
	 * The embedded text box widget that contains the search query.
	 */
	private TextBox tb = new TextBox();

	/**
	 * The search widget constructor.
	 *
	 * @param images the image bundle
	 */
	public Search(final Images images) {
		tb.setWidth("200px");
		tb.setText(TEXT_HINT);
		tb.setStylePrimaryName("gss-search");
		tb.addStyleDependentName("empty");
		tb.addFocusHandler(this);
		tb.addBlurHandler(this);
		tb.getElement().setId("textBox.search");
		tb.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				char keyCode = event.getCharCode();
				if (keyCode == '\r')
					GSS.get().showSearchResults(tb.getText());
				else if (keyCode == 27) {
					// Simulate the proper behavior for the escape key
					// (27 == ESC).
					onLostFocus((Widget)event.getSource());
					tb.setFocus(false);
				}
			}
		});

		Button b = new Button(createHeaderHTML(images.searchButton(), "Search"), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GSS.get().showSearchResults(tb.getText());
			}
		});
		b.getElement().setId("button.search");
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.add(tb);
		panel.add(b);
		initWidget(panel);
	}

	/**
	 * Creates an HTML fragment that places an image & caption together.
	 *
	 * @param imageProto an image prototype for an image
	 * @param caption the caption
	 * @return the HTML fragment
	 */
	private String createHeaderHTML(ImageResource imageProto, String caption) {
		String captionHTML = "<table cellpadding='0' cellspacing='0'>" + "<tr><td>" +
		AbstractImagePrototype.create(imageProto).getHTML() + "</td><td style='font-size: 90%;'>&nbsp;" +
			caption + "</td></tr></table>";
		return captionHTML;
	}

	public void onLostFocus(Widget sender) {
		TextBox b = (TextBox) sender;
		if (b.getText().equals("")) {
			b.addStyleDependentName("empty");
			b.setText(TEXT_HINT);
		}
	}

	@Override
	public void onFocus(FocusEvent event) {
		TextBox b = (TextBox) event.getSource();
		if (b.getText().equals(TEXT_HINT))
			b.setText("");
		b.removeStyleDependentName("empty");
	}

	@Override
	public void onBlur(BlurEvent event) {
		TextBox b = (TextBox) event.getSource();
		onLostFocus(b);
	}
}
