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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.desktop.Desktop;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.desktop.OpenFilesHandler;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.google.gwt.gears.client.httprequest.ProgressEvent;
import com.google.gwt.gears.client.httprequest.ProgressHandler;
import com.google.gwt.gears.client.httprequest.RequestCallback;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'File upload' dialog box implementation with Google Gears support.
 */
public class FileUploadGearsDialog extends FileUploadDialog implements Updateable {

	protected final Factory factory = Factory.getInstance();

	/**
	 * The array of files to upload.
	 */
	private File[] fileObjects;

	/**
	 * A list of files to upload, created from files array. Used to signal
	 * finished state when empty.
	 */
	protected List<File> selectedFiles = new ArrayList<File>();

	/**
	 * The list of progress bars for individual files.
	 */
	protected List<ProgressBar> progressBars = new ArrayList<ProgressBar>();

	private Button browse;

	private Button submit;

	private FlexTable generalTable;

	private Map<String, FileResource> toRename;

	protected List<HttpRequest> requests = new ArrayList<HttpRequest>();

	/**
	 * The widget's constructor.
	 */
	public FileUploadGearsDialog() {
		// Set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
		// Create a panel to hold all of the dialog widgets.
		VerticalPanel panel = new VerticalPanel();
		final HTML info = new HTML("You may select one or more files to upload.");
		info.addStyleName("gss-uploadNote");
		panel.add(info);
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
		submit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				prepareAndSubmit();
			}
		});
		submit.setEnabled(false);
		buttons.add(submit);
		buttons.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				cancelUpload();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gss-DialogBox");

		browse.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Desktop desktop = factory.createDesktop();
				desktop.openFiles(new OpenFilesHandler() {

					public void onOpenFiles(OpenFilesEvent ofevent) {
						fileObjects = ofevent.getFiles();
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
	 * Cancels the file upload.
	 */
	private void cancelUpload() {
		for (HttpRequest request: requests)
			request.abort();
		hide();
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
		for (File file: selectedFiles)
			if (!canContinue(file)) {
				app.displayError("The file name " + file.getName() +
							" already exists in this folder");
				hide();
				return;
			}
		submit.setEnabled(false);
		browse.setVisible(false);
		List<String> toUpdate = new ArrayList<String>();
		toRename = new HashMap<String, FileResource>();
		for (File file: selectedFiles) {
			String fname = getFilename(file.getName());
			if (getFileForName(fname) == null) {
				// We are going to create a file, so we check to see if there is a
				// trashed file with the same name.
				FileResource same = null;
				for (FileResource fres : folder.getFiles())
					if (fres.isDeleted() && fres.getName().equals(fname))
						same = fres;
				// In that case add it to the list of files to rename.
				if (same != null)
					toRename.put(getBackupFilename(fname), same);
			} else
				// If we are updating a file add it to the list of files to update.
				toUpdate.add(fname);
		}

		if (!toUpdate.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (String name: toUpdate)
				sb.append(name).append("<br/>");
			// We are going to update existing files, so show a confirmation dialog.
			ConfirmationDialog confirm = new ConfirmationDialog("Are you sure " +
					"you want to update the following files?<br/><i>" + sb +
					"</i>", "Update") {

				@Override
				public void cancel() {
					hide();
				}

				@Override
				public void confirm() {
					confirmRename();
				}

			};
			confirm.center();
		} else
			confirmRename();
	}

	/**
	 * Confirm the renames of synonymous files already in the trash.
	 */
	private void confirmRename() {
		if (!toRename.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (FileResource file: toRename.values())
				sb.append(file.getName()).append("<br/>");
			ConfirmationDialog confirm = new ConfirmationDialog("Files " +
					"with the following names already exist in the trash. If" +
					" you continue,<br/>the trashed files will be renamed " +
					"automatically for you:<br/><i>" + sb + "</i>", "Continue") {

				@Override
				public void cancel() {
					hide();
				}

				@Override
				public void confirm() {
					updateTrashedFiles();
				}

			};
			confirm.center();
		} else
			uploadFiles();
	}

	/**
	 * Rename the conflicting trashed files with the supplied new names.
	 */
	private void updateTrashedFiles() {
		for (final String name: toRename.keySet()) {
			JSONObject json = new JSONObject();
			json.put("name", new JSONString(name));
			PostCommand cf = new PostCommand(toRename.get(name).getUri() + "?update=", json.toString(), 200) {

				@Override
				public void onComplete() {
					toRename.remove(name);
					uploadFiles();
				}

				@Override
				public void onError(Throwable t) {
					GSS app = GSS.get();
					GWT.log("", t);
					if (t instanceof RestException) {
						int statusCode = ((RestException) t).getHttpStatusCode();
						if (statusCode == 405)
							app.displayError("You don't have the necessary permissions");
						else if (statusCode == 404)
							app.displayError("User in permissions does not exist");
						else if (statusCode == 409)
							app.displayError("A file with the same name already exists");
						else if (statusCode == 413)
							app.displayError("Your quota has been exceeded");
						else
							app.displayError("Unable to modify file:" + ((RestException) t).getHttpStatusText());
					} else
						app.displayError("System error modifying file:" + t.getMessage());
				}

			};
			DeferredCommand.addCommand(cf);
		}
	}

	/**
	 * Checks if the renaming step for already trashed files is complete and
	 * starts file uploads.
	 */
	private void uploadFiles() {
		if (!toRename.isEmpty()) return;
		for (int i = 0; i< fileObjects.length; i++) {
			final int index = i;
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					doSend(fileObjects[index], index);
				}
			});
		}
	}

	/**
	 * Perform the HTTP request to upload the specified file.
	 */
	protected void doSend(final File file, final int index) {
		final GSS app = GSS.get();
		HttpRequest request = factory.createHttpRequest();
		requests.add(request);
		String method = "PUT";

		String path;
		final String filename = getFilename(file.getName());
		path = folder.getUri();
		if (!path.endsWith("/"))
			path = path + "/";
		path = path + encode(filename);

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
				int state = req.getReadyState();
				if (state != 4) return;
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
					case 405:
						app.displayError("You don't have permission to " +
								"upload file " + filename);
						break;
					case 409:
						app.displayError("A folder with the name " + filename +
								" already exists at this level");
						break;
					case 413:
						app.displayError("There is not enough free space " +
								"available for uploading " + filename);
						break;
					default:
						app.displayError("Error uploading file " + filename +
									": " + req.getStatus());
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
	protected void finish() {
		if (!selectedFiles.isEmpty()) return;
		hide();
		GSS.get().showFileList(true);
		GSS.get().getStatusPanel().updateStats();
	}

	/**
	 * Same as URL.encode, but also encode apostrophe since browsers aren't
	 * consistent about it (FF encodes, IE does not).
	 */
	protected String encode(String decodedURL) {
		String retv = decodedURL.replaceAll("@", "_"); // Replace bad character
		retv = URL.encodeComponent(retv);
		retv = retv.replaceAll("'", "%27");
		return retv;
	}

}
