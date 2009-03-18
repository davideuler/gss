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
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.ExecuteMultiplePost;
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.GroupUserResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;


/**
 *
 * Restore trashed files and folders
 * @author kman
 *
 */
public class RestoreTrashCommand implements Command{
	private PopupPanel containerPanel;

	public RestoreTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.Command#execute()
	 */
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null){
			//check to see if Trash Node is selected
			final List folderList = new ArrayList();
			TreeItem trashItem = GSS.get().getFolders().getTrashItem();
			for(int i=0 ; i < trashItem.getChildCount() ; i++)
				folderList.add(trashItem.getChild(i).getUserObject());
			if(GSS.get().getFolders().getCurrent().equals(GSS.get().getFolders().getTrashItem()))
				/*
				GSS.get().getRemoteService().restoreTrash(GSS.get().getCurrentUser().getId(), new AsyncCallback() {

					public void onSuccess(final Object result) {
						for(int k=0 ; k < folderList.size(); k++){
							FolderDTO fdto = (FolderDTO)folderList.get(k);
							GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getUserItem(fdto.getParent()));
						}
						GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getTrashItem());
						GSS.get().getFolders().update(GSS.get().getCurrentUser().getId(), GSS.get().getFolders().getMySharesItem());
						GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());

					}

					public void onFailure(final Throwable caught) {
						GWT.log("", caught);
						if (caught instanceof RpcException)
							GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
						else
							GSS.get().displayError(caught.getMessage());
					}
				});
				*/
			return;
		}
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof FileResource) {
			final FileResource resource = (FileResource)selection;
			ExecutePost rt = new ExecutePost(resource.getPath()+"?restore=","", 200){

				public void onComplete() {
					GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
					GSS.get().showFileList(true);
				}

				public void onError(Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File does not exist");
						else if(statusCode == 409)
							GSS.get().displayError("A file with the same name already exists");
						else if(statusCode == 413)
							GSS.get().displayError("Your quota has been exceeded");
						else
							GSS.get().displayError("Unable to restore file:"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error restoring file:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(rt);
		}
		else if (selection instanceof List) {
			final List<FileResource> fdtos = (List<FileResource>) selection;
			final List<String> fileIds = new ArrayList<String>();
			for(FileResource f : fdtos)
				fileIds.add(f.getPath()+"?restore=");
			ExecuteMultiplePost rt = new ExecuteMultiplePost(fileIds.toArray(new String[0]), 200){

				public void onComplete() {
					GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
					GSS.get().showFileList(true);
				}


				public void onError(String p, Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File does not exist");
						else if(statusCode == 409)
							GSS.get().displayError("A file with the same name already exists");
						else if(statusCode == 413)
							GSS.get().displayError("Your quota has been exceeded");
						else
							GSS.get().displayError("Unable to restore file::"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error restoring file:"+t.getMessage());

				}
			};
			DeferredCommand.addCommand(rt);
		}
		else if (selection instanceof FolderResource) {
			final FolderResource resource = (FolderResource)selection;
			ExecutePost rt = new ExecutePost(resource.getPath()+"?restore=","", 200){

				public void onComplete() {
					GSS.get().getFolders().updateFolder((DnDTreeItem) GSS.get().getFolders().getRootItem());

					GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
				}

				public void onError(Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("Folder does not exist");
						else if(statusCode == 409)
							GSS.get().displayError("A folder with the same name already exists");
						else if(statusCode == 413)
							GSS.get().displayError("Your quota has been exceeded");
						else
							GSS.get().displayError("Unable to restore folder::"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error restoring folder:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(rt);
		} else if (selection instanceof GroupUserResource) {
			// TODO do we need trash in users?
		} else if (selection instanceof GroupResource) {
			// TODO do we need trash for groups?
		}

	}

}
