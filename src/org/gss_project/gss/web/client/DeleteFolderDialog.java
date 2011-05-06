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
package org.gss_project.gss.web.client;

import org.gss_project.gss.web.client.MessagePanel.Images;
import org.gss_project.gss.web.client.rest.DeleteCommand;
import org.gss_project.gss.web.client.rest.RestException;
import org.gss_project.gss.web.client.rest.resource.FolderResource;
import org.gss_project.gss.web.client.rest.resource.RestResource;
import org.gss_project.gss.web.client.rest.resource.RestResourceWrapper;
import org.gss_project.gss.web.client.rest.resource.TrashFolderResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

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
		FolderResource folder = ((RestResourceWrapper) GSS.get().getTreeView().getSelection()).getResource();
		// Create a VerticalPanel to contain the HTML label and the buttons.
		VerticalPanel outer = new VerticalPanel();
		HorizontalPanel buttons = new HorizontalPanel();

		HTML text = new HTML("<table><tr><td rowspan='2'>" + AbstractImagePrototype.create(images.warn()).getHTML() +
					"</td><td>" + "Are you sure you want to <b>permanently</b> delete folder '" + folder.getName() +
					"'?</td></tr></table>");
		text.setStyleName("gss-warnMessage");
		outer.add(text);

		// Create the 'Delete' button, along with a listener that hides the dialog
		// when the button is clicked and deletes the folder.
		Button ok = new Button("Delete", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteFolder();
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

	/**
	 * Generate an RPC request to delete a folder.
	 *
	 * @param userId the ID of the current user
	 */
	private void deleteFolder() {
		RestResource folder = GSS.get().getTreeView().getSelection();
		if (folder == null) {
			GSS.get().displayError("No folder was selected");
			return;
		}
		if(!(folder instanceof RestResourceWrapper))
			return;

		DeleteCommand df = new DeleteCommand(folder.getUri()){

			@Override
			public void onComplete() {
				FolderResource fres = ((RestResourceWrapper) GSS.get().getTreeView().getSelection()).getResource();
				if((RestResourceWrapper) GSS.get().getTreeView().getSelection() instanceof TrashFolderResource)
					GSS.get().getTreeView().updateTrashNode();
				else
					GSS.get().getTreeView().updateNodeChildrenForRemove(fres.getParentURI());
				GSS.get().getTreeView().clearSelection();
				GSS.get().showFileList(true);
				
				GSS.get().getStatusPanel().updateStats();
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
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					hide();
					deleteFolder();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}

}
