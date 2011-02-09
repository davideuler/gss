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
package gr.ebs.gss.client.clipboard;


/**
 * @author koutsoub
 *
 */
public class Clipboard {
	public final static int CUT=1;
	public final static int COPY=2;
	private ClipboardItem item;

	/**
	 * Retrieve the item.
	 *
	 * @return the item
	 */
	public ClipboardItem getItem() {
		return item;
	}

	/**
	 * Modify the item.
	 *
	 * @param anItem the item to set
	 */
	public void setItem(ClipboardItem anItem) {
		item = anItem;
	}

	public boolean hasFolderOrFileItem(){
		if( item !=null )
			return item.isFileOrFolder();
		return false;
	}

	public boolean hasFileItem(){
		if( item !=null )
			return item.isFile();
		return false;
	}

	public boolean hasUserItem(){
		if( item !=null )
			return item.isUser();
		return false;
	}

	public boolean isEmpty(){
		return item == null;
	}

	public void clear(){
		item = null;
	}
}
