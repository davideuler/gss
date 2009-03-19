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

import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.QuotaHolder;
import gr.ebs.gss.client.rest.resource.UserResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * The panel that displays a status bar with quota information.
 */
public class StatusPanel extends Composite {
	public static final boolean DONE = false;
	private HTML fileCountLabel;
	private HTML fileSizeLabel;
	private HTML quotaLabel;
	private HTML currentlyShowingLabel;

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ImageBundle {

		@Resource("gr/ebs/gss/resources/windowlist.png")
		AbstractImagePrototype totalFiles();

		@Resource("gr/ebs/gss/resources/database.png")
		AbstractImagePrototype totalSize();

		@Resource("gr/ebs/gss/resources/redled.png")
		AbstractImagePrototype freeSize();

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
		outer.setSpacing(8);
		outer.add(new HTML("<b>Totals:</b> "));
		outer.add(images.totalFiles().createImage());
		outer.add(fileCountLabel = new HTML(""));
		outer.add(images.totalSize().createImage());
		outer.add(fileSizeLabel = new HTML(""));
		outer.add(quotaLabel = new HTML(""));
		outer.add(currentlyShowingLabel = new HTML(""));
		outer.setStyleName("statusbar-inner");

		initWidget(outer);

		DeferredCommand.addCommand(new IncrementalCommand() {
			public boolean execute() {
				return updateStats();
			}
		});
	}

	public boolean updateStats() {
		UserResource userResource = GSS.get().getCurrentUserResource();
		if (userResource == null || GSS.get().getFolders().getRootItem() == null) return !DONE;
		ExecuteGet<UserResource> uc = new ExecuteGet<UserResource>(UserResource.class, userResource.getPath()){
			@Override
			public void onComplete() {
				final QuotaHolder stats = getResult().getQuota();
				if (stats.getFileCount() == 1)
					fileCountLabel.setHTML("1 file");
				else
					fileCountLabel.setHTML(stats.getFileCount() + " files");
				fileSizeLabel.setHTML(stats.getFileSizeAsString() + " used");
				long pc = stats.percentOfFreeSpace();
				if(pc<10)
					quotaLabel.setHTML(images.freeSize().getHTML()+"&nbsp;"+stats.getQuotaLeftAsString() +" free");
				else if(pc<20)
					quotaLabel.setHTML(images.yellowSize().getHTML()+"&nbsp;"+stats.getQuotaLeftAsString() +" free");
				else
					quotaLabel.setHTML(images.greenSize().getHTML()+"&nbsp;"+stats.getQuotaLeftAsString() +" free");
			}

			@Override
			public void onError(Throwable t) {
				if(t instanceof RestException)
					GSS.get().displayError("Unable to fetch quota:"+((RestException)t).getHttpStatusText());
				else
					GSS.get().displayError("System error fetching quota:"+t.getMessage());
				GWT.log("ERR", t);
			}
		};
		DeferredCommand.addCommand(uc);
		return DONE;
	}

	public void updateCurrentlyShowing(String text) {
		if (text == null)
			currentlyShowingLabel.setText("");
		else
			currentlyShowingLabel.setHTML(" <b>Showing:</b> " + text);
	}

}
