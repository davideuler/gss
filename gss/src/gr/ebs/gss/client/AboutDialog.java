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

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'about' dialog box.
 */
public class AboutDialog extends DialogBox {

	/**
	 * The widget constructor.
	 */
	public AboutDialog() {
		// Set the dialog's caption.
		setText("About GSS");
		setAnimationEnabled(true);
		// Create a VerticalPanel to contain the 'about' label and the 'OK'
		// button.
		final VerticalPanel outer = new VerticalPanel();

		// Create the 'about' text and set a style name so we can style it with CSS.
		final HTML text = new HTML("This is the Web client for the GSS service. You " +
					"can use it to store, retrieve and share files in the GSS server " +
					"grid. <p>Design and implementation: <a href='http://www.ebs.gr' target='about'>EBS</a></p>");
		text.setStyleName("gss-AboutText");
		outer.add(text);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button confirm = new Button("Close", new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
		outer.add(confirm);
		outer.setCellHorizontalAlignment(confirm, HasHorizontalAlignment.ALIGN_CENTER);
		outer.setSpacing(8);
		setWidget(outer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.PopupPanel#onKeyDownPreview(char, int)
	 */
	public boolean onKeyDownPreview(final char key, final int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}

		return true;
	}

}
