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

import gr.ebs.gss.client.commands.CopyCommand;
import gr.ebs.gss.client.commands.CutCommand;
import gr.ebs.gss.client.commands.DeleteCommand;
import gr.ebs.gss.client.commands.PropertiesCommand;
import gr.ebs.gss.client.commands.RestoreTrashCommand;
import gr.ebs.gss.client.commands.ToTrashCommand;
import gr.ebs.gss.client.commands.UpdateFileCommand;
import gr.ebs.gss.client.rest.resource.FileResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The 'File Context' menu implementation.
 */
public class FileContextMenu extends PopupPanel implements ClickListener {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private MenuItem cutItem;

	private MenuItem copyItem;

	private MenuItem updateItem;

	private MenuItem propItem;

	private MenuItem trashItem;

	private MenuItem deleteItem;

	private MenuItem downloadItem;

	/**
	 * The image bundle for this widget's images that reuses images defined in
	 * other menus.
	 */
	public interface Images extends FileMenu.Images, EditMenu.Images {

		/**
		 * Will bundle the file 'document_menu.png' residing in the package
		 * 'gr.ebs.gss.resources.mimetypes'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/mimetypes/document_menu.png")
		AbstractImagePrototype fileContextMenu();

		/**
		 * Will bundle the file 'doc_versions.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/doc_versions.png")
		AbstractImagePrototype versions();
	}

	public static native String getDate()/*-{
		return (new Date()).toUTCString();
	}-*/;

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public FileContextMenu(final Images newImages, boolean isTrash) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;

		// A dummy command that we will execute from unimplemented leaves.
		final Command cmd = new Command() {

			public void execute() {
				hide();
				Window.alert("You selected a menu item!");
			}
		};

		// The command that does some validation before downloading a file.
		final Command downloadCmd = new Command() {

			public void execute() {
				hide();
				// GSS.get().getTopPanel().getFileMenu().preDownloadCheck();
				if (GSS.get().getCurrentSelection() instanceof FileResource) {
					FileResource res = (FileResource) GSS.get().getCurrentSelection();
					RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, res.getPath());
					String date = getDate();
					requestBuilder.setHeader("X-GSS-Date", date);
					String username = "kman";
					if (GSS.get().getCurrentUserResource() != null)
						username = GSS.get().getCurrentUserResource().getUsername();
					String token = GSS.get().getToken();
					if (token == null)
						token = "aa";
					requestBuilder.setHeader("Authorization", username + " " + token);
					requestBuilder.setHeader("Accept", "application/json; charset=utf-8");
					requestBuilder.setHeader("Accept-Charset", "utf-8");
					try {
						requestBuilder.sendRequest("", new RequestCallback() {

							public void onError(Request arg0, Throwable arg1) {
								// TODO Auto-generated method stub

							}

							public void onResponseReceived(Request arg0, Response arg1) {
								// TODO Auto-generated method stub

							}

						});
					} catch (RequestException e) {
						GWT.log("EXC", e);
					}
				}

			}

		};

		final MenuBar contextMenu = new MenuBar(true);
		if (isTrash) {
			contextMenu.addItem("<span>" + newImages.versions().getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(this));
			contextMenu.addItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));
		} else {
			cutItem = new MenuItem("<span>" + newImages.cut().getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
			copyItem = new MenuItem("<span>" + newImages.copy().getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
			updateItem = new MenuItem("<span>" + newImages.copy().getHTML() + "&nbsp;Update</span>", true, new UpdateFileCommand(this));

			propItem = new MenuItem("<span>" + newImages.viewText().getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, images));
			trashItem = new MenuItem("<span>" + newImages.emptyTrash().getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
			deleteItem = new MenuItem("<span>" + newImages.delete().getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));

			contextMenu.addItem(cutItem);
			contextMenu.addItem(copyItem);
			contextMenu.addItem(updateItem);

			contextMenu.addItem(propItem);
			contextMenu.addItem(trashItem);
			contextMenu.addItem(deleteItem);
			String[] link = {"", ""};
			GSS.get().getTopPanel().getFileMenu().createDownloadLink(link);
			// downloadItem = new MenuItem("<span>" + link[0] +
			// newImages.download().getHTML() + "&nbsp;Download File" + link[1]
			// + "</span>", true, downloadCmd);
			downloadItem = new MenuItem("<span>" + newImages.download().getHTML() + "&nbsp;Download File </span>", true, downloadCmd);
			contextMenu.addItem(downloadItem);
		}

		add(contextMenu);

		// Postpone the addition of the Download File menu option in order
		// to finish the object construction phase, since we need a reference
		// to a properly initialized GSS object.
		// DeferredCommand.addCommand(new Command() {

		// public void execute() {
		// Construct a download URL in order to force the browser to
		// actually download a file.
		// String[] link = {"", ""};
		// GSS.get().getTopPanel().getFileMenu().createDownloadLink(link);
		// downloadItem = new MenuItem("<span>" + link[0] +
		// newImages.download().getHTML() + "&nbsp;Download File" + link[1] +
		// "</span>", true, downloadCmd);
		// contextMenu.addItem(downloadItem);
		// }
		// });
	}

	void onMultipleSelection() {
		updateItem.setVisible(false);
		propItem.setVisible(false);
		downloadItem.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(final Widget sender) {

		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (res.isDeleted())
					menu = new FileContextMenu(images, true);
				else
					menu = new FileContextMenu(images, false);
				int left = sender.getAbsoluteLeft();
				int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
				menu.setPopupPosition(left, top);
				menu.show();
			} else if (GSS.get().getCurrentSelection() instanceof List) {
				List<FileResource> dto = (List<FileResource>) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (GSS.get().getFolders().isTrashItem(GSS.get().getFolders().getCurrent()))
					menu = new FileContextMenu(images, true);
				else {
					menu = new FileContextMenu(images, false);
					menu.onMultipleSelection();
				}
				int left = sender.getAbsoluteLeft();
				int top = sender.getAbsoluteTop() + sender.getOffsetHeight();
				menu.setPopupPosition(left, top);
				menu.show();
			}
	}
}
