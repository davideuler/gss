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

import gr.ebs.gss.client.MessagePanel.Images;
import gr.ebs.gss.client.domain.GroupDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author kman
 *
 */
public class DeleteUserDialog extends DialogBox {

	/**
	 * The widget's constructor.
	 * @param images the supplied images
	 */
	public DeleteUserDialog(final Images images) {
		// Use this opportunity to set the dialog's caption.
		setText("Delete user");
		setAnimationEnabled(true);
		final UserDTO group = (UserDTO) GSS.get().getCurrentSelection();
		// Create a VerticalPanel to contain the 'about' label and the 'OK'
		// button.
		final VerticalPanel outer = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();

		// Create the 'about' text and set a style name so we can style it with
		// CSS.
		final HTML text = new HTML("<table><tr><td>" + images.warn().getHTML() + "</td><td>" + "Are you sure you want to remove user '" + group.getName() + "'?</td></tr></table>");
		text.setStyleName("gss-warnMessage");
		outer.add(text);

		// Create the 'Quit' button, along with a listener that hides the dialog
		// when the button is clicked and quits the application.
		final Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {
				deleteUser(GSS.get().getCurrentUser().getId());
				hide();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
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

	/**
	 * Generate an RPC request to delete a group.
	 *
	 * @param userId the ID of the current user
	 */
	private void deleteUser(final Long userId) {
		final GSSServiceAsync service = GSS.get().getRemoteService();
		final TreeItem user = GSS.get().getGroups().getCurrent();
		final TreeItem group = user.getParentItem();
		if (group == null) {
			GSS.get().displayError("No user was selected!");
			return;
		}
		final Long groupId = ((GroupDTO) group.getUserObject()).getId();
		final Long memberId = ((UserDTO) user.getUserObject()).getId();
		service.removeMemberFromGroup(userId, groupId, memberId, new AsyncCallback() {

			public void onSuccess(final Object result) {
				GSS.get().getGroups().updateGroups(GSS.get().getCurrentUser().getId());
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});
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
				hide();
				deleteUser(GSS.get().getCurrentUser().getId());
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}

		return true;
	}

}

