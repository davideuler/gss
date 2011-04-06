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
package gr.ebs.gss.web.client;

import gr.ebs.gss.web.client.components.TristateCheckBox;
import gr.ebs.gss.web.client.rest.MultiplePostCommand;
import gr.ebs.gss.web.client.rest.RestException;
import gr.ebs.gss.web.client.rest.resource.FileResource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'Multiple file properties' dialog box implementation.
 *
 * @author droutsis
 */
public class FilesPropertiesDialog extends AbstractPropertiesDialog {

	private final TristateCheckBox versionedCheck;

	private final List<FileResource> files;

	private Boolean initialVersioned;


	/**
	 * The widget's constructor.
	 *
	 * @param _files
	 */
	public FilesPropertiesDialog(final List<FileResource> _files) {
		super();

		files = _files;
		int versionedNum = 0;
		for (FileResource fr : files)
			if (fr.isVersioned()) versionedNum++;
		Boolean versioned = null;
		if (versionedNum==0) versioned = false;
		if (versionedNum==files.size()) versioned = true;
		initialVersioned = versioned;
		versionedCheck = new TristateCheckBox(versioned);

		// Set the dialog's caption.
		setText("Files properties");

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel.
		inner = new DecoratedTabPanel();
		inner.setAnimationEnabled(true);
		final VerticalPanel generalPanel = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();
		final VerticalPanel verPanel = new VerticalPanel();
		final HorizontalPanel vPanel = new HorizontalPanel();

		inner.add(generalPanel, "General");
		inner.add(verPanel, "Versions");
		inner.selectTab(0);

		final FlexTable generalTable = new FlexTable();
		generalTable.setText(0, 0, String.valueOf(files.size())+" files selected");
		generalTable.setText(1, 0, "Folder");
		generalTable.setText(2, 0, "Tags");
		FileResource firstFile = files.get(0);
		if(firstFile.getFolderName() != null)
			generalTable.setText(1, 1, firstFile.getFolderName());
		else
			generalTable.setText(1, 1, "-");

		// Find if tags are identical
		List<String> tagsList = files.get(0).getTags();
		List<String> tagss;
		for (int i=1; i<files.size(); i++) {
			tagss = files.get(i).getTags();
			if (tagsList.size() != tagss.size() || !tagsList.containsAll(tagss)) {
				tagsList = null;
				break;
			}
		}
		// Get the tags.
		StringBuffer tagsBuffer = new StringBuffer();
		if (tagsList==null)
			tagsBuffer.append(MULTIPLE_VALUES_TEXT);
		else {
			Iterator i = tagsList.iterator();
			while (i.hasNext()) {
				String tag = (String) i.next();
				tagsBuffer.append(tag).append(", ");
			}
			if (tagsBuffer.length() > 1)
				tagsBuffer.delete(tagsBuffer.length() - 2, tagsBuffer.length() - 1);
		}
		initialTagText = tagsBuffer.toString();
		tags.setText(initialTagText);
		tags.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				if (MULTIPLE_VALUES_TEXT.equals(tags.getText()))
					tags.setText("");
			}
		}
		);

		generalTable.setWidget(2, 1, tags);
		generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
		generalTable.setCellSpacing(4);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				accept();
				closeDialog();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gss-TabPanelBottom");

		generalPanel.add(generalTable);

		// Asynchronously retrieve the tags defined by this user.
		DeferredCommand.addCommand(new Command() {

			@Override
			public void execute() {
				updateTags();
			}
		});

		DisclosurePanel allTags = new DisclosurePanel("All tags");
		allTagsContent = new FlowPanel();
		allTags.setContent(allTagsContent);
		generalPanel.add(allTags);
		generalPanel.setSpacing(4);


		vPanel.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.setSpacing(8);
		vPanel.addStyleName("gss-TabPanelBottom");
		vPanel.add(new Label("Versioned"));

		vPanel.add(versionedCheck);
		verPanel.add(vPanel);
		outer.add(inner);
		outer.add(buttons);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		outer.addStyleName("gss-TabPanelBottom");

		focusPanel.setFocus(true);
		setWidget(outer);
	}


	/**
	 * Accepts any change and updates the file
	 *
	 */
	@Override
	protected void accept() {
		JSONObject json = new JSONObject();
		if ( versionedCheck.getState()!=null && !versionedCheck.getState().equals(initialVersioned) )
				json.put("versioned", JSONBoolean.getInstance(versionedCheck.getState()));

		JSONArray taga = new JSONArray();
		int i = 0;
		String tagText = tags.getText();
		if (!MULTIPLE_VALUES_TEXT.equals(tagText) && !initialTagText.equals(tagText)) {
			String[] tagset = tagText.split(",");
			for (String t : tagset) {
				JSONString to = new JSONString(t);
				taga.set(i, to);
				i++;
			}
			json.put("tags", taga);
		}
		String jsonString = json.toString();
		if(jsonString.equals("{}")){
			GWT.log("NO CHANGES", null);
			return;
		}
		final List<String> fileIds = new ArrayList<String>();
		for(FileResource f : files)
			fileIds.add(f.getUri()+"?update=");
		MultiplePostCommand rt = new MultiplePostCommand(fileIds.toArray(new String[0]), jsonString, 200){

			@Override
			public void onComplete() {
				GSS.get().getTreeView().refreshCurrentNode(false);
			}

			@Override
			public void onError(String p, Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("File does not exist");
					else if(statusCode == 409)
						GSS.get().displayError("A file with the same name already exists");
					else if(statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to modify file::"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error modifying file:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(rt);
	}


}
