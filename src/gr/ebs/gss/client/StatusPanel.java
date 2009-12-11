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
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.QuotaHolder;
import gr.ebs.gss.client.rest.resource.UserResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * The panel that displays a status bar with quota information.
 */
public class StatusPanel extends Composite {
	public static final boolean DONE = false;
	private HTML fileCountLabel = new HTML("");
	private HTML fileSizeLabel = new HTML("");
	private HTML quotaIcon = new HTML("");
	private HTML quotaLabel = new HTML("");
	private HTML currentlyShowingLabel = new HTML("");

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ImageBundle {

		@Resource("gr/ebs/gss/resources/windowlist.png")
		AbstractImagePrototype totalFiles();

		@Resource("gr/ebs/gss/resources/database.png")
		AbstractImagePrototype totalSize();

		@Resource("gr/ebs/gss/resources/redled.png")
		AbstractImagePrototype redSize();

		@Resource("gr/ebs/gss/resources/greenled.png")
		AbstractImagePrototype greenSize();

		@Resource("gr/ebs/gss/resources/yellowled.png")
		AbstractImagePrototype yellowSize();
	}

	private final Images images;

	/**
	 * The constructor of the status panel.
	 *
	 * @param theImages the supplied images
	 */
	public StatusPanel(Images theImages) {
		images = theImages;
		HorizontalPanel outer = new HorizontalPanel();
		outer.setWidth("100%");
		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		HorizontalPanel left = new HorizontalPanel();
		left.setSpacing(8);
		HorizontalPanel right = new HorizontalPanel();
		right.setSpacing(8);
		outer.add(left);
		outer.add(right);
		left.add(new HTML("<b>Totals:</b> "));
		left.add(images.totalFiles().createImage());
		left.add(fileCountLabel);
		left.add(images.totalSize().createImage());
		left.add(fileSizeLabel);
		quotaIcon.setHTML(images.greenSize().getHTML());
		left.add(quotaIcon);
		left.add(quotaLabel);
		right.add(currentlyShowingLabel);
		outer.setStyleName("statusbar-inner");
		left.setStyleName("statusbar-inner");
		right.setStyleName("statusbar-inner");
		outer.setCellHorizontalAlignment(right, HasHorizontalAlignment.ALIGN_RIGHT);

		initWidget(outer);

		// Initialize and display the quota information.
		DeferredCommand.addCommand(new IncrementalCommand() {
			public boolean execute() {
				GSS app = GSS.get();
				UserResource user = app.getCurrentUserResource();
				if (user == null || app.getFolders().getRootItem() == null)
					return !DONE;
				displayStats(user.getQuota());
				return DONE;
			}
		});
	}

	/**
	 * Refresh the widget with the provided statistics.
	 */
	private void displayStats(QuotaHolder stats) {
		if (stats.getFileCount() == 1)
			fileCountLabel.setHTML("1 file");
		else
			fileCountLabel.setHTML(stats.getFileCount() + " files");
		fileSizeLabel.setHTML(stats.getFileSizeAsString() + " used");
		long pc = stats.percentOfFreeSpace();
		if(pc<10) {
			quotaIcon.setHTML(images.redSize().getHTML());
			quotaLabel.setHTML(stats.getQuotaLeftAsString() +" free");
		} else if(pc<20) {
			quotaIcon.setHTML(images.yellowSize().getHTML());
			quotaLabel.setHTML(stats.getQuotaLeftAsString() +" free");
		} else {
			quotaIcon.setHTML(images.greenSize().getHTML());
			quotaLabel.setHTML(stats.getQuotaLeftAsString() +" free");
		}
	}

	/**
	 * Requests updated quota information from the server and refreshes
	 * the display.
	 */
	public void updateStats() {
		final GSS app = GSS.get();
		UserResource user = app.getCurrentUserResource();
		GetCommand<UserResource> uc = new GetCommand<UserResource>(UserResource.class, user.getUri()){
			@Override
			public void onComplete() {
				displayStats(getResult().getQuota());
			}

			@Override
			public void onError(Throwable t) {
				if(t instanceof RestException)
					app.displayError("Unable to fetch quota:" +
								((RestException)t).getHttpStatusText());
				else
					app.displayError("System error fetching quota:" +
								t.getMessage());
				GWT.log("ERR", t);
			}
		};
		DeferredCommand.addCommand(uc);
	}

	/**
	 * Displays the statistics for the current folder.
	 *
	 * @param text the statistics to display
	 */
	public void updateCurrentlyShowing(String text) {
		if (text == null)
			currentlyShowingLabel.setText("");
		else
			currentlyShowingLabel.setHTML(" <b>Showing:</b> " + text);
	}

}
