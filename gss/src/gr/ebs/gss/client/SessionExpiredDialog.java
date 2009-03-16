/*
 * Copyright 2009 Electronic Business Systems Ltd.
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
		Button confirm = new Button("Proceed", new ClickListener() {

			public void onClick(Widget sender) {
				GSS.get().authenticateUser();
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
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				GSS.get().authenticateUser();
				hide();
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}

		return true;
	}

}
