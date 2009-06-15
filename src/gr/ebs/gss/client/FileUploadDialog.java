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

import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.PostCommand;
import gr.ebs.gss.client.rest.RestCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.UploadStatusResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'File upload' dialog box implementation.
 */
public class FileUploadDialog extends DialogBox implements Updateable {

	protected int prgBarInterval = 1500;

	protected ProgressBar progressBar;

	protected RepeatingTimer repeater = new RepeatingTimer(this, prgBarInterval);

	public static final boolean DONE = true;

	/**
	 * The Form element that performs the file upload.
	 */
	private final FormPanel form = new FormPanel();

	private final FileUpload upload = new FileUpload();

	protected final Label filenameLabel = new Label("");

	protected List<FileResource> files;

	protected boolean cancelEvent = false;

	protected String fileNameToUse;

	protected FolderResource folder;

	/**
	 * The widget's constructor.
	 */
	public FileUploadDialog() {
		// Set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
		// Since we're going to add a FileUpload widget, we'll need to set the
		// form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		VerticalPanel panel = new VerticalPanel();
		form.setWidget(panel);
		final Hidden date = new Hidden("Date", "");
		panel.add(date);
		final Hidden auth = new Hidden("Authorization", "");
		panel.add(auth);
		// Add an informative label with the folder name.
		Object selection = GSS.get().getFolders().getCurrent().getUserObject();
		folder = (FolderResource) selection;
		upload.setName("file");
		filenameLabel.setText("");
		filenameLabel.setVisible(false);
		filenameLabel.setStyleName("props-labels");
		HorizontalPanel fileUloadPanel = new HorizontalPanel();
		fileUloadPanel.add(filenameLabel);
		fileUloadPanel.add(upload);
		Grid generalTable = new Grid(2, 2);
		generalTable.setText(0, 0, "Folder");
		generalTable.setText(1, 0, "File");
		generalTable.setText(0, 1, folder.getName());
		generalTable.setWidget(1, 1, fileUloadPanel);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.setCellSpacing(4);

		panel.add(generalTable);

		// Create a panel to hold the buttons.
		HorizontalPanel buttons = new HorizontalPanel();

		// Create the 'upload' button, along with a listener that submits the
		// form.
		final Button submit = new Button("Upload", new ClickListener() {
			public void onClick(Widget sender) {
				prepareAndSubmit();
			}
		});
		buttons.add(submit);
		buttons.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				repeater.finish();
				hide();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gss-DialogBox");

		// Add an event handler to the form.
		form.addFormHandler(new FormHandler() {

			public void onSubmit(final FormSubmitEvent event) {
				GSS app = GSS.get();
				// This event is fired just before the form is submitted. We can
				// take this opportunity to perform validation.
				if (upload.getFilename().length() == 0) {
					app.displayError("You must select a file!");
					event.setCancelled(true);
					hide();
				} else {

					canContinue();
					GWT.log("Cancel:" + cancelEvent, null);
					if (cancelEvent) {
						cancelEvent = false;
						app.displayError("The specified file name already exists in this folder");
						event.setCancelled(true);
						hide();
					} else {

						fileNameToUse = getFilename(upload.getFilename());
						String apath;
						FileResource selectedFile = getFileForName(fileNameToUse);
						if (selectedFile == null ) {
							//we are going to create a file
							apath = folder.getUri();
							if (!apath.endsWith("/"))
								apath = apath + "/";
							apath = apath + encodeComponent(fileNameToUse);
						} else
							apath = selectedFile.getUri();
						form.setAction(apath);
						String dateString = RestCommand.getDate();
						String resource = apath.substring(app.getApiPath().length() - 1, apath.length());
						String sig = RestCommand.calculateSig("POST", dateString, resource, RestCommand.base64decode(app.getToken()));
						date.setValue(dateString);
						auth.setValue(app.getCurrentUserResource().getUsername() + " " + sig);
						GWT.log("FolderPATH:" + folder.getUri(), null);
						submit.setEnabled(false);
						upload.setVisible(false);
						filenameLabel.setText(fileNameToUse);
						filenameLabel.setVisible(true);
						repeater.start();
						progressBar.setVisible(true);
					}
				}
			}

			public void onSubmitComplete(final FormSubmitCompleteEvent event) {
				// When the form submission is successfully completed, this
				// event is fired. Assuming the service returned a response
				// of type text/html, we can get the result text here (see
				// the FormPanel documentation for further explanation).
				String results = event.getResults();

				// Unfortunately the results are never empty, even in
				// the absense of errors, so we have to check for '<pre></pre>'.
				if (!results.equalsIgnoreCase("<pre></pre>")) {
					GWT.log(results, null);
					GSS.get().displayError(results);
				}
				progressBar.setProgress(100);
				repeater.finish();
				hide();
				GSS.get().showFileList(true);
				GSS.get().getStatusPanel().updateStats();
			}
		});

		panel.add(buttons);
		progressBar = new ProgressBar(50, ProgressBar.SHOW_TIME_REMAINING);
		panel.add(progressBar);
		progressBar.setVisible(false);
		panel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		panel.setCellHorizontalAlignment(progressBar, HasHorizontalAlignment.ALIGN_CENTER);
		panel.addStyleName("gss-DialogBox");
		addStyleName("gss-DialogBox");
		setWidget(form);
	}

