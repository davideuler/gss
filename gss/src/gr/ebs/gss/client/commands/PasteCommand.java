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
package gr.ebs.gss.client.commands;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.clipboard.Clipboard;
import gr.ebs.gss.client.clipboard.ClipboardItem;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author kman Command for pasting Clipboard contents
 */
public class PasteCommand implements Command {

	private PopupPanel containerPanel;

	public PasteCommand(PopupPanel _containerPanel) {
		containerPanel = _containerPanel;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (GSS.get().getCurrentSelection() instanceof FolderResource) {
			final ClipboardItem citem = GSS.get().getClipboard().getItem();
			if (citem != null && citem.getFolderResource() != null) {

				String target = ((FolderResource) GSS.get().getCurrentSelection()).getPath();
				target = target.endsWith("/") ? target : target + '/';
				target = target + URL.encodeComponent(citem.getFolderResource().getName());
				if (citem.getOperation() == Clipboard.COPY) {
					ExecutePost cf = new ExecutePost(citem.getFolderResource().getPath() + "?copy=" + target, "", 200) {

						public void onComplete() {
							GSS.get().getFolders().updateFolder((DnDTreeItem) GSS.get().getFolders().getCurrent());
						}


						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");

								else if(statusCode == 409)
									GSS.get().displayError("A folder with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to copy folder, status code:"+statusCode+", "+t.getMessage());
							}
							else
								GSS.get().displayError("System error copying folder:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				} else if (citem.getOperation() == Clipboard.CUT) {
					ExecutePost cf = new ExecutePost(citem.getFolderResource().getPath() + "?move=" + target, "", 200) {

						public void onComplete() {
							List<TreeItem> items = GSS.get().getFolders().getItemsOfTreeForPath(citem.getFolderResource().getPath());
							for (TreeItem item : items)
								if (item.getParentItem() != null && !item.equals(GSS.get().getFolders().getCurrent()))
									GSS.get().getFolders().updateFolder((DnDTreeItem) item.getParentItem());
							GSS.get().getFolders().updateFolder((DnDTreeItem) GSS.get().getFolders().getCurrent());
						}

						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");
								else if(statusCode == 409)
									GSS.get().displayError("A folder with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to move folder, status code:"+statusCode+", "+t.getMessage());
							}
							else
								GSS.get().displayError("System error moving folder:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				}
				return;
			} else if (citem != null && citem.getFile() != null) {
				String target = ((FolderResource) GSS.get().getCurrentSelection()).getPath();
				target = target.endsWith("/") ? target : target + '/';
				target = target + URL.encodeComponent(citem.getFile().getName());
				if (citem.getOperation() == Clipboard.COPY) {
					ExecutePost cf = new ExecutePost(citem.getFile().getPath() + "?copy=" + target, "", 200) {

						public void onComplete() {
							GSS.get().showFileList(true);
						}

						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");
								else if(statusCode == 404)
									GSS.get().displayError("File not found");
								else if(statusCode == 409)
									GSS.get().displayError("A file with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to copy file");
							}
							else
								GSS.get().displayError("System error copying file:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				} else if (citem.getOperation() == Clipboard.CUT) {
					ExecutePost cf = new ExecutePost(citem.getFile().getPath() + "?move=" + target, "", 200) {

						public void onComplete() {
							GSS.get().showFileList(true);
						}

						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");
								else if(statusCode == 404)
									GSS.get().displayError("File not found");
								else if(statusCode == 409)
									GSS.get().displayError("A file with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to copy file");
							}
							else
								GSS.get().displayError("System error copying file:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				}
				return;
			} else if (citem != null && citem.getFiles() != null) {
				List<FileResource> res = citem.getFiles();
				List<String> fileIds = new ArrayList<String>();
				String target = ((FolderResource) GSS.get().getCurrentSelection()).getPath();
				target = target.endsWith("/") ? target : target + '/';

				if (citem.getOperation() == Clipboard.COPY) {
					for (FileResource fileResource : res) {
						String fileTarget = target + fileResource.getName();
						fileIds.add(fileResource.getPath() + "?copy=" + fileTarget);
					}
					int index = 0;
					executeCopyOrMove(index, fileIds);

				} else if (citem.getOperation() == Clipboard.CUT) {
					for (FileResource fileResource : res) {
						String fileTarget = target + fileResource.getName();
						fileIds.add(fileResource.getPath() + "?move=" + fileTarget);
					}
					int index =0;
					executeCopyOrMove(index, fileIds);

				}
				return;
			}

		}
		else if(GSS.get().getCurrentSelection() instanceof GroupResource){
			final ClipboardItem citem = GSS.get().getClipboard().getItem();
			GroupResource group = (GroupResource) GSS.get().getCurrentSelection();
			if(citem.getUser() != null){
				ExecutePost cg = new ExecutePost(group.getPath()+"?name="+citem.getUser().getUsername(), "", 201){

					public void onComplete() {
						GSS.get().getGroups().updateGroups();
						GSS.get().showUserList();
					}
					public void onError(Throwable t) {
						GWT.log("", t);
						if(t instanceof RestException){
							int statusCode = ((RestException)t).getHttpStatusCode();
							if(statusCode == 405)
								GSS.get().displayError("You don't have the necessary permissions");
							else if(statusCode == 404)
								GSS.get().displayError("User does not exist");
							else if(statusCode == 409)
								GSS.get().displayError("A user with the same name already exists");
							else if(statusCode == 413)
								GSS.get().displayError("Your quota has been exceeded");
							else
								GSS.get().displayError("Unable to add user, status code:"+statusCode);
						}
						else
							GSS.get().displayError("System error adding user:"+t.getMessage());
					}
				};
				DeferredCommand.addCommand(cg);
			}
		}

	}

	private void executeCopyOrMove(final int index, final List<String> paths){
		if(index >= paths.size()){
			GSS.get().showFileList(true);
			return;
		}
		ExecutePost cf = new ExecutePost(paths.get(index), "", 200){
			@Override
			public void onComplete() {
				executeCopyOrMove(index+1, paths);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("File not found");
					else if(statusCode == 409)
						GSS.get().displayError("A file with the same name already exists");
					else if(statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to copy file:"+t.getMessage());
				}
				else
					GSS.get().displayError("System error copying file:"+t.getMessage());

			}
		};
		DeferredCommand.addCommand(cf);
	}
}
