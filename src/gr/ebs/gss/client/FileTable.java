/*
 * Copyright 2010 Electronic Business Systems Ltd.
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
package gr.ebs.gss.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;


/**
 * @author kman
 *
 */
public class FileTable extends Grid{

	/**
	 *
	 */
	public FileTable() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param rows
	 * @param columns
	 */
	public FileTable(int rows, int columns) {
		super(rows, columns);
		// TODO Auto-generated constructor stub
	}

	public int getRowForEvent2(Event event) {
	    Element td = getEventTargetCell(event);
	    if (td == null)
			return -1;

	    Element tr = DOM.getParent(td);
	    Element body = DOM.getParent(tr);
	    int row = DOM.getChildIndex(body, tr);
	    return row;
	  }

}
