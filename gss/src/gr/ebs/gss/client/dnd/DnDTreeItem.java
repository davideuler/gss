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

import gr.ebs.gss.client.Folders;
import gr.ebs.gss.client.GSS;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.UserDTO;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author kman
 *
 */
public class DnDTreeItem extends TreeItem implements SourcesMouseEvents {
	public static final int FOLDER = 0;
	public static final int SHARED = 1;
	public static final int TRASH = 2;
	public static final int OTHERS = 3;

	private DnDFocusPanel focus;
	private String type;
	private Widget content;
	private Widget toDrag;
	private DnDDropController drop;
	private List<DnDTreeItem> toRemove = new ArrayList();
	private boolean draggable = false;

	public DnDTreeItem(Widget widget,String name, boolean _draggable) {
		super();
		draggable = _draggable;
		content = widget;
		focus = new DnDFocusPanel(content,this);
		focus.setTabIndex(-1);
		setWidget(focus);
	}

	public void updateWidget(Widget widget){
		content = widget;
		focus.setWidget(content);

	}

	public void addMouseListener(MouseListener mouseListener){
		focus.addMouseListener(mouseListener);
	}

	public void removeMouseListener(MouseListener mouseListener){
		focus.removeMouseListener(mouseListener);
	}


	/**
	 * Retrieve the drop.
	 *
	 * @return the drop
	 */
	public DnDDropController getDrop() {
		return drop;
	}



	public Widget getContent() {
		return content;
	}


	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.TreeItem#removeItems()
	 */
	@Override
	public void removeItems() {
		toRemove = new ArrayList();
		removeItems(this);
		for(DnDTreeItem it : toRemove)
			if(it.getDrop() != null)
				GSS.get().getDragController().unregisterDropController(it.getDrop());
		toRemove.clear();
		super.removeItems();

	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.TreeItem#removeItem(com.google.gwt.user.client.ui.TreeItem)
	 */
	@Override
	public void removeItem(TreeItem item) {
		item.removeItems();
		if(item instanceof DnDTreeItem){
			DnDTreeItem it = (DnDTreeItem) item;
			if(it.getDrop() != null)
				GSS.get().getDragController().unregisterDropController(it.getDrop());
		}
		super.removeItem(item);

	}
	public void doDroppable(){
		drop = new DnDDropController(focus);
		GSS.get().getDragController().registerDropController(drop);
	}

	public void doDraggable(){
		GSS.get().getDragController().makeDraggable(getFocus(), getContent());
		drop = new DnDDropController(focus);
		GSS.get().getDragController().registerDropController(drop);
	}

	public void undoDraggable(){
		GSS.get().getDragController().makeNotDraggable(getFocus());
		if(drop!=null)
			GSS.get().getDragController().unregisterDropController(getDrop());
	}

	protected void removeItems(DnDTreeItem item){
		for(int i=0;i<item.getChildCount();i++) {
			DnDTreeItem it = (DnDTreeItem)item.getChild(i);
			toRemove.add(it);
			removeItems(it);
		}
	}


	/**
	 * Retrieve the focus.
	 *
	 * @return the focus
	 */
	public DnDFocusPanel getFocus() {
		return focus;
	}


	/**
	 * Retrieve the draggable.
	 *
	 * @return the draggable
	 */
	public boolean isDraggable() {
		return draggable;
	}

	public DnDTreeItem getChild(FolderDTO folder){
		for(int i=0; i< getChildCount(); i++){
			DnDTreeItem c = (DnDTreeItem) getChild(i);
			if(c.getUserObject() instanceof FolderDTO)
				if(((FolderDTO)c.getUserObject()).getId().equals(folder.getId()))
					return c;
		}
		return null;
	}

	public DnDTreeItem getChild(UserDTO user){
		for(int i=0; i< getChildCount(); i++){
			DnDTreeItem c = (DnDTreeItem) getChild(i);
			if(c.getUserObject() instanceof UserDTO)
				if(((UserDTO)c.getUserObject()).getId().equals(user.getId()))
					return c;
		}
		return null;
	}

	public void insertItem(TreeItem item, int position) {
	    addItem(item);
	    //if(position != 0)
	    	DOM.insertChild(getElement(), item.getElement(), position);
	  }


	public int getItemType(){
		Folders f = GSS.get().getFolders();
		if(f.isFileItem(this))
			return FOLDER;
		if(f.isMySharedItem(this))
			return SHARED;
		if(f.isOthersSharedItem(this))
			return OTHERS;
		return TRASH;
	}


}
