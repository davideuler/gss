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

import gr.ebs.gss.client.dnd.DnDSimpleFocusPanel;
import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.MultipleHeadCommand;
import gr.ebs.gss.client.rest.RestCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * A composite that displays the list of files in a particular folder.
 */
public class FileList extends Composite {

	interface TableResources extends CellTable.Resources {
	    @Source({CellTable.Style.DEFAULT_CSS, "GssCellTable.css"})
	    TableStyle cellTableStyle();
	  }
	
	/**
	   * The styles applied to the table.
	   */
	  interface TableStyle extends CellTable.Style {
	  }

	private String showingStats = "";

	private int startIndex = 0;

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	/**
	 * The context menu for the selected file.
	 */
	final DnDSimpleFocusPanel contextMenu;
	
	private final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");

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

		@Source("gr/ebs/gss/resources/mimetypes/document_shared.png")
		ImageResource documentShared();

		@Source("gr/ebs/gss/resources/mimetypes/kcmfontinst.png")
		ImageResource wordprocessor();

		@Source("gr/ebs/gss/resources/mimetypes/log.png")
		ImageResource spreadsheet();

		@Source("gr/ebs/gss/resources/mimetypes/kpresenter_kpr.png")
		ImageResource presentation();

		@Source("gr/ebs/gss/resources/mimetypes/acroread.png")
		ImageResource pdf();

		@Source("gr/ebs/gss/resources/mimetypes/image.png")
		ImageResource image();

		@Source("gr/ebs/gss/resources/mimetypes/video2.png")
		ImageResource video();

		@Source("gr/ebs/gss/resources/mimetypes/knotify.png")
		ImageResource audio();

		@Source("gr/ebs/gss/resources/mimetypes/html.png")
		ImageResource html();

		@Source("gr/ebs/gss/resources/mimetypes/txt.png")
		ImageResource txt();

		@Source("gr/ebs/gss/resources/mimetypes/ark2.png")
		ImageResource zip();

		@Source("gr/ebs/gss/resources/mimetypes/kcmfontinst_shared.png")
		ImageResource wordprocessorShared();

		@Source("gr/ebs/gss/resources/mimetypes/log_shared.png")
		ImageResource spreadsheetShared();

		@Source("gr/ebs/gss/resources/mimetypes/kpresenter_kpr_shared.png")
		ImageResource presentationShared();

		@Source("gr/ebs/gss/resources/mimetypes/acroread_shared.png")
		ImageResource pdfShared();

		@Source("gr/ebs/gss/resources/mimetypes/image_shared.png")
		ImageResource imageShared();

		@Source("gr/ebs/gss/resources/mimetypes/video2_shared.png")
		ImageResource videoShared();

		@Source("gr/ebs/gss/resources/mimetypes/knotify_shared.png")
		ImageResource audioShared();

		@Source("gr/ebs/gss/resources/mimetypes/html_shared.png")
		ImageResource htmlShared();

		@Source("gr/ebs/gss/resources/mimetypes/txt_shared.png")
		ImageResource txtShared();

