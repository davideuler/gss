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

import gr.ebs.gss.client.rest.PostCommand;
import gr.ebs.gss.client.rest.RestCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.desktop.Desktop;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.desktop.OpenFilesHandler;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.google.gwt.gears.client.httprequest.ProgressEvent;
import com.google.gwt.gears.client.httprequest.ProgressHandler;
import com.google.gwt.gears.client.httprequest.RequestCallback;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'File upload' dialog box implementation with Google Gears support.
 */
public class FileUploadGearsDialog extends FileUploadDialog implements Updateable {

	private final Factory factory = Factory.getInstance();

	/**
	 * The array of files to upload.
	 */
	private File[] fileObjects;

	/**
	 * A list of files to upload, created from files array. Used to signal
	 * finished state when empty.
	 */
	private List<File> selectedFiles = new ArrayList<File>();

	/**
	 * The list of progress bars for individual files.
	 */
	private List<ProgressBar> progressBars = new ArrayList<ProgressBar>();

	private Button browse;

	private Button submit;

	private FlexTable generalTable;

	/**
	 * The widget's constructor.
	 */
	public FileUploadGearsDialog() {
		// Set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
		// Create a panel to hold all of the dialog widgets.
		VerticalPanel panel = new VerticalPanel();
		// Add an informative label with the folder name.
		Object selection = GSS.get().getFolders().getCurrent().getUserObject();
		folder = (FolderResource) selection;

		browse = new Button("Browse...");

		HorizontalPanel fileUploadPanel = new HorizontalPanel();
		fileUploadPanel.add(browse);

		generalTable = new FlexTable();
		generalTable.setText(0, 0, "Folder");
		generalTable.setText(1, 0, "File");
		generalTable.setText(0, 1, folder.getName());
		generalTable.setWidget(1, 1, fileUploadPanel);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.setCellSpacing(4);

		panel.add(generalTable);

		// Create a panel to hold the buttons.
		HorizontalPanel buttons = new HorizontalPanel();

		submit = new Button("Upload");
		submit.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				prepareAndSubmit();
			}
		});
		submit.setEnabled(false);
		buttons.add(submit);
		buttons.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_CENTER);
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
		buttons.addStyleName("gss-DialogBox");

		browse.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				Desktop desktop = factory.createDesktop();
				desktop.openFiles(new OpenFilesHandler() {

					public void onOpenFiles(OpenFilesEvent event) {
						fileObjects = event.getFiles();
						selectedFiles.addAll(Arrays.asList(fileObjects));
						for (int i = 0; i< selectedFiles.size(); i++) {
							generalTable.setText(i+1, 0, "File");
							generalTable.setText(i+1, 1, selectedFiles.get(i).getName());
							ProgressBar progress = new ProgressBar(20, 0);
							generalTable.setWidget(i+1, 2, progress);
							progressBars.add(progress);
							generalTable.getCellFormatter().setStyleName(i+1, 0, "props-labels");
							generalTable.getCellFormatter().setStyleName(i+1, 1, "props-values");
						}
						submit.setEnabled(true);
					}
				});
			}
		});

		panel.add(buttons);
		panel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		panel.addStyleName("gss-DialogBox");
		addStyleName("gss-DialogBox");
		setWidget(panel);
	}

	/**
	 * Check whether the specified file name exists in the selected folder.
	 */
	private boolean canContinue(File file) {
		String fileName = getFilename(file.getName());
		if (getFileForName(fileName) == null)
			// For file creation, check to see if the file already exists.
			for (FileResource fileRes : files)
				if (!fileRes.isDeleted() && fileRes.getName().equals(fileName))
					return false;
		return true;
	}

	@Override
	public void prepareAndSubmit() {
		GSS app = GSS.get();
		if (selectedFiles.size() == 0) {
			app.displayError("You must select a file!");
			hide();
			return;
		}
		for(File file: selectedFiles)
			if (!canContinue(file)) {
				app.displayError("The file name " + file.getName() +
							" already exists in this folder");
				hide();
				return;
			}
		submit.setEnabled(false);
		browse.setVisible(false);
		final String fname = getFilename(selectedFiles.get(0).getName());
		if (getFileForName(fname) == null) {
			// We are going to create a file, so we check to see if there is a
			// trashed file with the same name.
			FileResource same = null;
			for (FileResource fres : folder.getFiles())
				if (fres.isDeleted() && fres.getName().equals(fname))
					same = fres;
			if (same == null)
				uploadFiles();
			else {
				final FileResource sameFile = same;
				GWT.log("Same deleted file", null);
				ConfirmationDialog confirm = new ConfirmationDialog("A file " +
						"with the same name exists in the trash. If you " +
						"continue,<br/>the trashed file  '" + fname +
						"' will be renamed automatically for you.", "Continue") {

					@Override
					public void cancel() {
						hide();
					}

					@Override
					public void confirm() {
						updateTrashedFile(getBackupFilename(fname), sameFile);
					}

				};
				confirm.center();
			}
		} else {
			// We are going to update an existing file, so show a confirmation dialog.
			ConfirmationDialog confirm = new ConfirmationDialog("Are you sure " +
					"you want to update " + fname + "?", "Update"){

				@Override
				public void cancel() {
					hide();
				}

				@Override
				public void confirm() {
					uploadFiles();
				}

			};
			confirm.center();
		}
	}

	private void updateTrashedFile(String newName, FileResource trashedFile) {
		JSONObject json = new JSONObject();
		json.put("name", new JSONString(newName));
		PostCommand cf = new PostCommand(trashedFile.getUri() + "?update=", json.toString(), 200) {

			@Override
			public void onComplete() {
				uploadFiles();
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if (t instanceof RestException) {
					int statusCode = ((RestException) t).getHttpStatusCode();
					if (statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if (statusCode == 404)
						GSS.get().displayError("User in permissions does not exist");
					else if (statusCode == 409)
						GSS.get().displayError("A file with the same name already exists");
					else if (statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to modify file:" +((RestException)t).getHttpStatusText());
				} else
					GSS.get().displayError("System error modifying file:" + t.getMessage());
			}

		};
		DeferredCommand.addCommand(cf);
	}

	/**
	 * Schedule the PUT requests to upload the files.
	 */
	private void uploadFiles() {
		for (int i = 0; i< fileObjects.length; i++) {
			final int index = i;
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					doPut(fileObjects[index], index);
				}
			});
		}
	}

	/**
	 * Perform the HTTP PUT requests to upload the specified file.
	 */
	protected void doPut(final File file, final int index) {
		GSS app = GSS.get();
		HttpRequest request = factory.createHttpRequest();
		String method = "PUT";

		String path;
		String filename = getFilename(file.getName());
		FileResource selectedResource = getFileForName(filename);
		if (selectedResource == null ) {
			// We are going to create a file.
			path = folder.getUri();
			if (!path.endsWith("/"))
				path = path + "/";
			path = path + encodeComponent(filename);
		} else
			path = selectedResource.getUri();

		String token = app.getToken();
		String resource = path.substring(app.getApiPath().length()-1, path.length());
		String date = RestCommand.getDate();
		String sig = RestCommand.calculateSig(method, date, resource, RestCommand.base64decode(token));
		request.open(method, path);
		request.setRequestHeader("X-GSS-Date", date);
		request.setRequestHeader("Authorization", app.getCurrentUserResource().getUsername() + " " + sig);
		request.setRequestHeader("Accept", "application/json; charset=utf-8");
		request.setCallback(new RequestCallback() {
			public void onResponseReceived(HttpRequest req) {
				switch(req.getStatus()) {
					case 201: // Created falls through to updated.
					case 204:
						selectedFiles.remove(file);
						finish();
						break;
					case 403:
						SessionExpiredDialog dlg = new SessionExpiredDialog();
						dlg.center();
						break;
					default:
						DisplayHelper.log(req.getStatus() + ":" + req.getStatusText());
				}
			}
		});
		request.getUpload().setProgressHandler(new ProgressHandler() {
			public void onProgress(ProgressEvent event) {
				double pcnt = (double) event.getLoaded() / event.getTotal();
				progressBars.get(index).setProgress((int) Math.floor(pcnt * 100));
			}
		});
		request.send(file.getBlob());
	}

	/**
	 * Perform the final actions after the files are uploaded.
	 */
	private void finish() {
		if (!selectedFiles.isEmpty()) return;
		hide();
		GSS.get().showFileList(true);
		GSS.get().getStatusPanel().updateStats();
	}
}
