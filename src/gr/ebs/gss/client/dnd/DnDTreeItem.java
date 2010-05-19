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
import gr.ebs.gss.client.PopupTree;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author kman
 *
 */
public class DnDTreeItem extends TreeItem implements HasAllMouseHandlers  {
	public static final int FOLDER = 0;
	public static final int SHARED = 1;
	public static final int TRASH = 2;
	public static final int OTHERS = 3;

	private DnDFocusPanel focus;
	private Widget content;
	private DnDDropController drop;
	private List<TreeItem> toRemove = new ArrayList();
	private boolean draggable = false;
	PopupTree tree;
	public boolean lazy = false;
	public DnDTreeItem(Widget widget, boolean _draggable, PopupTree atree, boolean _lazy){
		this(widget,_draggable,atree);
		lazy = _lazy;
		if(lazy)
			addItem(new TreeItem());
	}
	public DnDTreeItem(Widget widget, boolean _draggable, PopupTree atree) {
		super();
		tree=atree;
		draggable = _draggable;
		content = widget;
		focus = new DnDFocusPanel(content,this);
		focus.setTabIndex(-1);
		setWidget(focus);
	}

	public void setFocus(){
		((DnDFocusPanel)getWidget()).setFocus(true);
	}

	public void updateWidget(Widget widget){
		content = widget;
		focus.setWidget(content);

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
		for(TreeItem it : toRemove)
			if(it instanceof DnDTreeItem)
				if(((DnDTreeItem)it).getDrop() != null)
					GSS.get().getDragController().unregisterDropController(((DnDTreeItem)it).getDrop());
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

	public void undoDroppable(){
		if(drop!=null)
			GSS.get().getDragController().unregisterDropController(getDrop());
	}

	protected void removeItems(TreeItem item){
		for(int i=0;i<item.getChildCount();i++) {
			TreeItem it = item.getChild(i);
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

	public DnDTreeItem getChild(FolderResource folder){
		for(int i=0; i< getChildCount(); i++){
			DnDTreeItem c = (DnDTreeItem) getChild(i);
			if(c.getUserObject() instanceof FolderResource)
				if(((FolderResource)c.getUserObject()).getUri().equals(folder.getUri()))
					return c;
		}
		return null;
	}

	public DnDTreeItem getChild(OtherUserResource user){
		for(int i=0; i< getChildCount(); i++){
			DnDTreeItem c = (DnDTreeItem) getChild(i);
			if(c.getUserObject() instanceof OtherUserResource)
				if(((OtherUserResource)c.getUserObject()).getUri().equals(user.getUri()))
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


	/**
	 * Retrieve the folderResource.
	 *
	 * @return the folderResource
	 */
	public FolderResource getFolderResource() {
		if(getUserObject() instanceof FolderResource)
			return (FolderResource)getUserObject();
		return null;
	}





	/**
	 * Retrieve the sharedResource.
	 *
	 * @return the sharedResource
	 */
	public SharedResource getSharedResource() {
		if(getUserObject() instanceof SharedResource)
			return (SharedResource)getUserObject();
		return null;
	}





	/**
	 * Retrieve the trashResource.
	 *
	 * @return the trashResource
	 */
	public TrashResource getTrashResource() {
		if(getUserObject() instanceof TrashResource)
			return (TrashResource)getUserObject();
		return null;
	}





	/**
	 * Retrieve the othersResource.
	 *
	 * @return the othersResource
	 */
	public OthersResource getOthersResource() {
		if(getUserObject() instanceof OthersResource)
			return (OthersResource)getUserObject();
		return null;
	}


	/**
	 * Retrieve the otherUserResource.
	 *
	 * @return the otherUserResource
	 */
	public OtherUserResource getOtherUserResource() {
		if(getUserObject() instanceof OtherUserResource)
			return (OtherUserResource)getUserObject();
		return null;
	}




	public boolean needExpanding(){
		/*if(GSS.get().getFolders().isMySharedItem(this) && ! GSS.get().getFolders().isMyShares(this)){
			if(getFolderResource() != null){
				SharedResource sr = ((DnDTreeItem)GSS.get().getFolders().getMySharesItem()).getSharedResource();
				int count =0;
				for(String s : getFolderResource().getSubfolderPaths())
					if(sr.getSubfolders().contains(s))
						count++;
				if(count != getChildCount())
					return true;
			}
		}
		else*/
		if(getFolderResource() != null){
			//if(equals(GSS.get().getFolders().getRootItem()))
				//return false;
			if(getFolderResource().getFolders().size() > 0)
				for(FolderResource r : getFolderResource().getFolders() )
					if(r.isNeedsExpanding())
						return true;
			if(getFolderResource().getSubfolderPaths().size() != getChildCount())
				return true;
		}
		else if (getOtherUserResource() != null)
			if(getOtherUserResource().getSubfolderPaths().size() != getChildCount())
				return true;
		return false;
	}

	@Override
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return focus.addMouseDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		focus.fireEvent(event);

	}

	@Override
	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return focus.addMouseUpHandler(handler);
	}

	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return focus.addMouseOutHandler(handler);
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return focus.addMouseOverHandler(handler);
	}

	@Override
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return focus.addMouseMoveHandler(handler);
	}

	@Override
	public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
		return focus.addMouseWheelHandler(handler);
	}




}
