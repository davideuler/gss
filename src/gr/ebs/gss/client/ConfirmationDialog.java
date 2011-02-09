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
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * A dialog for requesting confirmation from the user.
 *
 * @author kman
 */
public abstract class ConfirmationDialog extends DialogBox {

	/**
	 * The widget's constructor.
	 *
	 * @param message the message to display
	 * @param buttonLabel the label of the confirmation button
	 */
	public ConfirmationDialog(String message, String buttonLabel) {
		// Set the dialog's caption.
		setText("Confirmation");
		setAnimationEnabled(true);
		// Create a VerticalPanel to contain the label and the buttons.
		VerticalPanel outer = new VerticalPanel();
		HorizontalPanel buttons = new HorizontalPanel();

		HTML text = new HTML("<table><tr><td rowspan='2'> " +
				AbstractImagePrototype.create(MessagePanel.images.warn()).getHTML() +
				"</td><td>" + message + "</td></tr></table>");
		text.setStyleName("gss-warnMessage");
		outer.add(text);

		// Create the 'Update' button, along with a listener that hides the
		// dialog when the button is clicked and renames the file.
		Button ok = new Button(buttonLabel, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				confirm();
				hide();
			}
		});
		ok.getElement().setId("confirmation.ok");
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		Button cancel = new Button("Cancel", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
				cancel();
			}
		});
		cancel.getElement().setId("confirmation.cancel");
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.setStyleName("gss-warnMessage");
		outer.setStyleName("gss-warnMessage");
		outer.add(buttons);
		outer.setCellHorizontalAlignment(text, HasHorizontalAlignment.ALIGN_CENTER);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
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
					hide();
					confirm();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					cancel();
					break;
			}
	}

	public abstract void confirm();

	public abstract void cancel();
}
