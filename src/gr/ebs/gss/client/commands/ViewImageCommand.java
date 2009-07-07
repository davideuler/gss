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
package gr.ebs.gss.client.commands;

import gr.ebs.gss.client.FileMenu;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


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

	public void execute() {
		containerPanel.hide();

		final Image image = new Image();
		// Hook up a load listener, so that we can be informed if the image fails
	    // to load.
	    image.addLoadListener(new LoadListener() {
	    	public void onError(Widget sender) {
	    		errorLabel.setText("An error occurred while loading.");
	    	}

	    	public void onLoad(Widget sender) {
	    	}
	    });
	    image.setUrl(imageDownloadURL);
	    //final PopupPanel imagePopup = new PopupPanel(true);
	    final DialogBox imagePopup = new DialogBox(true, true);
	    imagePopup.setAnimationEnabled(true);
	    imagePopup.setText("Showing image in actual size");
	    VerticalPanel imageViewPanel = new VerticalPanel();
	    imageViewPanel.add(errorLabel);
	    imageViewPanel.add(image);
	    imagePopup.setWidget(imageViewPanel);
	    image.setTitle("Click to close");
	    image.addClickListener(new ClickListener() {
	    	public void onClick(Widget sender) {
	    		imagePopup.hide();
	    	}
	    });
	    imagePopup.setPopupPosition(0, 0);
	    imagePopup.show();
	}
}