	@Override
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				prepareAndSubmit();
				break;
			case KeyboardListener.KEY_ESCAPE:
				repeater.finish();
				hide();
				break;
		}

		return true;
	}


	public void prepareAndSubmit(){
		final String fname = getFilename(upload.getFilename());
		if (getFileForName(fname) == null) {
			//we are going to create a file, so we check to see if there is a trashed file with the same name
			FileResource same = null;
			for (FileResource fres : folder.getFiles())
				if (fres.isDeleted() && fres.getName().equals(fname))
					same = fres;
			if (same == null)
				form.submit();
			else {
				final FileResource sameFile = same;
				GWT.log("Same deleted file", null);
				ConfirmationDialog confirm = new ConfirmationDialog("A file with " +
						"the same name exists in the trash. If you continue,<br/>the trashed " +
						"file  '" + fname + "' will be renamed automatically for you.", "Continue") {

					@Override
					public void cancel() {
						FileUploadDialog.this.hide();
					}

					@Override
					public void confirm() {
						updateTrashedFile(getBackupFilename(fname), sameFile);
					}

				};
				confirm.center();
			}
		}
		else {
			// We are going to update an existing file, so show a confirmation dialog.
			ConfirmationDialog confirm = new ConfirmationDialog("Are you sure " +
					"you want to update " + fname + "?", "Update") {

				@Override
				public void cancel() {
					FileUploadDialog.this.hide();
				}

				@Override
				public void confirm() {
					form.submit();
				}

			};
			confirm.center();
		}
	}

	/**
	 * Returns the file name from a potential full path argument. Apparently IE
	 * insists on sending the full path name of a file when uploading, forcing
	 * us to trim the extra path info. Since this is only observed on Windows we
	 * get to check for a single path separator value.
	 *
	 * @param name the potentially full path name of a file
	 * @return the file name without extra path information
	 */
	protected String getFilename(String name) {
		int pathSepIndex = name.lastIndexOf("\\");
		if (pathSepIndex == -1) {
			pathSepIndex = name.lastIndexOf("/");
			if (pathSepIndex == -1)
				return name;
		}
		return name.substring(pathSepIndex + 1);
	}

	/**
	 * Check whether the file name exists in selected folder.
	 *
	 * @return
	 */
	private boolean canContinue() {
		if (files == null)
			return false;
		String fileName = getFilename(upload.getFilename());
		if (getFileForName(fileName) == null) {
			// For file creation, check to see if the file already exists.
			GWT.log("filename to upload:" + fileName, null);
			for (FileResource dto : files) {
				GWT.log("Check:" + dto.getName() + "/" + fileName, null);
				if (!dto.isDeleted() && dto.getName().equals(fileName)) {
					cancelEvent = true;
					return true;
				}
			}
		}
		return true;
	}

	class RepeatingTimer extends Timer {

		private Updateable updateable;

		private int interval = 1500;

		private boolean running = true;

		RepeatingTimer(Updateable _updateable, int _interval) {
			updateable = _updateable;
			interval = _interval;
		}

		@Override
		public void run() {
			updateable.update();
		}

		public void start() {
			running = true;

			scheduleRepeating(interval);
		}

		public void finish() {
			running = false;
			cancel();
		}

		public int getInterval() {
			return interval;
		}

		public void setInterval(int anInterval) {
			if (interval != anInterval) {
				interval = anInterval;
				if (running) {
					finish();
					start();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see gr.ebs.gss.client.Updateable#update()
	 */
	public void update() {
		String apath = folder.getUri();
		if (!apath.endsWith("/"))
			apath = apath + "/";
		apath = apath + encodeComponent(fileNameToUse) + "?progress=" + encodeComponent(fileNameToUse);
		GetCommand eg = new GetCommand<UploadStatusResource>(UploadStatusResource.class, apath, false) {

			@Override
			public void onComplete() {
				UploadStatusResource res = getResult();
				progressBar.setProgress(res.percent());
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
			}

		};
		DeferredCommand.addCommand(eg);
	}

	protected String getBackupFilename(String filename) {
		List<FileResource> filesInSameFolder = new ArrayList<FileResource>();
		for (FileResource deleted : folder.getFiles())
			if (deleted.isDeleted())
				filesInSameFolder.add(deleted);
		int i = 1;
		for (FileResource same : filesInSameFolder)
			if (same.getName().startsWith(filename)) {
				String toCheck = same.getName().substring(filename.length(), same.getName().length());
				if (toCheck.startsWith(" ")) {
					int test = -1;
					try {
						test = Integer.valueOf(toCheck.replace(" ", ""));
					} catch (NumberFormatException e) {
						// Do nothing since string is not a number.
					}
					if (test >= i)
						i = test + 1;
				}
			}

		return filename + " " + i;
	}

	protected void updateTrashedFile(String newName, FileResource trashedFile) {
		JSONObject json = new JSONObject();
		json.put("name", new JSONString(newName));
		PostCommand cf = new PostCommand(trashedFile.getUri() + "?update=", json.toString(), 200) {

			@Override
			public void onComplete() {
				form.submit();
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

	protected FileResource getFileForName(String name){
		for (FileResource f : folder.getFiles())
			if (!f.isDeleted() && f.getName().equals(name))
				return f;
		return null;
	}


	/**
	 * Same as URL.encodeComponent, but also
	 * encode apostrophe since browsers aren't consistent about it
	 * (FF encodes, IE does not).
	 *
	 * @param decodedURLComponent
	 * @return
	 */
	protected String encodeComponent(String decodedURLComponent) {
		String retv = URL.encodeComponent(decodedURLComponent);
		retv = retv.replaceAll("'", "%27");
		return retv;
	}

	/**
	 * Modify the files.
	 *
	 * @param newFiles the files to set
	 */
	public void setFiles(List<FileResource> newFiles) {
		files = newFiles;
	}
}
