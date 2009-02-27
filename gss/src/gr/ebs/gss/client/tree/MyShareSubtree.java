/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd. This file is part of
 * GSS. GSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. GSS is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with GSS. If not, see <http://www.gnu.org/licenses/>.
 */
package gr.ebs.gss.client.tree;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.PopupTree;
import gr.ebs.gss.client.Folders.Images;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.ExecuteMultipleGet;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman
 */
public class MyShareSubtree extends Subtree {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	private DnDTreeItem rootItem;

	public MyShareSubtree(PopupTree tree, final Images _images) {
		super(tree, _images);

		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return updateInit();
			}
		});
	}

	public boolean updateInit() {
		UserResource userResource = GSS.get().getCurrentUserResource();
		if (userResource == null || GSS.get().getFolders().getRootItem() == null || GSS.get().getFolders().getTrashItem() == null)
			return !DONE;

		ExecuteGet<SharedResource> gs = new ExecuteGet<SharedResource>(SharedResource.class, userResource.getSharedPath()) {

			public void onComplete() {
				rootItem = new DnDTreeItem(imageItemHTML(images.myShared(), "My Shared"), "My Shared", false);
				rootItem.setUserObject(getResult());
				tree.addItem(rootItem);
				rootItem.removeItems();
				update(rootItem);
			}

			public void onError(Throwable t) {
				GWT.log("Error fetching Shared Root folder", t);
				GSS.get().displayError("Unable to fetch Shared Root folder");
			}
		};
		DeferredCommand.addCommand(gs);
		/*
		DeferredCommand.addCommand(new GetSharedCommand(userResource.getSharedPath()) {

			public void onComplete() {
				rootItem = new DnDTreeItem(imageItemHTML(images.myShared(), "My Shared"), "My Shared", false);
				rootItem.setUserObject("My shared");
				rootItem.setUserObject(getResource());
				tree.addItem(rootItem);
				rootItem.removeItems();
				update(rootItem);
			}
		});
		*/
		return DONE;

	}

	public void update(final DnDTreeItem folderItem) {
		if (folderItem.getFolderResource() != null) {
			folderItem.removeItems();
			List<String> newPaths = new ArrayList<String>();
			for (String s : folderItem.getFolderResource().getSubfolderPaths()) {

				if (!s.endsWith("/"))
					s = s + "/";
				if (rootItem.getSharedResource().getSubfolderPaths().contains(s))
					newPaths.add(s);
			}
			String parentName = "";
			if(folderItem.getParentItem() != null && ((DnDTreeItem)folderItem.getParentItem()).getFolderResource() != null)
				parentName = ((DnDTreeItem)folderItem.getParentItem()).getFolderResource().getName()+"->";
			parentName = parentName+folderItem.getFolderResource().getName();
			folderItem.getFolderResource().setSubfolderPaths(newPaths);
			ExecuteMultipleGet<FolderResource> gf = new ExecuteMultipleGet<FolderResource>(FolderResource.class, newPaths.toArray(new String[] {})) {

				public void onComplete() {
					List<FolderResource> res = getResult();
					for (FolderResource r : res) {

						DnDTreeItem child = (DnDTreeItem) addImageItem(folderItem, r.getName(), images.folderYellow(), true);
						child.setUserObject(r);
						child.setState(false);
						debug();
					}

				}

				public void onError(Throwable t) {
					GSS.get().displayError("Unable to fetch subfolders");
					GWT.log("Unable to fetch subfolders", t);
				}

				@Override
				public void onError(String p, Throwable throwable) {
					GWT.log("Path:"+p, throwable);
				}
			};
			/*
			GetFoldersCommand gf = new GetFoldersCommand(newPaths.toArray(new String[]{})) {

				public void onComplete() {
					List<FolderResource> res = getFile();
					for (FolderResource r : res){
						//if (rootItem.getSharedResource().getSubfolders().contains(r.getPath())) {
							DnDTreeItem child = (DnDTreeItem) addImageItem(folderItem, r.getName(), images.folderYellow(), true);
							child.setUserObject(r);
							//child.setState(false);

			//						}
					}

				}
			};*/
			DeferredCommand.addCommand(gf);
		}
		if (folderItem.getSharedResource() != null) {
			folderItem.removeItems();
			List<String> paths = folderItem.getSharedResource().getSubfolderPaths();
			List<String> newPaths = new ArrayList<String>();
			for (String r : paths)
				if (isRoot(r, paths))
					newPaths.add(r);
			ExecuteMultipleGet<FolderResource> gf = new ExecuteMultipleGet<FolderResource>(FolderResource.class, newPaths.toArray(new String[] {})) {

				public void onComplete() {
					List<FolderResource> res = getResult();
					for (FolderResource r : res) {
						// if (isRoot(r, res)) {
						DnDTreeItem child = (DnDTreeItem) addImageItem(folderItem, r.getName(), images.folderYellow(), true);
						child.setUserObject(r);
						child.setState(false);
						// }
					}

				}

				public void onError(Throwable t) {
					GSS.get().displayError("Unable to fetch subfolders");
					GWT.log("Unable to fetch subfolders", t);
				}

				@Override
				public void onError(String p, Throwable throwable) {
					GWT.log("Path:"+p, throwable);
				}
			};
			/*
			GetFoldersCommand gf = new GetFoldersCommand(folderItem.getSharedResource().getSubfolders().toArray(new String[] {})) {

				public void onComplete() {
					List<FolderResource> res = getFile();
					List<FolderResource> bucket = new ArrayList<FolderResource>();
					bucket.addAll(res);
					for (FolderResource r : res)
						if (isRoot(r, res)) {
							DnDTreeItem child = (DnDTreeItem) addImageItem(folderItem, r.getName(), images.folderYellow(), true);
							child.setUserObject(r);
							//child.setState(false);
							//update(child);
							//for(FolderResource ff : res)
								//if(isChild(r, ff)){
									//DnDTreeItem child2 = (DnDTreeItem) addImageItem(child, ff.getName(), images.folderYellow(), true);
									//child2.setFolderResource(ff);
									//child2.setState(false);
								//}
						}

				}
			};*/
			DeferredCommand.addCommand(gf);
		}
	}

	private void handleFolders(DnDTreeItem child, FolderResource ff, List<FolderResource> folders) {

	}

	private boolean isRoot(FolderResource f, List<FolderResource> folders) {
		for (FolderResource t : folders)
			if (!f.getPath().equals(t.getPath()) && f.getPath().startsWith(t.getPath()))
				return false;
		return true;
	}

	private boolean isRoot(String f, List<String> folders) {
		for (String t : folders)
			if (!f.equals(t) && f.startsWith(t))
				return false;
		return true;
	}



	public void updateFolderAndSubfolders(final DnDTreeItem folderItem) {
		if (folderItem.getFolderResource() != null) {
			final String path = folderItem.getFolderResource().getPath();
			ExecuteGet<SharedResource> gs = new ExecuteGet<SharedResource>(SharedResource.class, GSS.get().getCurrentUserResource().getSharedPath()) {

				public void onComplete() {
					rootItem.setUserObject(getResult());
					ExecuteGet<FolderResource> gf = new ExecuteGet<FolderResource>(FolderResource.class, path) {

						public void onComplete() {
							FolderResource rootResource = getResult();
							if(rootItem.getSharedResource().getSubfolderPaths().contains(rootResource.getPath())){
								folderItem.undoDraggable();
								folderItem.updateWidget(imageItemHTML(images.folderYellow(), rootResource.getName()));
								folderItem.setUserObject(rootResource);
								folderItem.doDraggable();
								update(folderItem);
							} else
								folderItem.getParentItem().removeItem(folderItem);
						}

						public void onError(Throwable t) {
							GWT.log("Error fetching folder", t);
							GSS.get().displayError("Unable to fetch folder:" + folderItem.getFolderResource().getName());
						}
					};
					DeferredCommand.addCommand(gf);
				}

				public void onError(Throwable t) {
					GWT.log("Error fetching Shared Root folder", t);
					GSS.get().displayError("Unable to fetch Shared Root folder");
				}
			};
			DeferredCommand.addCommand(gs);

		}
		else if( folderItem.getSharedResource() != null){
			ExecuteGet<SharedResource> gs = new ExecuteGet<SharedResource>(SharedResource.class, GSS.get().getCurrentUserResource().getSharedPath()) {

				public void onComplete() {
					rootItem.setUserObject(getResult());
					rootItem.removeItems();
					update(rootItem);
				}

				public void onError(Throwable t) {
					GWT.log("Error fetching Shared Root folder", t);
					GSS.get().displayError("Unable to fetch Shared Root folder");
				}
			};
			DeferredCommand.addCommand(gs);
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




	public void updateNode(TreeItem node, FolderResource folder) {
		node.getWidget().removeStyleName("gss-SelectedRow");
		if (node instanceof DnDTreeItem) {
			((DnDTreeItem) node).undoDraggable();
			((DnDTreeItem) node).updateWidget(imageItemHTML(images.folderYellow(), folder.getName()));
			((DnDTreeItem) node).doDraggable();
		} else
			node.setWidget(imageItemHTML(images.folderYellow(), folder.getName()));
		node.setUserObject(folder);
	}
}
