/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.dnd.DnDFocusPanel;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.RestCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.SearchResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

/**
 * A composite that displays a list of search results for a particular query on
 * files.
 */
public class SearchResults extends Composite implements  ClickHandler {

	private HTML prevButton = new HTML("<a href='javascript:;'>&lt; Previous</a>", true);

	private HTML nextButton = new HTML("<a href='javascript:;'>Next &gt;</a>", true);

	private String showingStats = "";

	private int startIndex = 0;

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	private boolean clickControl = false;

	private boolean clickShift = false;

	private int firstShift = -1;

	private ArrayList<Integer> selectedRows = new ArrayList<Integer>();

	/**
	 * The context menu for the selected file.
	 */
	final DnDFocusPanel contextMenu;

	/**
	 * Specifies that the images available for this composite will be the ones
	 * available in FileContextMenu.
	 */
	public interface Images extends ClientBundle,FileContextMenu.Images, Folders.Images {

		@Source("gr/ebs/gss/resources/blank.gif")
		ImageResource blank();

		@Source("gr/ebs/gss/resources/asc.png")
		ImageResource asc();

		@Source("gr/ebs/gss/resources/desc.png")
		ImageResource desc();
	}

	/**
	 * A label with the number of files in this folder.
	 */
	private HTML countLabel = new HTML();

	/**
	 * The table widget with the file list.
	 */
	private Grid table;

	/**
	 * The navigation bar for paginating the results.
	 */
	private HorizontalPanel navBar = new HorizontalPanel();

	/**
	 * The number of files in the search results
	 */
	private int folderFileCount;

	/**
	 * Total search results size
	 */
	private long folderTotalSize;

	/**
	 * A cache of the files in the list.
	 */
	private List<FileResource> files;

	/**
	 * The widget's image bundle.
	 */
	private final Images images;

	private String sortingProperty = "name";

	private boolean sortingType = true;

	private HTML nameLabel;

	private HTML versionLabel;

	private HTML sizeLabel;

	private HTML dateLabel;

	private HTML ownerLabel;

	private HTML pathLabel;

	private HTML searchResults = new HTML("Results for search:");

	/**
	 * Construct the file list widget. This entails setting up the widget
	 * layout, fetching the number of files in the current folder from the
	 * server and filling the local file cache of displayed files with data from
	 * the server, as well.
	 *
	 * @param _images
	 */
	public SearchResults(final Images _images) {
		images = _images;
		final GSS app = GSS.get();
		table = new Grid(GSS.VISIBLE_FILE_COUNT + 1, 8) {

			@Override
			public void onBrowserEvent(Event event) {
				if (files == null || files.size() == 0)
					return;
				if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && selectedRows.size() != 0) {
					FileContextMenu fm = new FileContextMenu(images, false, false);
					fm.onEvent(event);
				}
				else if (DOM.eventGetType(event) == Event.ONDBLCLICK)
					if(getSelectedFiles().size() == 1){
						FileResource file = getSelectedFiles().get(0);
						String dateString = RestCommand.getDate();
						String resource = file.getUri().substring(app.getApiPath().length()-1,file.getUri().length());
						String sig = app.getCurrentUserResource().getUsername()+" "+RestCommand.calculateSig("GET", dateString, resource, RestCommand.base64decode(GSS.get().getToken()));
						Window.open(file.getUri() + "?Authorization=" + URL.encodeComponent(sig) + "&Date="+URL.encodeComponent(dateString), "_blank", "");
					}
				if (DOM.eventGetType(event) == Event.ONCLICK) {
					if (DOM.eventGetCtrlKey(event))
						clickControl = true;
					else
						clickControl = false;
					if (DOM.eventGetShiftKey(event)) {
						clickShift = true;
						if (selectedRows.size() == 1)
							firstShift = selectedRows.get(0) - startIndex;
						//event.preventDefault();
					} else {
						clickShift = false;
						firstShift = -1;
						//event.preventDefault();
					}
				}

				super.onBrowserEvent(event);
			}
		};
		prevButton.addClickHandler(this);
		nextButton.addClickHandler(this);

		contextMenu = new DnDFocusPanel(new HTML(AbstractImagePrototype.create(images.fileContextMenu()).getHTML()));
		contextMenu.addClickHandler(new FileContextMenu(images, false, false));
		app.getDragController().makeDraggable(contextMenu);

		// Setup the table.
		table.setCellSpacing(0);
		table.setCellPadding(2);
		table.setWidth("100%");

