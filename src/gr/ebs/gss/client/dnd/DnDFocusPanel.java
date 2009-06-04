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

import gr.ebs.gss.client.rest.resource.FileResource;

import java.util.List;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author kman
 *
 */
public class DnDFocusPanel extends FocusPanel {
	private DnDTreeItem item;
	private List<FileResource> files;

	public DnDFocusPanel(Widget widget, DnDTreeItem anItem) {
		super(widget);
		item = anItem;
	}

	public DnDFocusPanel(Widget widget){
		super(widget);
	}

	/**
	 * Retrieve the item.
	 *
	 * @return the item
	 */
	public DnDTreeItem getItem() {
		return item;
	}

	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<FileResource> getFiles() {
		return files;
	}

	/**
	 * Modify the files.
	 *
	 * @param newFiles the files to set
	 */
	public void setFiles(List<FileResource> newFiles) {
		files = newFiles;
	}

	public HTML cloneHTML(){
		if(getWidget() instanceof HTML){
			HTML ht = (HTML)getWidget();
			HTML res = new HTML(ht.getHTML());
			return res;
		}
		return null;
	}
}