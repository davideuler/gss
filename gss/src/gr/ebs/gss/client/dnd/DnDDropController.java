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

/**
 * @author kman
 */
public class DnDDropController extends SimpleDropController {

	DnDFocusPanel nodeHolder;

	/**
	 *
	 */
	public DnDDropController(DnDFocusPanel widget) {
		super(widget);
		nodeHolder = widget;
	}

	/* (non-Javadoc)
	 * @see com.allen_sauer.gwt.dnd.client.drop.AbstractDropController#onDrop(com.allen_sauer.gwt.dnd.client.DragContext)
	 */
	public void onDrop(DragContext context) {
		super.onDrop(context);
		DnDFocusPanel toDrop = (DnDFocusPanel) context.draggable;
		if (toDrop.getItem() != null) {
			if (toDrop.getItem().getUserObject() != null && toDrop.getItem().getUserObject() instanceof FolderResource) {
				FolderResource folderToDrop = (FolderResource) toDrop.getItem().getUserObject();
				FolderResource initialFolder = null;
				if (GSS.get().getFolders().isTrash(nodeHolder.getItem())) {
				} else if (nodeHolder.getItem().getUserObject() instanceof FolderResource)
					initialFolder = (FolderResource) nodeHolder.getItem().getUserObject();
				boolean othersShared = false;
				if (GSS.get().getFolders().isOthersSharedItem(nodeHolder.getItem()))
					othersShared = true;
				DnDFolderPopupMenu popup = new DnDFolderPopupMenu(GSS.get().getFolders().getImages(), initialFolder, folderToDrop, othersShared);
				int left = nodeHolder.getItem().getAbsoluteLeft() + 40;
				int top = nodeHolder.getItem().getAbsoluteTop() + 20;
				popup.setPopupPosition(left, top);
				popup.show();
			}
		} else if (toDrop.getFiles() != null) {
			List<FileResource> folderToDrop = toDrop.getFiles();
			FolderResource initialFolder = null;
			if (GSS.get().getFolders().isTrash(nodeHolder.getItem())) {
			} else if (nodeHolder.getItem().getUserObject() instanceof FolderResource)
				initialFolder = (FolderResource) nodeHolder.getItem().getUserObject();
			boolean othersShared = false;
			if (GSS.get().getFolders().isOthersSharedItem(nodeHolder.getItem()))
				othersShared = true;
			DnDFolderPopupMenu popup = new DnDFolderPopupMenu(GSS.get().getFolders().getImages(), initialFolder, folderToDrop, othersShared);
			int left = nodeHolder.getItem().getAbsoluteLeft() + 40;
			int top = nodeHolder.getItem().getAbsoluteTop() + 20;
			popup.setPopupPosition(left, top);
			popup.show();
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
		nodeHolder.getItem().getWidget().removeStyleName("gss-SelectedRow");
		super.onLeave(context);
	}

}