		// Hook up events.
		table.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Cell cell = table.getCellForEvent(event);
				onRowClicked(cell.getRowIndex());
			}
		});

		// Create the 'navigation' bar at the upper-right.
		HorizontalPanel innerNavBar = new HorizontalPanel();
		innerNavBar.setStyleName("gss-ListNavBar");
		innerNavBar.setSpacing(8);
		innerNavBar.add(prevButton);
		innerNavBar.add(countLabel);
		innerNavBar.add(nextButton);
		navBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		navBar.add(innerNavBar);
		navBar.setWidth("100%");
		VerticalPanel vp = new VerticalPanel();
		vp.add(searchResults);
		searchResults.addStyleName("gss-searchLabel");
		vp.add(table);
		initWidget(vp);
		table.setStyleName("gss-List");
		initTable();
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return fetchRootFolder();
			}
		});
		table.sinkEvents(Event.ONCONTEXTMENU);
		table.sinkEvents(Event.ONMOUSEUP);
		table.sinkEvents(Event.ONCLICK);
		table.sinkEvents(Event.ONKEYDOWN);
		table.sinkEvents(Event.ONDBLCLICK);
		preventIESelection();
	}

	public void onClick(ClickEvent event) {
		if (event.getSource() == nextButton) {
			// Move forward a page.
			clearSelectedRows();
			startIndex += GSS.VISIBLE_FILE_COUNT;
			if (startIndex >= folderFileCount)
				startIndex -= GSS.VISIBLE_FILE_COUNT;
			else
				update(false);
		} else if (event.getSource() == prevButton) {
			clearSelectedRows();
			// Move back a page.
			startIndex -= GSS.VISIBLE_FILE_COUNT;
			if (startIndex < 0)
				startIndex = 0;
			else
				update(false);
		}
	}

	/**
	 * Retrieve the root folder for the current user.
	 *
	 * @return true if the retrieval was successful
	 */
	protected boolean fetchRootFolder() {
		UserResource user = GSS.get().getCurrentUserResource();
		if (user == null)
			return !DONE;
		updateFileCache("");
		return DONE;
	}

	public void onRowClicked(int row) {
		// Select the row that was clicked (-1 to account for header row).
		if (row > folderFileCount)
			return;
		if (clickShift) {
			GWT.log("ROW is:" + row + " fs:" + firstShift, null);
			if (firstShift == -1)
				firstShift = row;
			else if (row > firstShift) {
				clearSelectedRows();
				for (int i = firstShift; i < row; i++) {
					selectedRows.add(startIndex + i);
					styleRow(i, true);
				}
				GSS.get().setCurrentSelection(getSelectedFiles());
				//contextMenu.setFiles(getSelectedFiles());
				table.setWidget(row, 0, contextMenu);
			} else if (row != -1 && row == firstShift) {
				selectedRows.add(row);
				selectedRows.add(row - 1);
				styleRow(row, true);
				styleRow(row - 1, true);
			} else if (row < firstShift) {
				GWT.log("ROW is:" + row + " fs:" + firstShift, null);
				clearSelectedRows();

				for (int i = firstShift; i >= row - 1; i--) {
					selectedRows.add(startIndex + i);
					styleRow(i, true);
				}
				GSS.get().setCurrentSelection(getSelectedFiles());
				table.setWidget(row, 0, contextMenu);
				//contextMenu.setFiles(getSelectedFiles());
			}

		} else if (row > 0)
			selectRow(row - 1);

	}

	/**
	 * Initializes the table so that it contains enough rows for a full page of
	 * files.
	 */
	private void initTable() {

		nameLabel = new HTML("Name");
		nameLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortFiles("name");
			}

		});
		versionLabel = new HTML("Version");
		versionLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortFiles("version");
			}

		});
		sizeLabel = new HTML("Size");
		sizeLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortFiles("size");
			}

		});
		dateLabel = new HTML("Last modified");
		dateLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortFiles("date");
			}

		});
		ownerLabel = new HTML("Owner");
		ownerLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortFiles("owner");
			}

		});

		pathLabel = new HTML("Path");
		pathLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortFiles("path");
			}

		});
		// Create the header row.
		table.setText(0, 0, "");
		// table.setText(0, 1, "Name");
		table.setWidget(0, 1, nameLabel);
		table.setWidget(0, 2, ownerLabel);
		// table.setText(0, 3, "Version");
		table.setWidget(0, 3, pathLabel);
		table.setWidget(0, 4, versionLabel);
		// table.setText(0, 4, "Size");
		table.setWidget(0, 5, sizeLabel);
		// table.setText(0, 5, "Last modified");
		table.setWidget(0, 6, dateLabel);
		table.setWidget(0, 7, navBar);
		table.getRowFormatter().setStyleName(0, "gss-ListHeader");

		// Initialize the rest of the rows.
		for (int i = 1; i < GSS.VISIBLE_FILE_COUNT + 1; ++i) {
			table.setText(i, 0, "");
			table.setText(i, 1, "");
			table.setText(i, 2, "");
			table.setText(i, 3, "");
			table.setText(i, 4, "");
			table.setText(i, 5, "");
			table.setText(i, 6, "");
			table.getCellFormatter().setWordWrap(i, 0, false);
			table.getCellFormatter().setWordWrap(i, 1, false);
			table.getCellFormatter().setWordWrap(i, 2, false);
			table.getCellFormatter().setWordWrap(i, 3, false);
			table.getCellFormatter().setWordWrap(i, 4, false);
			table.getCellFormatter().setWordWrap(i, 5, false);
			table.getCellFormatter().setWordWrap(i, 6, false);
			table.getCellFormatter().setHorizontalAlignment(i, 4, HasHorizontalAlignment.ALIGN_CENTER);
		}
		prevButton.setVisible(false);
		nextButton.setVisible(false);
	}

	/**
	 * Selects the given row (relative to the current page).
	 *
	 * @param row the row to be selected
	 */
	private void selectRow(final int row) {
		if (row < folderFileCount) {
			if (clickControl)
				if (selectedRows.contains(row)) {
					int i = selectedRows.indexOf(startIndex + row);
					selectedRows.remove(i);
					styleRow(row, false);
				} else {
					for (int r : selectedRows) {
						int prow = r - startIndex;
						table.setWidget(prow + 1, 0, AbstractImagePrototype.create(images.document()).createImage());
					}
					selectedRows.add(startIndex + row);
					styleRow(row, true);
				}
			else {
				clearSelectedRows();
				selectedRows.add(startIndex + row);
				styleRow(row, true);
			}
			if (selectedRows.size() == 1)
				GSS.get().setCurrentSelection(files.get(selectedRows.get(0)));
			else
				GSS.get().setCurrentSelection(getSelectedFiles());
			//contextMenu.setFiles(getSelectedFiles());
			table.setWidget(row + 1, 0, contextMenu);
		}
	}

	public List<FileResource> getSelectedFiles() {
		List<FileResource> result = new ArrayList();
		for (int i : selectedRows)
			result.add(files.get(i));
		return result;
	}

	/**
	 * Make the specified row look like selected or not, according to the
	 * <code>selected</code> flag.
	 *
	 * @param row
	 * @param selected
	 */
	void styleRow(final int row, final boolean selected) {
		if (row != -1 && row >= 0)
			if (selected)
				table.getRowFormatter().addStyleName(row + 1, "gss-SelectedRow");
			else
				table.getRowFormatter().removeStyleName(row + 1, "gss-SelectedRow");
	}

	/**
	 * Update the display of the file list.
	 */
	void update(boolean sort) {
		int count = folderFileCount;
		int max = startIndex + GSS.VISIBLE_FILE_COUNT;
		if (max > count)
			max = count;
		folderTotalSize = 0;

		if (sort && files != null && files.size() != 0) {
			clearLabels();
			clearSelectedRows();

			Collections.sort(files, new Comparator<FileResource>() {

				public int compare(FileResource arg0, FileResource arg1) {
					AbstractImagePrototype descPrototype = AbstractImagePrototype.create(images.desc());
					AbstractImagePrototype ascPrototype = AbstractImagePrototype.create(images.asc());
					if (sortingType)
						if (sortingProperty.equals("version")) {
							versionLabel.setHTML("Version&nbsp;" + descPrototype.getHTML());
							return arg0.getVersion().compareTo(arg1.getVersion());
						} else if (sortingProperty.equals("owner")) {
							ownerLabel.setHTML("Owner&nbsp;" + descPrototype.getHTML());
							return arg0.getOwner().compareTo(arg1.getOwner());
						} else if (sortingProperty.equals("date")) {
							dateLabel.setHTML("Last modified&nbsp;" + descPrototype.getHTML());
							return arg0.getModificationDate().compareTo(arg1.getModificationDate());
						} else if (sortingProperty.equals("size")) {
							sizeLabel.setHTML("Size&nbsp;" + descPrototype.getHTML());
							return arg0.getContentLength().compareTo(arg1.getContentLength());
						} else if (sortingProperty.equals("name")) {
							nameLabel.setHTML("Name&nbsp;" + descPrototype.getHTML());
							return arg0.getName().compareTo(arg1.getName());
						} else if (sortingProperty.equals("path")) {
							pathLabel.setHTML("Path&nbsp;" + descPrototype.getHTML());
							return arg0.getUri().compareTo(arg1.getUri());
						} else {
							nameLabel.setHTML("Name&nbsp;" + descPrototype.getHTML());
							return arg0.getName().compareTo(arg1.getName());
						}
					else if (sortingProperty.equals("version")) {
						versionLabel.setHTML("Version&nbsp;" + ascPrototype.getHTML());
						return arg1.getVersion().compareTo(arg0.getVersion());
					} else if (sortingProperty.equals("owner")) {
						ownerLabel.setHTML("Owner&nbsp;" + ascPrototype.getHTML());
						return arg1.getOwner().compareTo(arg0.getOwner());
					} else if (sortingProperty.equals("date")) {
						dateLabel.setHTML("Last modified&nbsp;" + ascPrototype.getHTML());
						return arg1.getModificationDate().compareTo(arg0.getModificationDate());
					} else if (sortingProperty.equals("size")) {
						sizeLabel.setHTML("Size&nbsp;" + ascPrototype.getHTML());
						return arg1.getContentLength().compareTo(arg0.getContentLength());
					} else if (sortingProperty.equals("name")) {
						nameLabel.setHTML("Name&nbsp;" + ascPrototype.getHTML());
						return arg1.getName().compareTo(arg0.getName());
					} else if (sortingProperty.equals("path")) {
						pathLabel.setHTML("Path&nbsp;" + ascPrototype.getHTML());
						return arg1.getUri().compareTo(arg0.getUri());
					} else {
						nameLabel.setHTML("Name&nbsp;" + ascPrototype.getHTML());
						return arg1.getName().compareTo(arg0.getName());
					}
				}

			});

		}
		// Show the selected files.
		int i = 1;
		for (; i < GSS.VISIBLE_FILE_COUNT + 1; ++i) {
			// Don't read past the end.
			// if (i > folderFileCount)
			// break;
			if (startIndex + i > folderFileCount)
				break;
			// Add a new row to the table, then set each of its columns to the
			// proper values.
			table.setWidget(i, 0, AbstractImagePrototype.create(images.document()).createImage());
			FileResource fileHeader = files.get(startIndex + i - 1);
			table.getRowFormatter().addStyleName(i, "gss-fileRow");
			table.setHTML(i, 1, fileHeader.getName());
			table.setText(i, 2, fileHeader.getOwner());
			table.setText(i, 3, fileHeader.getPath());
			table.setText(i, 4, String.valueOf(fileHeader.getVersion()));
			table.setText(i, 5, String.valueOf(fileHeader.getFileSizeAsString()));
			final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
			table.setText(i, 6, formatter.format(fileHeader.getModificationDate()));
			folderTotalSize += fileHeader.getContentLength();
		}

		// Clear any remaining slots.
		for (; i < GSS.VISIBLE_FILE_COUNT + 1; ++i) {
			table.setHTML(i, 0, "&nbsp;");
			table.setHTML(i, 1, "&nbsp;");
			table.setHTML(i, 2, "&nbsp;");
			table.setHTML(i, 3, "&nbsp;");
			table.setHTML(i, 4, "&nbsp;");
			table.setHTML(i, 5, "&nbsp;");
			table.setHTML(i, 6, "&nbsp;");
			table.setHTML(i, 7, "&nbsp;");
		}

		if (folderFileCount == 0) {
			showingStats = "no files";
			prevButton.setVisible(false);
			nextButton.setVisible(false);
		} else if (folderFileCount < GSS.VISIBLE_FILE_COUNT) {
			if (folderFileCount == 1)
				showingStats = "1 file";
			else
				showingStats = folderFileCount + " files";
			showingStats += " (" + FileResource.getFileSizeAsString(folderTotalSize) + ")";
			prevButton.setVisible(false);
			nextButton.setVisible(false);
		} else {
			showingStats = "" + (startIndex + 1) + " - " + max + " of " + count + " files" + " (" + FileResource.getFileSizeAsString(folderTotalSize) + ")";
			prevButton.setVisible(startIndex != 0);
			nextButton.setVisible(startIndex + GSS.VISIBLE_FILE_COUNT < count);
		}
		updateCurrentlyShowingStats();
	}

	/**
	 *  update status panel with currently showing file stats
	 */
	public void updateCurrentlyShowingStats() {
		GSS.get().getStatusPanel().updateCurrentlyShowing(showingStats);
	}

	/**
	 * Adjust the height of the table by adding and removing rows as necessary.
	 *
	 * @param newHeight the new height to reach
	 */
	void resizeTableHeight(final int newHeight) {
		GWT.log("Panel: " + newHeight + ", parent: " + table.getParent().getOffsetHeight(), null);
		// Fill the rest with empty slots.
		if (newHeight > table.getOffsetHeight())
			while (newHeight > table.getOffsetHeight()) {
				// table.setHTML(table.getRowCount(), 5, "&nbsp;");
				table.resizeRows(table.getRowCount() + 1);
				GWT.log("Table: " + table.getOffsetHeight() + ", rows: " + table.getRowCount(), null);
			}
		else
			while (newHeight < table.getOffsetHeight()) {
				// table.setHTML(table.getRowCount(), 5, "&nbsp;");
				table.resizeRows(table.getRowCount() - 1);
				GWT.log("Table: " + table.getOffsetHeight() + ", rows: " + table.getRowCount(), null);
			}
	}

	/**
	 * Update the file cache with data from the server.
	 */
	public void updateFileCache(String query) {
		final GSS app = GSS.get();
		clearSelectedRows();
		clearLabels();
		startIndex = 0;
		app.showLoadingIndicator();
		if (query == null || query.trim().equals("")) {
			searchResults.setHTML("You must specify a query");
			setFiles(new ArrayList());
			update(true);
			app.hideLoadingIndicator();
		} else{
			searchResults.setHTML("Search results for " + query);

			GetCommand<SearchResource> eg = new GetCommand<SearchResource>(SearchResource.class,
						app.getApiPath() + "search/" + URL.encodeComponent(query)) {

				@Override
				public void onComplete() {
					SearchResource s = getResult();
					setFiles(s.getFiles());
					update(true);
				}

				@Override
				public void onError(Throwable t) {
					if(t instanceof RestException)
						app.displayError("Unable to perform search:"+((RestException)t).getHttpStatusText());
					else
						app.displayError("System error performing search:"+t.getMessage());
					updateFileCache("");
				}

			};
			DeferredCommand.addCommand(eg);
		}
	}

	/**
	 * Fill the file cache with data.
	 *
	 * @param _files
	 * @param filePaths the files to set
	 */
	private void setFiles(List<FileResource> _files) {
		files = _files;
		Collections.sort(files, new Comparator<FileResource>() {

			public int compare(FileResource arg0, FileResource arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}

		});
		folderFileCount = files.size();
		GWT.log("File count:" + folderFileCount, null);
	}

	private void sortFiles(final String sortProperty) {
		if (sortProperty.equals(sortingProperty))
			sortingType = !sortingType;
		else {
			sortingProperty = sortProperty;
			sortingType = true;
		}
		update(true);
	}

	private void clearLabels() {
		nameLabel.setText("Name");
		versionLabel.setText("Version");
		sizeLabel.setText("Size");
		dateLabel.setText("Last modified");
		ownerLabel.setText("Owner");
		pathLabel.setText("Path");
	}

	/**
	 * Retrieve the table.
	 *
	 * @return the table
	 */
	Grid getTable() {
		return table;
	}

	/**
	 * Does the list contains the requested filename
	 *
	 * @param fileName
	 * @return true/false
	 */
	public boolean contains(String fileName) {
		for (int i = 0; i < files.size(); i++)
			if (files.get(i).getName().equals(fileName))
				return true;
		return false;
	}

	public void clearSelectedRows() {
		for (int r : selectedRows) {
			int row = r - startIndex;
			styleRow(row, false);
			table.setWidget(row + 1, 0, AbstractImagePrototype.create(images.document()).createImage());
		}
		selectedRows.clear();
		Object sel = GSS.get().getCurrentSelection();
		if (sel instanceof FileResource || sel instanceof List)
			GSS.get().setCurrentSelection(null);
	}

	public static native void preventIESelection() /*-{
	     $doc.body.onselectstart = function () { return false; };
	 }-*/;

	public static native void enableIESelection() /*-{
	 if ($doc.body.onselectstart != null)
	     $doc.body.onselectstart = null;
	 }-*/;

	public void selectAllRows() {
		clearSelectedRows();
		int count = folderFileCount;
		if (count == 0)
			return;
		int max = startIndex + GSS.VISIBLE_FILE_COUNT;
		if (max > count)
			max = count;
		int i = 1;
		for (; i < GSS.VISIBLE_FILE_COUNT + 1; ++i) {
			// Don't read past the end.
			// if (i > folderFileCount)
			// break;
			if (startIndex + i > folderFileCount)
				break;
			selectedRows.add(startIndex + i - 1);
			styleRow(i - 1, true);
		}
		GSS.get().setCurrentSelection(getSelectedFiles());
		//contextMenu.setFiles(getSelectedFiles());
		table.setWidget(i - 1, 0, contextMenu);
	}

}
