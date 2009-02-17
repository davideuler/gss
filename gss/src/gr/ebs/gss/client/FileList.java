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
import gr.ebs.gss.client.domain.FileHeaderDTO;
import gr.ebs.gss.client.domain.FolderDTO;
import gr.ebs.gss.client.domain.UserDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * A composite that displays the list of files in a particular folder.
 */
public class FileList extends Composite implements TableListener, ClickListener {

	private HTML prevButton = new HTML("<a href='javascript:;'>&lt; Previous</a>", true);

	private HTML nextButton = new HTML("<a href='javascript:;'>Next &gt;</a>", true);

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
	public interface Images extends FileContextMenu.Images, Folders.Images {

		@Resource("gr/ebs/gss/resources/blank.gif")
		AbstractImagePrototype blank();

		@Resource("gr/ebs/gss/resources/asc.png")
		AbstractImagePrototype asc();

		@Resource("gr/ebs/gss/resources/desc.png")
		AbstractImagePrototype desc();
	}

	/**
	 * A label with the number of files in this folder.
	 */
	private HTML countLabel = new HTML();

	/**
	 * The table widget with the file list.
	 */
	private Grid table = new Grid(GSSService.VISIBLE_FILE_COUNT + 1, 8);

	/**
	 * The navigation bar for paginating the results.
	 */
	private HorizontalPanel navBar = new HorizontalPanel();

	/**
	 * The number of files in this folder.
	 */
	int folderFileCount;

	/**
	 * A cache of the files in the list.
	 */
	private List<FileHeaderDTO> files;

	/**
	 * A proxy handler for the remote GSS service.
	 */
	private final GSSServiceAsync service;

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
	/**
	 * Construct the file list widget. This entails setting up the widget
	 * layout, fetching the number of files in the current folder from the
	 * server and filling the local file cache of displayed files with data from
	 * the server, as well.
	 *
	 * @param _images
	 */
	public FileList(final Images _images) {
		images = _images;

		prevButton.addClickListener(this);
		nextButton.addClickListener(this);

		contextMenu = new DnDFocusPanel(new HTML(images.fileContextMenu().getHTML()));
		contextMenu.addClickListener(new FileContextMenu(images, false));
		GSS.get().getDragController().makeDraggable(contextMenu);
		// Set up the service proxy
		service = GSS.get().getRemoteService();

		// Setup the table.
		table.setCellSpacing(0);
		table.setCellPadding(2);
		table.setWidth("100%");

		// Hook up events.
		table.addTableListener(this);

		// Create the 'navigation' bar at the upper-right.
		final HorizontalPanel innerNavBar = new HorizontalPanel();
		innerNavBar.setStyleName("gss-ListNavBar");
		innerNavBar.setSpacing(8);
		innerNavBar.add(prevButton);
		innerNavBar.add(countLabel);
		innerNavBar.add(nextButton);
		navBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		navBar.add(innerNavBar);
		navBar.setWidth("100%");

		initWidget(table);
		setStyleName("gss-List");

		initTable();
		DeferredCommand.addCommand(new IncrementalCommand() {

			public boolean execute() {
				return fetchRootFolder();
			}
		});
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
		sinkEvents(Event.ONCLICK);
		sinkEvents(Event.ONKEYDOWN);
		preventIESelection();
	}

	public void onClick(Widget sender) {
		if (sender == nextButton) {
			// Move forward a page.
			clearSelectedRows();
			startIndex += GSSService.VISIBLE_FILE_COUNT;
			if (startIndex >= folderFileCount)
				startIndex -= GSSService.VISIBLE_FILE_COUNT;
			else
				update();
		} else if (sender == prevButton) {
			clearSelectedRows();
			// Move back a page.
			startIndex -= GSSService.VISIBLE_FILE_COUNT;
			if (startIndex < 0)
				startIndex = 0;
			else
				update();
		}
	}

