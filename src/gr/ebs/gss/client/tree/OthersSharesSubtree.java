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
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman
 */
public class OthersSharesSubtree extends Subtree {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	private DnDTreeItem rootItem;

	public OthersSharesSubtree(PopupTree aTree, final Images _images) {
		super(aTree, _images);
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return updateInit();
			}
		});
	}

	public boolean updateInit() {
		UserResource userResource = GSS.get().getCurrentUserResource();
		if (userResource == null ||
					GSS.get().getFolders().getRootItem() == null ||
					GSS.get().getFolders().getTrashItem() == null ||
					GSS.get().getFolders().getMySharesItem() == null)
			return !DONE;

		GetCommand<OthersResource> go = new GetCommand<OthersResource>(OthersResource.class,
					userResource.getOthersPath()) {

			@Override
			public void onComplete() {
				rootItem = new DnDTreeItem(imageItemHTML(images.othersShared(), "Other's Shared"), "Other's Shared", false);
				rootItem.setUserObject(getResult());
				tree.addItem(rootItem);
				rootItem.removeItems();
				update(rootItem);
				GSS.get().removeGlassPanel();
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Error fetching Others Root folder", t);
				GSS.get().displayError("Unable to fetch Others Root folder");
				if(rootItem != null){
					rootItem = new DnDTreeItem(imageItemHTML(images.othersShared(), "ERROR"), "ERROR", false);
					tree.addItem(rootItem);
				}
			}
		};
		DeferredCommand.addCommand(go);
		return DONE;
	}

	public void update(final DnDTreeItem folderItem) {
		if (folderItem.getOthersResource() != null) {

			MultipleGetCommand<OtherUserResource> go = new MultipleGetCommand<OtherUserResource>(OtherUserResource.class,
						folderItem.getOthersResource().getOthers().toArray(new String[] {})) {

				@Override
				public void onComplete() {
					List<OtherUserResource> res = getResult();
					folderItem.removeItems();
					for (OtherUserResource r : res) {
						DnDTreeItem child = (DnDTreeItem) addImageItem(folderItem,
									r.getName(), images.folderYellow(), true);
						child.setUserObject(r);
						child.setState(false);
						if(folderItem.getState())
							update(child);
					}
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("Error fetching Others Root folder", t);
					GSS.get().displayError("Unable to fetch Others Root folder");
				}

				@Override
				public void onError(String p, Throwable throwable) {
					GWT.log("Path:"+p, throwable);
				}
			};
			DeferredCommand.addCommand(go);
		} else if (folderItem.getOtherUserResource() != null) {

			GetCommand<OtherUserResource> go = new GetCommand<OtherUserResource>(OtherUserResource.class,
						folderItem.getOtherUserResource().getUri()) {

				@Override
				public void onComplete() {
					OtherUserResource res = getResult();
					folderItem.removeItems();
					for (FolderResource r : res.getFolders()) {
						DnDTreeItem child = (DnDTreeItem) addImageItem(folderItem,
									r.getName(), images.folderYellow(), true);
						child.setUserObject(r);
						child.setState(false);
						child.doDraggable();
						update(child);
						updateFolderAndSubfolders(child);
					}
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("Error fetching Others Root folder", t);
					GSS.get().displayError("Unable to fetch Others Root folder");
				}

			};
			DeferredCommand.addCommand(go);
		} else if (folderItem.getFolderResource() != null) {

			MultipleGetCommand<FolderResource> go = new MultipleGetCommand<FolderResource>(FolderResource.class,
						folderItem.getFolderResource().getSubfolderPaths().toArray(new String[] {})) {

				@Override
				public void onComplete() {
					List<FolderResource> res = getResult();
					folderItem.removeItems();
					for (FolderResource r : res) {
						DnDTreeItem child = (DnDTreeItem) addImageItem(folderItem,
									r.getName(), images.folderYellow(), true);
						child.setUserObject(r);
						child.setState(false);
						child.doDraggable();
						update(child);
					}
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("Error fetching Others Root folder", t);
					GSS.get().displayError("Unable to fetch Others Root folder");
				}

				@Override
				public void onError(String p, Throwable throwable) {
					GWT.log("Path:"+p, throwable);
				}
			};
			DeferredCommand.addCommand(go);
		}

	}

	public void updateFolderAndSubfolders(final DnDTreeItem folderItem) {
		if (folderItem.getFolderResource() != null) {
			final String path = folderItem.getFolderResource().getUri();
			GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, path) {

				@Override
				public void onComplete() {
					FolderResource rootResource = getResult();
					folderItem.undoDraggable();
					folderItem.updateWidget(imageItemHTML(images.folderYellow(), rootResource.getName()));
					folderItem.setUserObject(rootResource);
					folderItem.doDraggable();
					update(folderItem);
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("Error fetching folder", t);
					GSS.get().displayError("Unable to fetch folder:" + folderItem.getFolderResource().getName());
				}
			};
			DeferredCommand.addCommand(gf);
		}
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
