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
package gr.ebs.gss.client.dnd;

import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;

import java.util.List;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.core.client.GWT;

/**
 * @author kman
 */
public class DnDDropController extends SimpleDropController {

	DnDFocusPanel nodeHolder;

	public DnDDropController(DnDFocusPanel widget) {
		super(widget);
		nodeHolder = widget;
	}

	@Override
	public void onDrop(DragContext context) {
		super.onDrop(context);
		if(context.draggable instanceof DnDFocusPanel){
			DnDFocusPanel toDrop = (DnDFocusPanel) context.draggable;
			if (toDrop.getItem() != null)
				if (toDrop.getItem().getUserObject() != null && toDrop.getItem().getUserObject() instanceof FolderResource) {
					FolderResource folderToDrop = (FolderResource) toDrop.getItem().getUserObject();
					FolderResource initialFolder = null;
					if (nodeHolder.getItem().getUserObject() instanceof FolderResource)
						initialFolder = (FolderResource) nodeHolder.getItem().getUserObject();
					if (GSS.get().getFolders().isOthersSharedItem(nodeHolder.getItem())) {
					}
					DnDFolderPopupMenu popup = new DnDFolderPopupMenu(GSS.get().getFolders().getImages(), initialFolder, folderToDrop);
					int left = nodeHolder.getItem().getAbsoluteLeft() + 40;
					int top = nodeHolder.getItem().getAbsoluteTop() + 20;
					popup.setPopupPosition(left, top);
					popup.show();
				}
		}
		else if(context.draggable instanceof DnDSimpleFocusPanel){
			DnDSimpleFocusPanel toDrop = (DnDSimpleFocusPanel) context.draggable;
			if (GSS.get().getFileList().getSelectedFiles() != null) {
				List<FileResource> folderToDrop = GSS.get().getFileList().getSelectedFiles();
				GWT.log("DROPPING:"+toDrop.getFiles().size());
				FolderResource initialFolder = null;
				if (GSS.get().getFolders().isTrash(nodeHolder.getItem())) {
				} else if (nodeHolder.getItem().getUserObject() instanceof FolderResource)
					initialFolder = (FolderResource) nodeHolder.getItem().getUserObject();
				if (GSS.get().getFolders().isOthersSharedItem(nodeHolder.getItem())) {
				}
				DnDFolderPopupMenu popup = new DnDFolderPopupMenu(GSS.get().getFolders().getImages(), initialFolder, folderToDrop);
				int left = nodeHolder.getItem().getAbsoluteLeft() + 40;
				int top = nodeHolder.getItem().getAbsoluteTop() + 20;
				popup.setPopupPosition(left, top);
				popup.show();
			}
		}
	}

	@Override
	public void onEnter(DragContext context) {
		super.onEnter(context);
		nodeHolder.getItem().getWidget().addStyleName("gss-SelectedRow");
		nodeHolder.getItem().setState(true);
		GSS.get().getDragController().resetCache();
	}

	@Override
	public void onLeave(DragContext context) {
		if(!nodeHolder.getItem().equals(GSS.get().getFolders().getCurrent()))
			nodeHolder.getItem().getWidget().removeStyleName("gss-SelectedRow");
		super.onLeave(context);
	}

}
