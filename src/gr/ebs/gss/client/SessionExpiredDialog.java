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
package gr.ebs.gss.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * @author kman
 *
 */
public class SessionExpiredDialog extends DialogBox {
	/**
	 * The widget constructor.
	 */
	public SessionExpiredDialog() {
		// Set the dialog's caption.
		setText("Session Expired");
		setAnimationEnabled(true);
		VerticalPanel outer = new VerticalPanel();

		// Create the text and set a style name so we can style it with CSS.
		HTML text = new HTML("<p>Your session has expired. You will have to reauthenticate with your Identity Provider.</p> ");
		text.setStyleName("gss-AboutText");
		outer.add(text);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		Button confirm = new Button("Proceed", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GSS.get().authenticateUser();
				hide();
			}
		});
		outer.add(confirm);
		outer.setCellHorizontalAlignment(confirm, HasHorizontalAlignment.ALIGN_CENTER);
		outer.setSpacing(8);
		setWidget(outer);
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					GSS.get().authenticateUser();
					hide();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}



}
