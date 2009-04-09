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
import gr.ebs.gss.client.rest.ExecuteDelete;
import gr.ebs.gss.client.rest.ExecuteMultipleDelete;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'delete file' dialog box.
 */
public class DeleteFileDialog extends DialogBox {

	/**
	 * The widget's constructor.
	 *
	 * @param images the supplied images
	 */
	public DeleteFileDialog(Images images) {
		// Set the dialog's caption.
		setText("Confirmation");
		setAnimationEnabled(true);
		Object selection = GSS.get().getCurrentSelection();
		// Create a VerticalPanel to contain the label and the buttons.
		VerticalPanel outer = new VerticalPanel();
		HorizontalPanel buttons = new HorizontalPanel();

		HTML text;
		if (selection instanceof FileResource)
			text = new HTML("<table><tr><td>" + images.warn().getHTML() + "</td><td>" + "Are you sure you want to <b>permanently</b> delete file '" + ((FileResource) selection).getName() + "'?</td></tr></table>");
		else
			text = new HTML("<table><tr><td>" + images.warn().getHTML() + "</td><td>" + "Are you sure you want to <b>permanently</b> delete the selected files?</td></tr></table>");
		text.setStyleName("gss-warnMessage");
		outer.add(text);

		// Create the 'Delete' button, along with a listener that hides the dialog
		// when the button is clicked and deletes the file.
		Button ok = new Button("Delete", new ClickListener() {

			public void onClick(Widget sender) {
				deleteFile();
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
	 * Generate an RPC request to delete a file.
	 *
	 * @param userId the ID of the current user
	 */
	private void deleteFile() {
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null) {
			GSS.get().displayError("No file was selected");
			return;
		}
		if (selection instanceof FileResource) {
			FileResource file = (FileResource) selection;

			ExecuteDelete df = new ExecuteDelete(file.getUri()){

				@Override
				public void onComplete() {
					GSS.get().getFileList().updateFileCache(true, true /*clear selection*/);
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
							GSS.get().displayError("File not found");
						else
							GSS.get().displayError("Unable to delete file: "+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error unable to delete file: "+t.getMessage());
				}
			};

			DeferredCommand.addCommand(df);
		}
		else if(selection instanceof List){
			List<FileResource> files = (List<FileResource>) selection;
			List<String> fileIds = new ArrayList<String>();
			for(FileResource f : files)
				fileIds.add(f.getUri());

			ExecuteMultipleDelete ed = new ExecuteMultipleDelete(fileIds.toArray(new String[0])){

				@Override
				public void onComplete() {
					GSS.get().showFileList(true);
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
					GSS.get().showFileList(true);
				}

				@Override
				public void onError(String path, Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File not found");
						else
							GSS.get().displayError("Unable to delete file:"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error unable to delete file:"+t.getMessage());

				}
			};

			DeferredCommand.addCommand(ed);
		}
	}

	@Override
	public boolean onKeyDownPreview(final char key, final int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				hide();
				deleteFile();
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}

		return true;
	}

}
