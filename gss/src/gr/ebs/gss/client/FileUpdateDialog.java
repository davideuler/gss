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

import gr.ebs.gss.client.domain.UploadStatusDTO;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author kman
 */
public class FileUpdateDialog extends DialogBox implements Updateable {

	/**
	 * The path info portion of the URL that provides the file upload service.
	 */
	private static final String FILE_UPLOAD_PATH = "/gss/fileUpload";

	private int prgBarInterval = 1500;

	private ProgressBar progressBar;

	private RepeatingTimer repeater = new RepeatingTimer(this, prgBarInterval);

	/**
	 * The Form element that performs the file upload.
	 */
	final FormPanel form = new FormPanel();

	private String fileName;
	/**
	 * The widget's constructor.
	 */
	public FileUpdateDialog() {
		// Use this opportunity to set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
		form.setAction(FILE_UPLOAD_PATH);
		// Because we're going to add a FileUpload widget, we'll need to set the
		// form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		final VerticalPanel panel = new VerticalPanel();
		form.setWidget(panel);

		// Add an informative label with the folder name.
		final Object selection = GSS.get().getFolders().getCurrent().getUserObject();
		final FolderResource folder = (FolderResource) selection;

		FileResource selectedFile = (FileResource) GSS.get().getCurrentSelection();
		// Add the folder ID in a hidden field.
		final Hidden folderId = new Hidden("folderId", folder.getPath().toString());
		panel.add(folderId);
		final Hidden userId = new Hidden("userId", GSS.get().getCurrentUserResource().getUsername());
		panel.add(userId);

		final Hidden fileHeaderId = new Hidden("fileHeaderId", selectedFile.getPath().toString());
		panel.add(fileHeaderId);

		// Create a FileUpload widget.
		final FileUpload upload = new FileUpload();
		upload.setName("file");

		final Grid generalTable = new Grid(2, 2);
		generalTable.setText(0, 0, "Folder");
		generalTable.setText(1, 0, "File");
		generalTable.setText(0, 1, folder.getName());
		generalTable.setWidget(1, 1, upload);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.setCellSpacing(4);

		panel.add(generalTable);

		// Create a panel to hold the buttons.
		final HorizontalPanel buttons = new HorizontalPanel();

		// Create the 'upload' button, along with a listener that submits the
		// form.
		final Button submit = new Button("Update", new ClickListener() {

			public void onClick(Widget sender) {
				form.submit();
			}
		});
		buttons.add(submit);
		buttons.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
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
				// This event is fired just before the form is submitted. We can
				// take
				// this opportunity to perform validation.


				if (upload.getFilename().length() == 0) {
					GSS.get().displayError("You must select a file!");

					event.setCancelled(true);
					hide();
				} else{
					fileName = getFilename(upload.getFilename());
					submit.setEnabled(false);
					repeater.start();
					progressBar.setVisible(true);
				}
			}

			public void onSubmitComplete(final FormSubmitCompleteEvent event) {
				// When the form submission is successfully completed, this
				// event is
				// fired. Assuming the service returned a response of type
				// text/html,
				// we can get the result text here (see the FormPanel
				// documentation for
				// further explanation).
				final String results = event.getResults();
				// Unfortunately the results are never empty, even in
				// the absense of errors, so we have to check for '<pre></pre>'.
				if (!results.equalsIgnoreCase("<pre></pre>")) {
					GWT.log(results, null);
					GSS.get().displayError(results);
				}
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
		panel.addStyleName("gss-DialogBox");
		addStyleName("gss-DialogBox");
		setWidget(form);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#onKeyDownPreview(char, int)
	 */
	public boolean onKeyDownPreview(final char key, final int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				form.submit();
				break;
			case KeyboardListener.KEY_ESCAPE:
				repeater.finish();
				hide();
				break;
		}

		return true;
	}

	class RepeatingTimer extends Timer {

		private Updateable updateable;

		private int interval = 1500;

		private boolean running = true;

		RepeatingTimer(Updateable updateable, int interval) {
			this.updateable = updateable;
			this.interval = interval;
		}

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

		public void setInterval(int interval) {
			if (this.interval != interval) {
				this.interval = interval;
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

		final GSSServiceAsync service = GSS.get().getRemoteService();
		service.getUploadStatus(GSS.get().getCurrentUserResource().getUsername(), fileName, new AsyncCallback() {

			public void onFailure(Throwable arg0) {

			}

			public void onSuccess(Object arg0) {
				UploadStatusDTO res = (UploadStatusDTO) arg0;
				if (res != null)
					progressBar.setProgress(res.percent());

			}

		});

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
	private String getFilename(String name) {
		int pathSepIndex = name.lastIndexOf("\\");
		if (pathSepIndex == -1) {
			pathSepIndex = name.lastIndexOf("/");
			if (pathSepIndex == -1)
				return name;
		}
		return name.substring(pathSepIndex + 1);
	}

}
