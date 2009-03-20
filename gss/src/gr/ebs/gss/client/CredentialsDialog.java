/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * A dialog box that displays the user credentials for use in other client
 * applications, such as WebDAV clients.
 *
 * @author kman
 */
public class CredentialsDialog extends DialogBox {

	static final String WIDTH = "35em";
	/**
	 * The widget constructor.
	 */
	public CredentialsDialog() {
		// Set the dialog's caption.
		setText("User Credentials");
		setAnimationEnabled(true);
		// Create a VerticalPanel to contain the 'about' label and the 'OK'
		// button.
		VerticalPanel outer = new VerticalPanel();
		String token = GSS.get().getToken();
		if(token == null)
			token = "";
		int contextIndex = GWT.getModuleBaseURL().lastIndexOf("gss/");
		String webdavUrl = GWT.getModuleBaseURL().substring(0, contextIndex) + "webdav";
		// Create the text and set a style name so we can style it with CSS.
		HTML text = new HTML("<p>These are the user credentials that are required " +
				"for interacting with GSS. You can copy and paste them in the WebDAV " +
				"client as username/password, in order to use GSS through the WebDAV " +
				"interface, at:<br/> " + webdavUrl + "</p>");
		text.setStyleName("gss-AboutText");
		text.setWidth("42em");
		outer.add(text);
		FlexTable table = new FlexTable();
		table.setText(0, 0, "Username");
		table.setText(1, 0, "Token");
		TextBox username = new TextBox();
		username.setText(GSS.get().getCurrentUserResource().getUsername());
		username.setReadOnly(true);
		username.setWidth(WIDTH);
		username.addFocusListener(new FocusListener() {
			public void onFocus(Widget sender) {
				((TextBox) sender).selectAll();
			}
			public void onLostFocus(Widget sender) {
				((TextBox) sender).setSelectionRange(0, 0);
			}
		});
		table.setWidget(0, 1, username);
		TextBox tokenBox = new TextBox();
		tokenBox.setText(token);
		tokenBox.setReadOnly(true);
		tokenBox.setWidth(WIDTH);
		tokenBox.addFocusListener(new FocusListener() {
			public void onFocus(Widget sender) {
				((TextBox) sender).selectAll();
			}
			public void onLostFocus(Widget sender) {
				((TextBox) sender).setSelectionRange(0, 0);
			}
		});
		table.setWidget(1, 1, tokenBox);
		table.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		table.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		table.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		table.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		outer.add(table);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		Button confirm = new Button("Close", new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
		outer.add(confirm);
		outer.setCellHorizontalAlignment(confirm, HasHorizontalAlignment.ALIGN_CENTER);
		outer.setSpacing(8);
		setWidget(outer);
	}

	@Override
	public boolean onKeyDownPreview(char key, int modifiers) {
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
