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

import gr.ebs.gss.client.MessagePanel.Images;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.DeleteCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FolderResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
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
 * The 'delete folder' dialog box.
 */
public class DeleteFolderDialog extends DialogBox {

	/**
	 * The widget's constructor.
	 * @param images the supplied images
	 */
	public DeleteFolderDialog(Images images) {
		// Set the dialog's caption.
		setText("Confirmation");
		setAnimationEnabled(true);
		FolderResource folder = (FolderResource) GSS.get().getCurrentSelection();
		// Create a VerticalPanel to contain the HTML label and the buttons.
		VerticalPanel outer = new VerticalPanel();
		HorizontalPanel buttons = new HorizontalPanel();

		HTML text = new HTML("<table><tr><td rowspan='2'>" + images.warn().getHTML() +
					"</td><td>" + "Are you sure you want to <b>permanently</b> delete folder '" + folder.getName() +
					"'?</td></tr></table>");
		text.setStyleName("gss-warnMessage");
		outer.add(text);

		// Create the 'Delete' button, along with a listener that hides the dialog
		// when the button is clicked and deletes the folder.
		Button ok = new Button("Delete", new ClickListener() {

			public void onClick(Widget sender) {
				deleteFolder();
				hide();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		Button cancel = new Button("Cancel", new ClickListener() {

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
	 * Generate an RPC request to delete a folder.
	 *
	 * @param userId the ID of the current user
	 */
	private void deleteFolder() {

		DnDTreeItem folder = (DnDTreeItem) GSS.get().getFolders().getCurrent();
		if (folder == null) {
			GSS.get().displayError("No folder was selected");
			return;
		}
		if(folder.getFolderResource() == null)
			return;

		DeleteCommand df = new DeleteCommand(folder.getFolderResource().getUri()){

			@Override
			public void onComplete() {
				TreeItem curFolder = GSS.get().getFolders().getCurrent();
				GSS.get().getFolders().updateFolder((DnDTreeItem) curFolder.getParentItem());
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("Folder not found");
					else
						GSS.get().displayError("Unable to delete folder: "+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error unable to delete folder: "+t.getMessage());
			}
		};

		DeferredCommand.addCommand(df);
	}

	@Override
	public boolean onKeyDownPreview(final char key, final int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				hide();
				deleteFolder();
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}
		return true;
	}

}
