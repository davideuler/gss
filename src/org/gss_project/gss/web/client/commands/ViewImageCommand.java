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
package org.gss_project.gss.web.client.commands;

import org.gss_project.gss.web.client.FileMenu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class ViewImageCommand implements Command {

	final FileMenu.Images newImages;

	private PopupPanel containerPanel;

	private String imageDownloadURL;

	private Label errorLabel = new Label();

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 */
	public ViewImageCommand(PopupPanel _containerPanel, final FileMenu.Images _newImages, String _imageDownloadURL) {
		containerPanel = _containerPanel;
		newImages = _newImages;
		imageDownloadURL = _imageDownloadURL;
	}

	@Override
	public void execute() {
		containerPanel.hide();

		final Image image = new Image();
		// Hook up a load handler, so that we can be informed if the image
		// fails to load.
	    image.addLoadHandler(new LoadHandler() {

			@Override
			public void onLoad(LoadEvent event) {
				errorLabel.setText("");
			}
		});
		image.addErrorHandler(new ErrorHandler() {

			@Override
			public void onError(ErrorEvent event) {
				errorLabel.setText("An error occurred while loading.");
			}
		});
	    image.setUrl(imageDownloadURL);
	    final DialogBox imagePopup = new DialogBox(true, true);
	    imagePopup.setAnimationEnabled(true);
	    imagePopup.setText("Showing image in actual size");
	    VerticalPanel imageViewPanel = new VerticalPanel();
	    errorLabel.setText("loading image...");
	    imageViewPanel.add(errorLabel);
	    imageViewPanel.add(image);
	    imagePopup.setWidget(imageViewPanel);
	    image.setTitle("Click to close");
	    image.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
	    		imagePopup.hide();
	    	}
	    });
	    imagePopup.setPopupPosition(0, 0);
	    imagePopup.show();
	}
}
