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
import gr.ebs.gss.client.rest.resource.TagsResource;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract class, parent of all 'File properties' dialog boxes.
 *
 * @author droutsis
 */
public abstract class AbstractPropertiesDialog extends DialogBox {

	protected static final String MULTIPLE_VALUES_TEXT = "(Multiple values)";

	/**
	 * Text box with the tags associated with the file
	 */
	protected TextBox tags = new TextBox();

	protected String initialTagText;

	/**
	 * A FlowPanel with all user tags
	 */
	protected FlowPanel allTagsContent;


	protected TabPanel inner = null;

	/**
	 * The widget's constructor.
	 *
	 */
	public AbstractPropertiesDialog() {

		// Enable IE selection for the dialog (must disable it upon closing it)
		GSS.enableIESelection();

		setAnimationEnabled(true);

	}

	/**
	 * Retrieves all user tags from the server and updates the FlowPanel
	 *
	 * @param userId
	 */
	protected void updateTags() {
		GetCommand<TagsResource> tc = new GetCommand<TagsResource>(TagsResource.class, GSS.get().getCurrentUserResource().getTagsPath()) {

			@Override
			public void onComplete() {
				allTagsContent.clear();
				TagsResource tagr = getResult();
				List<String> userTags = tagr.getTags();
				Iterator t = userTags.iterator();
				while (t.hasNext()) {
					final Button tag = new Button((String) t.next(), new ClickListener() {

						public void onClick(Widget sender) {
							String existing = tags.getText();
							if (MULTIPLE_VALUES_TEXT.equals(existing)) existing = "";
							String newTag = ((Button) sender).getText().trim();
							// insert the new tag only if it is not in the list
							// already
							if (existing.indexOf(newTag + ",") == -1 && !existing.trim().endsWith(newTag))
								tags.setText(existing.trim() + (existing.length() > 0 ? ", " : "") + newTag);
						}
					});
					allTagsContent.add(tag);
				}
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch user tags");
			}
		};
		DeferredCommand.addCommand(tc);

	}

	/**
	 * Accepts any change and updates the file
	 *
	 */
	protected abstract void accept();

	@Override
	@SuppressWarnings("fallthrough")
	public boolean onKeyDownPreview(char key, int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				accept();
			case KeyboardListener.KEY_ESCAPE:
				closeDialog();
				break;
		}

		return true;
	}


	public void selectTab(int _tab) {
		inner.selectTab(_tab);
	}


	/**
	 * Enables IE selection prevention and hides the dialog
	 * (we disable the prevention on creation of the dialog)
	 */
	public void closeDialog() {
		GSS.preventIESelection();
		hide();
	}

}
