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
package gr.ebs.gss.client.tree;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.PopupTree;
import gr.ebs.gss.client.Folders.Images;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.TreeItem;



/**
 * @author kman
 */
public class TrashSubtree extends Subtree {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	private DnDTreeItem rootItem;

	public TrashSubtree(PopupTree tree, final Images _images) {
		super(tree, _images);

		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return updateInit();
			}
		});
	}

	public boolean updateInit() {

		UserResource userResource = GSS.get().getCurrentUserResource();
		if ( userResource == null || GSS.get().getFolders().getRootItem() == null)
			return !DONE;
		update();
		return DONE;

	}

	public void update() {
		DeferredCommand.addCommand(new ExecuteGet<TrashResource>(TrashResource.class, GSS.get().getCurrentUserResource().getTrashPath()) {
			public void onComplete() {
				if(rootItem == null){
					rootItem = new DnDTreeItem(imageItemHTML(images.trash(), "Trash"), "Trash", false);
					tree.addItem(rootItem);
					rootItem.doDroppable();
				}
				rootItem.setUserObject(getResult());
				rootItem.removeItems();
				List<FolderResource> res = rootItem.getTrashResource().getTrashedFolders();
				for (FolderResource r : res) {
					DnDTreeItem child = (DnDTreeItem) addImageItem(rootItem, r.getName(), images.folderYellow(), true);
					child.setUserObject(r);
					child.setState(false);
				}
			}

			public void onError(Throwable t) {
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					//empty trash ie returns 1223 status code instead of 204
					if(statusCode == 204 || statusCode == 1223){
						GWT.log("Trash is empty", null);
						if(rootItem == null){
							rootItem = new DnDTreeItem(imageItemHTML(images.trash(), "Trash"), "Trash", false);
							tree.addItem(rootItem);
							rootItem.doDroppable();
						}
						rootItem.setUserObject(new TrashResource(GSS.get().getCurrentUserResource().getTrashPath()));
						rootItem.removeItems();
					} else{
						if(rootItem == null){
							rootItem = new DnDTreeItem(imageItemHTML(images.trash(), "Trash"), "Trash", false);
							tree.addItem(rootItem);
						}
						rootItem.setUserObject(new TrashResource(GSS.get().getCurrentUserResource().getTrashPath()));
						GSS.get().displayError("Unable to fetch trash folder:"+((RestException)t).getHttpStatusText());
					}
				}
				else{
					GWT.log("", t);
					GSS.get().displayError("Unable to fetch trash folder:"+t.getMessage());
					if(rootItem == null){
						rootItem = new DnDTreeItem(imageItemHTML(images.trash(), "Trash"), "Trash", false);
						tree.addItem(rootItem);
						rootItem.doDroppable();
					}
					rootItem.setUserObject(new TrashResource(GSS.get().getCurrentUserResource().getTrashPath()));
				}
			}
		});

	}

	/**
	 * Retrieve the rootItem.
	 *
	 * @return the rootItem
	 */
	public TreeItem getRootItem() {
		return rootItem;
	}

}