		@Source("gr/ebs/gss/resources/mimetypes/ark2_shared.png")
		ImageResource zipShared();

	}

	/**
	 * A label with the number of files in this folder.
	 */
	private HTML countLabel = new HTML();

	/**
	 * The table widget with the file list.
	 */
	//private FileTable table = new FileTable(GSS.VISIBLE_FILE_COUNT + 1, 8);

	/**
	 * The navigation bar for paginating the results.
	 */
	private HorizontalPanel navBar = new HorizontalPanel();

	/**
	 * The number of files in this folder.
	 */
	int folderFileCount;

	/**
	 * Total folder size
	 */
	long folderTotalSize;

	/**
	 * A cache of the files in the list.
	 */
	private List<FileResource> files;

	/**
	 * The widget's image bundle.
	 */
	private final Images images;

	
	private FileContextMenu menuShowing;
	private CellTable<FileResource> celltable;
	private final MultiSelectionModel<FileResource> selectionModel;
	private final List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();
	SortableHeader nameHeader;
	/**
	 * Construct the file list widget. This entails setting up the widget
	 * layout, fetching the number of files in the current folder from the
	 * server and filling the local file cache of displayed files with data from
	 * the server, as well.
	 *
	 * @param _images
	 */
	public FileList(Images _images) {
		images = _images;
		CellTable.Resources resources = GWT.create(TableResources.class);
		
		contextMenu = new DnDSimpleFocusPanel(new HTML(AbstractImagePrototype.create(images.fileContextMenu()).getHTML()));
		GSS.get().getDragController().makeDraggable(contextMenu);

		// Setup the table.
		

		// Create the 'navigation' bar at the upper-right.
		HorizontalPanel innerNavBar = new HorizontalPanel();
		innerNavBar.setStyleName("gss-ListNavBar");
		innerNavBar.setSpacing(8);
		innerNavBar.add(countLabel);
		navBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		navBar.add(innerNavBar);
		navBar.setWidth("100%");
		ProvidesKey<FileResource> keyProvider = new ProvidesKey<FileResource>(){

			@Override
			public Object getKey(FileResource item) {
				return item.getUri();
			}
			
		};
		final TextColumn<FileResource> nameColumn = new TextColumn<FileResource>() {

			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getName();
			}
			
			
		};
		celltable = new CellTable<FileResource>(100,resources,keyProvider){
			@Override
			protected void onBrowserEvent2(Event event) {
				/*if (DOM.eventGetType((Event) event) == Event.ONMOUSEDOWN && DOM.eventGetButton((Event) event) == NativeEvent.BUTTON_RIGHT){
					fireClickEvent((Element) event.getEventTarget().cast());					
				}*/
				super.onBrowserEvent2(event);
			}
		};
		
		
		Column<FileResource, ImageResource> status = new Column<FileResource, ImageResource>(new ImageResourceCell()) {
	          @Override
	          public ImageResource getValue(FileResource entity) {
	            return getFileIcon(entity);
	          }
	       };
	       celltable.addColumn(status,"");
		
		
		celltable.addColumn(nameColumn,nameHeader = new SortableHeader("Name"));
		allHeaders.add(nameHeader);
		nameHeader.setSorted(true);
		nameHeader.toggleReverseSort();
		nameHeader.setUpdater(new FileValueUpdater(nameHeader, "name"));
		celltable.redrawHeaders();
		SortableHeader aheader;
		celltable.addColumn(new TextColumn<FileResource>() {
			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getOwner();
			}			
		},aheader = new SortableHeader("Owner"));
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "owner"));
		celltable.addColumn(new TextColumn<FileResource>() {
			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getPath();
			}			
		},aheader = new SortableHeader("Path"));
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "path"));	
		celltable.addColumn(new TextColumn<FileResource>() {
			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getVersion().toString();
			}			
		},aheader = new SortableHeader("Version"));
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "version"));
		celltable.addColumn(new TextColumn<FileResource>() {
			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getFileSizeAsString();
			}			
		},aheader = new SortableHeader("Size"));
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "size"));	
		celltable.addColumn(new TextColumn<FileResource>() {
			@Override
			public String getValue(FileResource object) {
				return formatter.format(object.getModificationDate());
			}			
		},aheader = new SortableHeader("Last Modified"));
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "date"));
		initWidget(celltable);
		setStyleName("gss-List");
		selectionModel = new MultiSelectionModel<FileResource>();
		

		 Handler selectionHandler = new SelectionChangeEvent.Handler() { 
             @Override 
             public void onSelectionChange(com.google.gwt.view.client.SelectionChangeEvent event) {
            	 if(getSelectedFiles().size()==1)
            		 GSS.get().setCurrentSelection(getSelectedFiles().get(0));
            	 else
            		 GSS.get().setCurrentSelection(getSelectedFiles());
 				contextMenu.setFiles(getSelectedFiles());
             }
         };
         selectionModel.addSelectionChangeHandler(selectionHandler);
         
		celltable.setSelectionModel(selectionModel,GSSSelectionEventManager.<FileResource>createDefaultManager());
		celltable.setPageSize(GSS.VISIBLE_FILE_COUNT);
		celltable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		Scheduler.get().scheduleIncremental(new RepeatingCommand() {

			@Override
			public boolean execute() {
				return fetchRootFolder();
			}
		});
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
		sinkEvents(Event.ONMOUSEDOWN);
		sinkEvents(Event.ONCLICK);
		sinkEvents(Event.ONKEYDOWN);
		sinkEvents(Event.ONDBLCLICK);
		GSS.preventIESelection();
	}
	
	//public native void fireClickEvent(Element element) /*-{
	  //  var evObj = $doc.createEvent('MouseEvents');
	    //evObj.initEvent('click', true, true);
	    //element.dispatchEvent(evObj);
  	//}-*/;

	 public List<FileResource> getSelectedFiles() {
         return new ArrayList<FileResource>(selectionModel.getSelectedSet());
	 }
	
	

	@Override
	public void onBrowserEvent(Event event) {
		
		if (files == null || files.size() == 0) {
			if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
				menuShowing = new FileContextMenu(images, false, true);
				menuShowing=menuShowing.onEmptyEvent(event);
			}
			return;
		}
		if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() != 0) {
			GWT.log("*****GOING TO SHOW CONTEXT MENU ****", null);
			menuShowing =  new FileContextMenu(images, false, false);
			menuShowing=menuShowing.onEvent(event);
		} else if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
			menuShowing = new FileContextMenu(images, false, true);
			menuShowing=menuShowing.onEmptyEvent(event);
		} else if (DOM.eventGetType(event) == Event.ONDBLCLICK)
			if (getSelectedFiles().size() == 1) {
				GSS app = GSS.get();
				FileResource file = getSelectedFiles().get(0);
				String dateString = RestCommand.getDate();
				String resource = file.getUri().substring(app.getApiPath().length() - 1, file.getUri().length());
				String sig = app.getCurrentUserResource().getUsername() + " " +
						RestCommand.calculateSig("GET", dateString, resource,
						RestCommand.base64decode(app.getToken()));
				Window.open(file.getUri() + "?Authorization=" + URL.encodeComponent(sig) + "&Date=" + URL.encodeComponent(dateString), "_blank", "");
				event.preventDefault();
				return;
			}
		super.onBrowserEvent(event);
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
		// Update cache and clear selection.
		updateFileCache(true);
		return DONE;
	}

	

	
	/**
	 * Make the specified row look like selected or not, according to the
	 * <code>selected</code> flag.
	 *
	 * @param row
	 * @param selected
	 */
	void styleRow(final int row, final boolean selected) {
		
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
		
		celltable.setRowCount(files.size());
		celltable.setRowData(0,files);
		celltable.redrawHeaders();
		if (folderFileCount == 0) {
			showingStats = "no files";
		} else if (folderFileCount < GSS.VISIBLE_FILE_COUNT) {
			if (folderFileCount == 1)
				showingStats = "1 file";
			else
				showingStats = folderFileCount + " files";
			showingStats += " (" + FileResource.getFileSizeAsString(folderTotalSize) + ")";
		} else {
			showingStats = "" + (startIndex + 1) + " - " + max + " of " + count + " files" + " (" + FileResource.getFileSizeAsString(folderTotalSize) + ")";
		}
		updateCurrentlyShowingStats();

	}

	/**
	 * Return the proper icon based on the MIME type of the file.
	 *
	 * @param file
	 * @return the icon
	 */
	private ImageResource getFileIcon(FileResource file) {
		String mimetype = file.getContentType();
		boolean shared = false;
		Folders folders = GSS.get().getFolders();
		if(folders.getCurrent() != null && folders.isOthersSharedItem(folders.getCurrent())){
			DnDTreeItem otherUser = (DnDTreeItem) folders.getUserOfSharedItem(folders.getCurrent());
			if(otherUser==null)
				shared = false;
			else{
				String uname = otherUser.getOtherUserResource().getUsername();
				if(uname==null)
					uname = ((DnDTreeItem)folders.getSharesItem()).getOthersResource().getUsernameOfUri(otherUser.getOtherUserResource().getUri());
				if(uname != null)
					shared = file.isShared();
			}
		}
		else
			shared = file.isShared();
		if (mimetype == null)
			return shared ? images.documentShared() : images.document();
		mimetype = mimetype.toLowerCase();
		if (mimetype.startsWith("application/pdf"))
			return shared ? images.pdfShared() : images.pdf();
		else if (mimetype.endsWith("excel"))
			return shared ? images.spreadsheetShared() : images.spreadsheet();
		else if (mimetype.endsWith("msword"))
			return shared ? images.wordprocessorShared() : images.wordprocessor();
		else if (mimetype.endsWith("powerpoint"))
			return shared ? images.presentationShared() : images.presentation();
		else if (mimetype.startsWith("application/zip") ||
					mimetype.startsWith("application/gzip") ||
					mimetype.startsWith("application/x-gzip") ||
					mimetype.startsWith("application/x-tar") ||
					mimetype.startsWith("application/x-gtar"))
			return shared ? images.zipShared() : images.zip();
		else if (mimetype.startsWith("text/html"))
			return shared ? images.htmlShared() : images.html();
		else if (mimetype.startsWith("text/plain"))
			return shared ? images.txtShared() : images.txt();
		else if (mimetype.startsWith("image/"))
			return shared ? images.imageShared() : images.image();
		else if (mimetype.startsWith("video/"))
			return shared ? images.videoShared() : images.video();
		else if (mimetype.startsWith("audio/"))
			return shared ? images.audioShared() : images.audio();
		return shared ? images.documentShared() : images.document();
	}

	/**
	 * Update status panel with currently showing file stats.
	 */
	public void updateCurrentlyShowingStats() {
		GSS.get().getStatusPanel().updateCurrentlyShowing(showingStats);
	}

	/**
	 * Adjust the height of the table by adding and removing rows as necessary.
	 *
	 * @param newHeight the new height to reach
	 */
	//void resizeTableHeight(final int newHeight) {
		/*GWT.log("Panel: " + newHeight + ", parent: " + table.getParent().getOffsetHeight(), null);
		// Fill the rest with empty slots.
		if (newHeight > table.getOffsetHeight())
			while (newHeight > table.getOffsetHeight()) {
				table.resizeRows(table.getRowCount() + 1);
				GWT.log("Table: " + table.getOffsetHeight() + ", rows: " + table.getRowCount(), null);
			}
		else
			while (newHeight < table.getOffsetHeight()) {
				table.resizeRows(table.getRowCount() - 1);
				GWT.log("Table: " + table.getOffsetHeight() + ", rows: " + table.getRowCount(), null);
			}*/
	//}

	public void updateFileCache(boolean updateSelectedFolder, final boolean clearSelection) {
		updateFileCache(updateSelectedFolder, clearSelection, null);
	}

	public void updateFileCache(boolean updateSelectedFolder, final boolean clearSelection, final String newFilename) {
		if (!updateSelectedFolder && !GSS.get().getFolders().getCurrent().equals(GSS.get().getFolders().getTrashItem()))
			updateFileCache(clearSelection);
		else if (GSS.get().getFolders().getCurrent() != null) {
			final DnDTreeItem folderItem = (DnDTreeItem) GSS.get().getFolders().getCurrent();
			if (folderItem.getFolderResource() != null) {
				if(GSS.get().getFolders().isFileItem(folderItem) || GSS.get().getFolders().isMySharedItem(folderItem)  || GSS.get().getFolders().isOthersSharedItem(folderItem) ){
				update(true);
				GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, folderItem.getFolderResource().getUri(),folderItem.getFolderResource()) {

					@Override
					public void onComplete() {
						folderItem.setUserObject(getResult());
								if(GSS.get().getFolders().isFileItem(folderItem)){
							String[] filePaths = new String[folderItem.getFolderResource().getFilePaths().size()];
							int c=0;
							for(String fpath : folderItem.getFolderResource().getFilePaths()){
								filePaths[c] = fpath + "?" + Math.random();
								c++;
							}
							MultipleHeadCommand<FileResource> getFiles = new MultipleHeadCommand<FileResource>(FileResource.class, filePaths, folderItem.getFolderResource().getFileCache()){

								@Override
								public void onComplete(){
									List<FileResource> result = getResult();
									//remove random from path
									for(FileResource r : result){
										String p = r.getUri();
										int indexOfQuestionMark = p.lastIndexOf('?');
										if(indexOfQuestionMark>0)
											r.setUri(p.substring(0, indexOfQuestionMark));
										GWT.log("FETCHED:"+r.getLastModifiedSince(), null);
									}
									folderItem.getFolderResource().setFiles(result);
									folderItem.getFolderResource().setFilesExpanded(true);
									updateFileCache(clearSelection, newFilename);
								}

								@Override
								public void onError(String p, Throwable throwable) {
									if(throwable instanceof RestException)
										GSS.get().displayError("Unable to retrieve file details:"+((RestException)throwable).getHttpStatusText());
								}

								@Override
								public void onError(Throwable t) {
									GWT.log("", t);
									GSS.get().displayError("Unable to fetch files for folder " + folderItem.getFolderResource().getName());
								}

							};
							DeferredCommand.addCommand(getFiles);
						}
						else
							updateFileCache(clearSelection, newFilename);
					}

					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch folder " + folderItem.getFolderResource().getName());
					}
				};
				DeferredCommand.addCommand(gf);
				}
			}
			else if (folderItem.getTrashResource() != null) {
				GetCommand<TrashResource> gt = new GetCommand<TrashResource>(TrashResource.class, folderItem.getTrashResource().getUri(), null) {

					@Override
					public void onComplete() {
						folderItem.setUserObject(getResult());
						updateFileCache(clearSelection);
					}

					@Override
					public void onError(Throwable t) {
						if (t instanceof RestException && (((RestException) t).getHttpStatusCode() == 204 || ((RestException) t).getHttpStatusCode() == 1223)) {
							folderItem.setUserObject(new TrashResource(folderItem.getTrashResource().getUri()));
							updateFileCache(clearSelection);
						} else {
							GWT.log("", t);
							GSS.get().displayError("Unable to fetch trash resource");
						}
					}
				};
				DeferredCommand.addCommand(gt);
			} else if (folderItem.getSharedResource() != null) {
				GetCommand<SharedResource> gt = new GetCommand<SharedResource>(SharedResource.class, folderItem.getSharedResource().getUri(), null) {

					@Override
					public void onComplete() {
						folderItem.setUserObject(getResult());
						for(FileResource r : folderItem.getSharedResource().getFiles()){
									String p = r.getUri();
									int indexOfQuestionMark = p.lastIndexOf('?');
									if(indexOfQuestionMark>0)
										r.setUri(p.substring(0, indexOfQuestionMark));
									GWT.log("FETCHED:"+r.getLastModifiedSince(), null);
								}
								folderItem.getSharedResource().setFilesExpanded(true);
								updateFileCache(clearSelection, newFilename);
						
					}

					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch My Shares resource");
					}
				};
				DeferredCommand.addCommand(gt);
			} else if (folderItem.getOtherUserResource() != null) {
				GetCommand<OtherUserResource> gt = new GetCommand<OtherUserResource>(OtherUserResource.class, folderItem.getOtherUserResource().getUri(), null) {

					@Override
					public void onComplete() {
						folderItem.setUserObject(getResult());
						//updateFileCache(clearSelection, newFilename);
						for(FileResource r : folderItem.getOtherUserResource().getFiles()){
									String p = r.getUri();
									int indexOfQuestionMark = p.lastIndexOf('?');
									if(indexOfQuestionMark>0)
										r.setUri(p.substring(0, indexOfQuestionMark));
									GWT.log("FETCHED:"+r.getLastModifiedSince(), null);
								}
								folderItem.getOtherUserResource().setFilesExpanded(true);
								updateFileCache(clearSelection, newFilename);
						
					}

					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch My Shares resource");
					}
				};
				DeferredCommand.addCommand(gt);
			}
		} else
			updateFileCache(clearSelection);
	}


	private void updateFileCache(boolean clearSelection) {
		updateFileCache(clearSelection, null);
	}

	/**
	 * Update the file cache with data from the server.
	 *
	 * @param newFilename the new name of the previously selected file,
	 * 			if a rename operation has taken place
	 */
	private void updateFileCache(boolean clearSelection, String newFilename) {
		if (clearSelection)
			clearSelectedRows();
		startIndex = 0;
		final TreeItem folderItem = GSS.get().getFolders().getCurrent();
		// Validation.
		if (folderItem == null || GSS.get().getFolders().isOthersShared(folderItem)) {
			setFiles(new ArrayList<FileResource>());
			update(true);
			return;
		}
		if (folderItem instanceof DnDTreeItem) {
			DnDTreeItem dnd = (DnDTreeItem) folderItem;
			if (dnd.getFolderResource() != null) {
				if (GSS.get().getFolders().isTrashItem(dnd))
					setFiles(new ArrayList<FileResource>());
				else
					setFiles(dnd.getFolderResource().getFiles());

			} else if (dnd.getTrashResource() != null)
				setFiles(dnd.getTrashResource().getFiles());
			else if (dnd.getSharedResource() != null)
				setFiles(dnd.getSharedResource().getFiles());
			else if (dnd.getOtherUserResource() != null)
				setFiles(dnd.getOtherUserResource().getFiles());
			else
				setFiles(dnd.getFolderResource().getFiles());
			update(true);

		}
	}

	/**
	 * Fill the file cache with data.
	 */
	public void setFiles(final List<FileResource> _files) {
		if (_files.size() > 0 && !GSS.get().getFolders().isTrash(GSS.get().getFolders().getCurrent())) {
			files = new ArrayList<FileResource>();
			for (FileResource fres : _files)
				if (!fres.isDeleted())
					files.add(fres);
		} else
			files = _files;
		Collections.sort(files, new Comparator<FileResource>() {

			@Override
			public int compare(FileResource arg0, FileResource arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}

		});
		folderFileCount = files.size();
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
		Iterator<FileResource> it = selectionModel.getSelectedSet().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),false);
		}
		
		/*for (int r : selectedRows) {
			int row = r - startIndex;
			styleRow(row, false);
			makeRowNotDraggable(row+1);
		}
		selectedRows.clear();
		Object sel = GSS.get().getCurrentSelection();
		if (sel instanceof FileResource || sel instanceof List)
			GSS.get().setCurrentSelection(null);
		if(menuShowing != null && menuShowing.isShowing()){
			menuShowing.hide();
			menuShowing=null;
		}*/
	}

	/**
	 *
	 */
	public void selectAllRows() {
		/*clearSelectedRows();
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
			makeRowDraggable(i);
		}
		GSS.get().setCurrentSelection(getSelectedFiles());
		contextMenu.setFiles(getSelectedFiles());*/


	}

	private void makeRowDraggable(int row){
	/*	int contextRow = getWidgetRow(contextMenu, table);
		if (contextRow != -1)
			table.setWidget(contextRow, 0, getFileIcon(files.get(contextRow - 1)).createImage());
		contextMenu.setWidget(new HTML(getFileIcon(files.get(row - 1)).getHTML()));
		table.setWidget(row, 0, contextMenu);
		//for(int i=1;i<table.getCellCount(row);i++)
			//GSS.get().getDragController().makeDraggable(table.getWidget(row, i));
		table.setWidget(row, 1, new DnDSimpleFocusPanel(table.getWidget(row, 1)));
		((DnDSimpleFocusPanel)table.getWidget(row, 1)).setFiles(getSelectedFiles());
		GSS.get().getDragController().makeDraggable(table.getWidget(row, 1));*/
	}
	private void makeRowNotDraggable(int row){
		/*if(table.getWidget(row, 1) instanceof DnDSimpleFocusPanel){
			((DnDSimpleFocusPanel)table.getWidget(row, 1)).setFiles(null);
			GSS.get().getDragController().makeNotDraggable(table.getWidget(row, 1));
			table.setWidget(row, 1, new DnDSimpleFocusPanel(((DnDSimpleFocusPanel)table.getWidget(row, 1)).getWidget()));

		}
		*/
	}

	private int getWidgetRow(Widget widget, Grid grid) {
		/*for (int row = 0; row < grid.getRowCount(); row++)
			for (int col = 0; col < grid.getCellCount(row); col++) {
				Widget w = table.getWidget(row, col);
				if (w == widget)
					return row;
			}*/
		return -1;
	}
	
	
	private void sortFiles(final String sortingProperty, final boolean sortingType){
		Collections.sort(files, new Comparator<FileResource>() {

            @Override
            public int compare(FileResource arg0, FileResource arg1) {
                    AbstractImagePrototype descPrototype = AbstractImagePrototype.create(images.desc());
                    AbstractImagePrototype ascPrototype = AbstractImagePrototype.create(images.asc());
                    if (sortingType){
                            if (sortingProperty.equals("version")) {
                                    return arg0.getVersion().compareTo(arg1.getVersion());
                            } else if (sortingProperty.equals("owner")) {
                                    return arg0.getOwner().compareTo(arg1.getOwner());
                            } else if (sortingProperty.equals("date")) {
                                    return arg0.getModificationDate().compareTo(arg1.getModificationDate());
                            } else if (sortingProperty.equals("size")) {
                                    return arg0.getContentLength().compareTo(arg1.getContentLength());
                            } else if (sortingProperty.equals("name")) {
                                    return arg0.getName().compareTo(arg1.getName());
                            } else if (sortingProperty.equals("path")) {
                                    return arg0.getUri().compareTo(arg1.getUri());
                            } else {
                                    return arg0.getName().compareTo(arg1.getName());
                            }
                    }
                    else if (sortingProperty.equals("version")) {
                            
                            return arg1.getVersion().compareTo(arg0.getVersion());
                    } else if (sortingProperty.equals("owner")) {
                            
                            return arg1.getOwner().compareTo(arg0.getOwner());
                    } else if (sortingProperty.equals("date")) {
                            
                            return arg1.getModificationDate().compareTo(arg0.getModificationDate());
                    } else if (sortingProperty.equals("size")) {
                            
                            return arg1.getContentLength().compareTo(arg0.getContentLength());
                    } else if (sortingProperty.equals("name")) {
                            
                            return arg1.getName().compareTo(arg0.getName());
                    } else if (sortingProperty.equals("path")) {
                            
                            return arg1.getUri().compareTo(arg0.getUri());
                    } else {
                            
                            return arg1.getName().compareTo(arg0.getName());
                    }
            }

		});
	}
	
	final class FileValueUpdater implements ValueUpdater<String>{
		private String property;
		private SortableHeader header;
		/**
		 * 
		 */
		public FileValueUpdater(SortableHeader header,String property) {
			this.property=property;
			this.header=header;
		}
		@Override
		public void update(String value) {
			header.setSorted(true);
			header.toggleReverseSort();

	        for (SortableHeader otherHeader : allHeaders) {
	          if (otherHeader != header) {
	            otherHeader.setSorted(false);
	            otherHeader.setReverseSort(true);
	          }
	        }
	        celltable.redrawHeaders();
	        sortFiles(property, header.getReverseSort());
	        FileList.this.update(true);			
		}
		
	}
}