	public void onBrowserEvent(Event event) {
		if(files ==null || files.size() == 0) return;
		if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && selectedRows.size() != 0) {
			FileContextMenu fm = new FileContextMenu(images, false);
			fm.onClick(contextMenu);
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
				event.preventDefault();
			} else {
				clickShift = false;
				firstShift = -1;
				event.preventDefault();
			}
		}

		super.onBrowserEvent(event);
	}

	/**
	 * Retrieve the root folder for the current user.
	 *
	 * @return true if the retrieval was successful
	 */
	protected boolean fetchRootFolder() {
		UserDTO user = GSS.get().getCurrentUser();
		if (user == null)
			return !DONE;
		updateFileCache(user.getId());
		return DONE;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.TableListener#onCellClicked(com.google.gwt.user.client.ui.SourcesTableEvents,
	 *      int, int)
	 */
	public void onCellClicked(@SuppressWarnings("unused") SourcesTableEvents sender, int row, @SuppressWarnings("unused") int cell) {
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
				contextMenu.setFiles(getSelectedFiles());
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
				contextMenu.setFiles(getSelectedFiles());
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
		nameLabel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				sortFiles("name");
				update();

			}

		});
		versionLabel = new HTML("Version");
		versionLabel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				sortFiles("version");
				update();

			}

		});
		sizeLabel = new HTML("Size");
		sizeLabel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				sortFiles("size");
				update();

			}

		});
		dateLabel = new HTML("Date");
		dateLabel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				sortFiles("date");
				update();

			}

		});
		ownerLabel = new HTML("Owner");
		ownerLabel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				sortFiles("owner");
				update();

			}

		});
		// Create the header row.
		table.setText(0, 0, "");
		// table.setText(0, 1, "Name");
		table.setWidget(0, 1, nameLabel);
		table.setWidget(0, 2, ownerLabel);
		//table.setText(0, 2, "Owner");
		// table.setText(0, 3, "Version");
		table.setWidget(0, 3, versionLabel);
		// table.setText(0, 4, "Size");
		table.setWidget(0, 4, sizeLabel);
		// table.setText(0, 5, "Date");
		table.setWidget(0, 5, dateLabel);
		table.setWidget(0, 6, navBar);
		table.getRowFormatter().setStyleName(0, "gss-ListHeader");

		// Initialize the rest of the rows.
		for (int i = 1; i < GSSService.VISIBLE_FILE_COUNT + 1; ++i) {
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
			// table.getFlexCellFormatter().setColSpan(i, 5, 2);
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
						table.setWidget(prow + 1, 0, images.document().createImage());
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
			contextMenu.setFiles(getSelectedFiles());
			table.setWidget(row + 1, 0, contextMenu);

		}
		/*if (row < folderFileCount) {

			styleRow(selectedRow, false);
			styleRow(row, true);

			previous = selectedRow;
			selectedRow = row;
			GSS.get().setCurrentSelection(files.get(selectedRow));

			if (previous >= 0)
				table.setWidget(previous + 1, 0, images.document().createImage());
			table.setWidget(selectedRow + 1, 0, contextMenu);
		}
		*/
	}

	public List<FileHeaderDTO> getSelectedFiles() {
		List<FileHeaderDTO> result = new ArrayList();
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
	void update() {
		int count = folderFileCount;
		int max = startIndex + GSSService.VISIBLE_FILE_COUNT;
		if (max > count)
			max = count;

		if (folderFileCount == 0) {
			countLabel.setText("no files");
			prevButton.setVisible(false);
			nextButton.setVisible(false);
		} else if (folderFileCount < GSSService.VISIBLE_FILE_COUNT) {
			countLabel.setText(folderFileCount + " files");
			prevButton.setVisible(false);
			nextButton.setVisible(false);
		} else {
			countLabel.setText("" + (startIndex + 1) + " - " + max + " of " + count + " files");
			prevButton.setVisible(startIndex != 0);
			nextButton.setVisible(startIndex + GSSService.VISIBLE_FILE_COUNT < count);
		}
		// Show the selected files.
		int i = 1;
		for (; i < GSSService.VISIBLE_FILE_COUNT + 1; ++i) {
			// Don't read past the end.
			// if (i > folderFileCount)
			// break;
			if (startIndex + i > folderFileCount)
				break;
			// Add a new row to the table, then set each of its columns to the
			// proper values.
			table.setWidget(i, 0, images.document().createImage());
			FileHeaderDTO fileHeader = files.get(startIndex + i - 1);
			table.getRowFormatter().addStyleName(i, "gss-fileRow");
			table.setHTML(i, 1, fileHeader.getName());
			table.setText(i, 2, fileHeader.getOwner().getName());
			table.setText(i, 3, String.valueOf(fileHeader.getVersion()));
			table.setText(i, 4, String.valueOf(fileHeader.getFileSizeAsString()));
			final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy");
			table.setText(i, 5, formatter.format(fileHeader.getAuditInfo().getCreationDate()));

		}

		// Clear any remaining slots.
		for (; i < GSSService.VISIBLE_FILE_COUNT + 1; ++i) {
			table.setHTML(i, 0, "&nbsp;");
			table.setHTML(i, 1, "&nbsp;");
			table.setHTML(i, 2, "&nbsp;");
			table.setHTML(i, 3, "&nbsp;");
			table.setHTML(i, 4, "&nbsp;");
			table.setHTML(i, 5, "&nbsp;");
			table.setHTML(i, 6, "&nbsp;");
		}

		// Reset the selected line.
		// styleRow(selectedRow, false);
		// selectedRow = -1;

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
	 *
	 * @param userId the ID of the current user
	 */
	public void updateFileCache(final Long userId) {
		clearSelectedRows();
		sortingProperty = "name";
		sortingType = true;
		startIndex = 0;
		final TreeItem folderItem = GSS.get().getFolders().getCurrent();
		// Validation.
		if (folderItem == null){
			setFiles(new ArrayList<FileHeaderDTO>());
			update();
			return;
		}
		GSS.get().showLoadingIndicator();
		if (GSS.get().getFolders().isTrash(folderItem))
			service.getDeletedFiles(userId, new AsyncCallback() {

				public void onSuccess(final Object result) {
					final List<FileHeaderDTO> f = (List<FileHeaderDTO>) result;
					setFiles(f);
					GSS.get().hideLoadingIndicator();
					update();
				}

				public void onFailure(final Throwable caught) {
					GWT.log("", caught);
					GSS.get().hideLoadingIndicator();
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			});
		else if (GSS.get().getFolders().isMyShares(folderItem))
			service.getSharedFiles(userId, new AsyncCallback() {

				public void onSuccess(final Object result) {
					final List<FileHeaderDTO> f = (List<FileHeaderDTO>) result;
					setFiles(f);
					GSS.get().hideLoadingIndicator();
					update();
				}

				public void onFailure(final Throwable caught) {
					GWT.log("", caught);
					GSS.get().hideLoadingIndicator();
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			});
		else if (GSS.get().getFolders().isTrashItem(folderItem) || GSS.get().getFolders().isMyShares(folderItem) || GSS	.get()
																														.getFolders()
																														.isOthersShared(folderItem)) {
			setFiles(new ArrayList<FileHeaderDTO>());
			GSS.get().hideLoadingIndicator();
			update();
		} else if (GSS.get().getFolders().isFileItem(folderItem) || GSS.get().getFolders().isMySharedItem(folderItem)) {
			final FolderDTO folder = (FolderDTO) folderItem.getUserObject();

			service.getFiles(userId, folder.getId(), new AsyncCallback() {

				public void onSuccess(final Object result) {
					final List<FileHeaderDTO> f = (List<FileHeaderDTO>) result;
					if (!GSS.get().getFolders().isTrashItem(folderItem))
						setFiles(f);
					GSS.get().hideLoadingIndicator();
					update();
				}

				public void onFailure(final Throwable caught) {
					GWT.log("", caught);
					GSS.get().hideLoadingIndicator();
					if (caught instanceof RpcException)
						GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
					else
						GSS.get().displayError(caught.getMessage());
				}
			});
		} else if (GSS.get().getFolders().isOthersSharedItem(folderItem)) {
			if (folderItem.getUserObject() instanceof UserDTO) {
				UserDTO owner = (UserDTO) folderItem.getUserObject();
				service.getSharedFiles(owner.getId(), userId, new AsyncCallback() {

					public void onSuccess(final Object result) {
						final List<FileHeaderDTO> f = (List<FileHeaderDTO>) result;
						setFiles(f);
						GSS.get().hideLoadingIndicator();
						update();
					}

					public void onFailure(final Throwable caught) {
						GWT.log("", caught);
						GSS.get().hideLoadingIndicator();
						if (caught instanceof RpcException)
							GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
						else
							GSS.get().displayError(caught.getMessage());
					}
				});
			} else {
				FolderDTO folder = (FolderDTO) folderItem.getUserObject();
				UserDTO owner = (UserDTO) GSS.get().getFolders().getUserOfSharedItem(folderItem).getUserObject();
				// TODO: FETCH FILES THAT ARE PERMITTED
				service.getFiles(owner.getId(), folder.getId(), new AsyncCallback() {

					public void onSuccess(final Object result) {
						final List<FileHeaderDTO> f = (List<FileHeaderDTO>) result;
						if (!GSS.get().getFolders().isTrashItem(folderItem))
							setFiles(f);
						GSS.get().hideLoadingIndicator();
						update();
					}

					public void onFailure(final Throwable caught) {
						GWT.log("", caught);
						GSS.get().hideLoadingIndicator();
						if (caught instanceof RpcException)
							GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
						else
							GSS.get().displayError(caught.getMessage());
					}
				});
			}
		}

		else if (folderItem.getUserObject() == null)
			GSS.get().hideLoadingIndicator();
		else
			GSS.get().hideLoadingIndicator();
	}

	/**
	 * Fill the file cache with data.
	 *
	 * @param _files
	 * @param files the files to set
	 */
	void setFiles(final List<FileHeaderDTO> _files) {
		files = _files;
		Collections.sort(files, new Comparator<FileHeaderDTO>() {

			public int compare(FileHeaderDTO arg0, FileHeaderDTO arg1) {
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
		clearLabels();
		clearSelectedRows();
		if(files == null || files.size()==0)
			return;
		Collections.sort(files, new Comparator<FileHeaderDTO>() {

			public int compare(FileHeaderDTO arg0, FileHeaderDTO arg1) {
				if (sortingType)
					if (sortProperty.equals("version")) {
						versionLabel.setHTML("Version&nbsp;" + images.desc().getHTML());
						return new Integer(arg0.getVersion()).compareTo(new Integer(arg1.getVersion()));
					} else if (sortProperty.equals("owner")) {
						ownerLabel.setHTML("Owner&nbsp;" + images.desc().getHTML());
						return new Integer(arg0.getOwner().getName()).compareTo(new Integer(arg1.getOwner().getName()));
					} else if (sortProperty.equals("date")) {
						dateLabel.setHTML("Date&nbsp;" + images.desc().getHTML());
						return arg0.getAuditInfo().getCreationDate().compareTo(arg1.getAuditInfo().getCreationDate());
					} else if (sortProperty.equals("size")) {
						sizeLabel.setHTML("Size&nbsp;" + images.desc().getHTML());
						return new Long(arg0.getFileSize()).compareTo(new Long(arg1.getFileSize()));
					} else if (sortProperty.equals("name")) {
						nameLabel.setHTML("Name&nbsp;" + images.desc().getHTML());
						return arg0.getName().compareTo(arg1.getName());
					} else {
						nameLabel.setHTML("Name&nbsp;" + images.desc().getHTML());
						return arg0.getName().compareTo(arg1.getName());
					}
				else if (sortProperty.equals("version")) {
					versionLabel.setHTML("Version&nbsp;" + images.asc().getHTML());
					return new Integer(arg1.getVersion()).compareTo(new Integer(arg0.getVersion()));
				}
				else if (sortProperty.equals("owner")) {
					ownerLabel.setHTML("Owner&nbsp;" + images.asc().getHTML());
					return new Integer(arg1.getOwner().getName()).compareTo(new Integer(arg0.getOwner().getName()));
				}
				else if (sortProperty.equals("date")) {
					dateLabel.setHTML("Date&nbsp;" + images.asc().getHTML());
					return arg1.getAuditInfo().getCreationDate().compareTo(arg0.getAuditInfo().getCreationDate());
				} else if (sortProperty.equals("size")) {
					sizeLabel.setHTML("Size&nbsp;" + images.asc().getHTML());
					return new Long(arg1.getFileSize()).compareTo(new Long(arg0.getFileSize()));
				} else if (sortProperty.equals("name")) {
					nameLabel.setHTML("Name&nbsp;" + images.asc().getHTML());
					return arg1.getName().compareTo(arg0.getName());
				} else {
					nameLabel.setHTML("Name&nbsp;" + images.asc().getHTML());
					return arg1.getName().compareTo(arg0.getName());
				}
			}

		});
	}

	private void clearLabels() {
		nameLabel.setText("Name");
		versionLabel.setText("Version");
		sizeLabel.setText("Size");
		dateLabel.setText("Date");
		ownerLabel.setText("Owner");
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
			table.setWidget(row + 1, 0, images.document().createImage());
		}
		selectedRows.clear();
		Object sel = GSS.get().getCurrentSelection();
		if (sel instanceof FileHeaderDTO || sel instanceof List)
			GSS.get().setCurrentSelection(null);
	}

	public static native void preventIESelection() /*-{
	     $doc.body.onselectstart = function () { return false; };
	 }-*/;

	public static native void enableIESelection() /*-{
	 if ($doc.body.onselectstart != null)
	     $doc.body.onselectstart = null;
	 }-*/;

	/**
	 *
	 */
	public void selectAllRows() {
		clearSelectedRows();
		int count = folderFileCount;
		if(count == 0)
			return;
		int max = startIndex + GSSService.VISIBLE_FILE_COUNT;
		if (max > count)
			max = count;
		int i = 1;
		for (; i < GSSService.VISIBLE_FILE_COUNT + 1; ++i) {
			// Don't read past the end.
			// if (i > folderFileCount)
			// break;
			if (startIndex + i > folderFileCount)
				break;
			selectedRows.add(startIndex + i-1);
			styleRow(i-1, true);
		}
		GSS.get().setCurrentSelection(getSelectedFiles());
		contextMenu.setFiles(getSelectedFiles());
		table.setWidget(i-1, 0, contextMenu);

	}

}
