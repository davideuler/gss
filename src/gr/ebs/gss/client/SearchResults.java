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


import static com.google.gwt.query.client.GQuery.$;
import gr.ebs.gss.client.FileList.FileValueUpdater;
import gr.ebs.gss.client.FileList.Images;
import gr.ebs.gss.client.FileList.TableResources;
import gr.ebs.gss.client.FileList.TableStyle;
import gr.ebs.gss.client.FileList.Templates;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.RestCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.RestResourceWrapper;
import gr.ebs.gss.client.rest.resource.SearchResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UserResource;
import gr.ebs.gss.client.rest.resource.UserSearchResource;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.StopDragException;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
import gwtquery.plugins.draggable.client.events.DragContext;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStopEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.draggable.client.events.DragStopEvent.DragStopEventHandler;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTable;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.hibernate.mapping.Array;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.GssSimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
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
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * A composite that displays a list of search results for a particular query on
 * files.
 */
public class SearchResults extends Composite{
	private HTML searchResults = new HTML("Results for search:");
	private String lastQuery;
	SearchDataProvider provider = new SearchDataProvider();
	/**
	 * Specifies that the images available for this composite will be the ones
	 * available in FileContextMenu.
	 */
	public interface Images extends ClientBundle,FileContextMenu.Images, CellTreeView.Images, FileList.Images {

		@Source("gr/ebs/gss/resources/blank.gif")
		ImageResource blank();

		@Source("gr/ebs/gss/resources/asc.png")
		ImageResource asc();

		@Source("gr/ebs/gss/resources/desc.png")
		ImageResource desc();
	}

	
	interface TableResources extends DragAndDropCellTable.Resources {
	    @Source({CellTable.Style.DEFAULT_CSS, "GssCellTable.css"})
	    TableStyle cellTableStyle();
	  }
	
	static interface Templates extends SafeHtmlTemplates {
	    Templates INSTANCE = GWT.create(Templates.class);

	    @Template("<div id='dragHelper' style='border:1px solid black; background-color:#ffffff; color:black; width:150px;z-index:100'></div>")
	    SafeHtml outerHelper();
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

	
	
	private final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");

	
	
	DragStopEventHandler dragStop = new DragStopEventHandler() {
		
		@Override
		public void onDragStop(DragStopEvent event) {
			GWT.log("DRAG STOPPED");
			
		}
	};
	
	private static class ContactCell extends AbstractCell<gr.ebs.gss.client.rest.resource.FileResource> {

	    /**
	     * The html of the image used for contacts.
	     * 
	     */
	    private final String imageHtml;

	    public ContactCell(ImageResource image) {
	      this.imageHtml = AbstractImagePrototype.create(image).getHTML();
	    }

	    

		

	    @Override
	    public void render(Context context, FileResource value, SafeHtmlBuilder sb) {
	      // Value can be null, so do a null check..
	      if (value == null) {
	        return;
	      }

	      sb.appendHtmlConstant("<table>");

	      // Add the contact image.
	      sb.appendHtmlConstant("<tr><td rowspan='3'>");
	      sb.appendHtmlConstant(imageHtml);
	      sb.appendHtmlConstant("</td>");

	      // Add the name and address.
	      DisplayHelper.log("value.getName()");
	      sb.appendHtmlConstant("<td style='font-size:95%;' id='"+value.getName()+"'>");
	      sb.appendEscaped(value.getName());
	      sb.appendHtmlConstant("</td></tr><tr><td>");
	      sb.appendEscaped(value.getFileSizeAsString());
	      sb.appendHtmlConstant("</td></tr></table>");
	    }


	  }
	/**
	 * Retrieve the celltable.
	 *
	 * @return the celltable
	 */
	public DragAndDropCellTable<FileResource> getCelltable() {
		return celltable;
	}
	

	
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
	private DragAndDropCellTable<FileResource> celltable;
	private final MultiSelectionModel<FileResource> selectionModel;
	private final List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();
	SortableHeader nameHeader;
	GssSimplePager pager;
	GssSimplePager pagerTop;
	/**
	 * Construct the file list widget. This entails setting up the widget
	 * layout, fetching the number of files in the current folder from the
	 * server and filling the local file cache of displayed files with data from
	 * the server, as well.
	 *
	 * @param _images
	 */
	public SearchResults(Images _images) {
		images = _images;
		DragAndDropCellTable.Resources resources = GWT.create(TableResources.class);
		ProvidesKey<FileResource> keyProvider = new ProvidesKey<FileResource>(){

			@Override
			public Object getKey(FileResource item) {
				return item.getUri();
			}
			
		};
		
		celltable = new DragAndDropCellTable<FileResource>(GSS.VISIBLE_FILE_COUNT,resources,keyProvider){
			@Override
			protected void onBrowserEvent2(Event event) {
				/*if (DOM.eventGetType((Event) event) == Event.ONMOUSEDOWN && DOM.eventGetButton((Event) event) == NativeEvent.BUTTON_RIGHT){
					fireClickEvent((Element) event.getEventTarget().cast());					
				}*/
				super.onBrowserEvent2(event);
			}
		};
		provider.addDataDisplay(celltable);
		celltable.addDragStopHandler(dragStop);
		celltable.addDragStartHandler(new DragStartEventHandler() {

		      public void onDragStart(DragStartEvent event) {
		        FileResource value = event.getDraggableData();
		        
		        com.google.gwt.dom.client.Element helper = event.getHelper();
		        SafeHtmlBuilder sb = new SafeHtmlBuilder();
		        sb.appendHtmlConstant("<b>");
		        DisplayHelper.log(value.getName());
		        if(getSelectedFiles().size()==1)
		        	sb.appendEscaped(value.getName());
		        else
		        	sb.appendEscaped(getSelectedFiles().size()+" files");
		        sb.appendHtmlConstant("</b>");
		        helper.setInnerHTML(sb.toSafeHtml().asString());

		      }
		    });
		DragAndDropColumn<FileResource, ImageResource> status = new DragAndDropColumn<FileResource, ImageResource>(new ImageResourceCell(){
			@Override
	          public boolean handlesSelection() {
	        	    return false;
	        	  }
		}) {
	          @Override
	          public ImageResource getValue(FileResource entity) {
	            return getFileIcon(entity);
	          }
	          
	       };
	    celltable.addColumn(status,"");
	    
	    initDragOperation(status);
		final DragAndDropColumn<FileResource,SafeHtml> nameColumn = new DragAndDropColumn<FileResource,SafeHtml>(new SafeHtmlCell()) {


			@Override
			public SafeHtml getValue(FileResource object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
				if (object.getContentType().endsWith("png") || object.getContentType().endsWith("gif") || object.getContentType().endsWith("jpeg") ){					
					sb.appendHtmlConstant("<span id='fileList."+ object.getName() +"'>");
					sb.appendEscaped(object.getName());
					sb.appendHtmlConstant("</span>");
					sb.appendHtmlConstant(" <a href='" +
                                GSS.get().getTopPanel().getFileMenu().getDownloadURL(object) +
                                "' title='" + object.getOwner() + " : " + object.getPath() + object.getName() +
                                "' rel='lytebox[mnf]' " +
                                "onclick='myLytebox.start(this, false, false); return false;'>" +
                                "(view)" + "</a>");
					
					
				}
				else{					
					sb.appendHtmlConstant("<span id='fileList."+ object.getName() +"'>");
					sb.appendEscaped(object.getName());
					sb.appendHtmlConstant("</span>");
				}
				
				return sb.toSafeHtml();
			}
			
		};
		initDragOperation(nameColumn);
		celltable.addColumn(nameColumn,nameHeader = new SortableHeader("Name"));
		allHeaders.add(nameHeader);
		//nameHeader.setSorted(true);
		//nameHeader.toggleReverseSort();
		nameHeader.setUpdater(new FileValueUpdater(nameHeader, "name"));
		celltable.redrawHeaders();
		
		
	    
	    
	    SortableHeader aheader;
	    DragAndDropColumn<FileResource,String> aColumn;
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				return GSS.get().findUserFullName(object.getOwner());
			}			
		},aheader = new SortableHeader("Owner"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "owner"));
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				if(object.isDeleted())
					return object.getPath()+" (In Trash)";
				return object.getPath();
			}			
		},aheader = new SortableHeader("Path"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		
		aheader.setUpdater(new FileValueUpdater(aheader, "path"));	
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				return object.getVersion().toString();
			}			
		},aheader = new SortableHeader("Version"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "version"));
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getFileSizeAsString();
			}			
		},aheader = new SortableHeader("Size"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "size"));	
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				return formatter.format(object.getModificationDate());
			}			
		},aheader = new SortableHeader("Last Modified"));
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "date"));
	       

		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");
		celltable.setWidth("100%");
		vp.add(searchResults);
		searchResults.addStyleName("gss-searchLabel");
		pagerTop = new GssSimplePager(GssSimplePager.TextLocation.CENTER);
		pagerTop.setDisplay(celltable);
		vp.add(pagerTop);
		vp.add(celltable);
		pager = new GssSimplePager(GssSimplePager.TextLocation.CENTER);
		pager.setDisplay(celltable);
		//celltable.setPageSize(2);
		
		vp.add(pager);
		vp.setCellWidth(celltable, "100%");
		
		initWidget(vp);
		
		//initWidget(celltable);
		celltable.setStyleName("gss-List");
		selectionModel = new MultiSelectionModel<FileResource>();
		

		 Handler selectionHandler = new SelectionChangeEvent.Handler() { 
             @Override 
             public void onSelectionChange(com.google.gwt.view.client.SelectionChangeEvent event) {
            	 if(getSelectedFiles().size()==1)
            		 GSS.get().setCurrentSelection(getSelectedFiles().get(0));
            	 else
            		 GSS.get().setCurrentSelection(getSelectedFiles());
 				//contextMenu.setFiles(getSelectedFiles());
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
	
	 private void initDragOperation(DragAndDropColumn<?, ?> column) {

		    // retrieve draggableOptions on the column
		    DraggableOptions draggableOptions = column.getDraggableOptions();
		    // use template to construct the helper. The content of the div will be set
		    // after
		    draggableOptions.setHelper($(Templates.INSTANCE.outerHelper().asString()));
		    //draggableOptions.setZIndex(100);
		    // opacity of the helper
		    draggableOptions.setAppendTo("body"); 
		    //draggableOptions.setOpacity((float) 0.8);
		    draggableOptions.setContainment("document");
		    // cursor to use during the drag operation
		    draggableOptions.setCursor(Cursor.MOVE);
		    // set the revert option
		    draggableOptions.setRevert(RevertOption.ON_INVALID_DROP);
		    // prevents dragging when user click on the category drop-down list
		    draggableOptions.setCancel("select");
		    
		    draggableOptions.setOnBeforeDragStart(new DragFunction() {
				
				@Override
				public void f(DragContext context) {
					 FileResource value = context.getDraggableData();
				     if(!selectionModel.isSelected(value)){
				       	throw new StopDragException();
				      }
					
				}
			});
		  }
	
	 public void showContextMenu(Event event){
		 menuShowing = new FileContextMenu(images, false, true);
			menuShowing=menuShowing.onEmptyEvent(event);
	 }
	@Override
	public void onBrowserEvent(Event event) {
		
		if (files == null || files.size() == 0) {
			if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
				menuShowing = new FileContextMenu(images, false, true);
				menuShowing=menuShowing.onEmptyEvent(event);
				event.preventDefault();
				event.cancelBubble(true);
			}
			return;
		}
		if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() != 0) {
			GWT.log("*****GOING TO SHOW CONTEXT MENU ****", null);
			menuShowing =  new FileContextMenu(images, false, false);
			menuShowing=menuShowing.onEvent(event);
			event.cancelBubble(true);
			event.preventDefault();
		} else if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
			menuShowing = new FileContextMenu(images, false, true);
			menuShowing=menuShowing.onEmptyEvent(event);
			event.cancelBubble(true);
			event.preventDefault();
		} else if (DOM.eventGetType(event) == Event.ONDBLCLICK)
			if (getSelectedFiles().size() == 1) {
				GSS app = GSS.get();
				FileResource file = getSelectedFiles().get(0);
				String dateString = RestCommand.getDate();
				String resource = file.getUri().substring(app.getApiPath().length() - 1, file.getUri().length());
				String sig = app.getCurrentUserResource().getUsername() + " " +
						RestCommand.calculateSig("GET", dateString, resource,
						RestCommand.base64decode(app.getToken()));
				if(!file.isDeleted()){
					Window.open(file.getUri() + "?Authorization=" + URL.encodeComponent(sig) + "&Date=" + URL.encodeComponent(dateString), "_blank", "");
				}
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
		updateFileCache(null);
		return DONE;
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
		
		copyListAndContinue(files);
		for(FileResource f : files){
			folderTotalSize += f.getContentLength();
		}
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
		boolean shared = file.isShared();
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

	public void updateFileCache(String query) {
		final GSS app = GSS.get();
		setLastQuery(query);
		clearSelectedRows();
		//clearLabels();
		startIndex = 0;
		app.showLoadingIndicator();
		if (query == null || query.trim().equals("")) {
			searchResults.setHTML("You must specify a query.");
			setFiles(new ArrayList());
			update(true);
			app.hideLoadingIndicator();
		} else if (!GSS.isValidResourceName(query)) {
			searchResults.setHTML("The query was invalid. Try to use words that appear in the file's name, contents or tags.");
			setFiles(new ArrayList());
			update(true);
			app.hideLoadingIndicator();
		} else{
			searchResults.setHTML("Search results for " + query);
			showCellTable(true);
			
		}
	}

	/**
	 * Fill the file cache with data.
	 */
	public void setFiles(final List<FileResource> _files) {
		if (_files.size() > 0 && ! (GSS.get().getTreeView().getSelection() instanceof TrashResource)) {
			files = new ArrayList<FileResource>();
			for (FileResource fres : _files)
				files.add(fres);
		}
		else
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
	}

	/**
	 *
	 */
	public void selectAllRows() {
		Iterator<FileResource> it = selectionModel.getSelectedSet().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),true);
		}


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
	        //sortFiles(property, header.getReverseSort());
	        provider.setSortProperty(property);
	        provider.setReverseSort(header.getReverseSort());
	        provider.onRangeChanged(celltable);
	        SearchResults.this.update(true);			
		}
		
	}
	/**
	 * Creates a new ArrayList<FileResources> from the given files ArrayList 
	 * in order that the input files remain untouched 
	 * and continues to find user's full names of each FileResource element
	 * in the new ArrayList
	 *    
	 * @param filesInput
	 */
	private void copyListAndContinue(List<FileResource> filesInput){
		List<FileResource> copiedFiles = new ArrayList<FileResource>();		
		for(FileResource file : filesInput) {
			copiedFiles.add(file);
		}
		handleFullNames(copiedFiles);
	}
	
	/**
	 * Examines whether or not the user's full name exists in the 
	 * userFullNameMap in the GSS.java for every element of the input list.
	 * If the user's full name does not exist in the map then a command is being made.  
	 * 
	 * @param filesInput
	 */
	private void handleFullNames(List<FileResource> filesInput){		
		if(filesInput.size() == 0){
			showCellTable(false);
			return;
		}		

		if(GSS.get().findUserFullName(filesInput.get(0).getOwner()) == null){
			findFullNameAndUpdate(filesInput);		
			return;
		}
				
		if(filesInput.size() >= 1){
			filesInput.remove(filesInput.get(0));
			if(filesInput.isEmpty()){
				showCellTable(false);				
			}else{
				handleFullNames(filesInput);
			}
		}		
	}
	
	/**
	 * Makes a command to search for full name from a given username. 
	 * Only after the completion of the command the celltable is shown
	 * or the search for the next full name continues.
	 *  
	 * @param filesInput
	 */
	private void findFullNameAndUpdate(final List<FileResource> filesInput){		
		String aUserName = filesInput.get(0).getOwner();
		String path = GSS.get().getApiPath() + "users/" + aUserName; 

		GetCommand<UserSearchResource> gg = new GetCommand<UserSearchResource>(UserSearchResource.class, path, false,null) {
			@Override
			public void onComplete() {
				final UserSearchResource result = getResult();
				for (UserResource user : result.getUsers()){
					String username = user.getUsername();
					String userFullName = user.getName();
					GSS.get().putUserToMap(username, userFullName);
					if(filesInput.size() >= 1){
						filesInput.remove(filesInput.get(0));
						if(filesInput.isEmpty()){
							showCellTable(false);
						}else{
							handleFullNames(filesInput);
						}												
					}									
				}
			}
			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch user's full name from the given username " + filesInput.get(0).getOwner());
				if(filesInput.size() >= 1){
					filesInput.remove(filesInput.get(0));
					handleFullNames(filesInput);					
				}
			}
		};
		DeferredCommand.addCommand(gg);
	
	}
	/**
	 * Shows the files in the cellTable 
	 */

	private void showCellTable(boolean update){
		if(celltable.getRowCount()>GSS.VISIBLE_FILE_COUNT){
			pager.setVisible(true);
			pagerTop.setVisible(true);
		}
		else{
			pager.setVisible(false);
			pagerTop.setVisible(false);
		}
		if(update)
			provider.onRangeChanged(celltable);
		celltable.redrawHeaders();		
	}
	
	
	/**
	 * Retrieve the lastQuery.
	 *
	 * @return the lastQuery
	 */
	public String getLastQuery() {
		return lastQuery;
	}
	
	
	/**
	 * Modify the lastQuery.
	 *
	 * @param lastQuery the lastQuery to set
	 */
	public void setLastQuery(String lastQuery) {
		this.lastQuery = lastQuery;
	}
	
	class SearchDataProvider extends AsyncDataProvider<FileResource>{
		String sortProperty;
		boolean reverseSort=false;
		
		
		/**
		 * Retrieve the sortProperty.
		 *
		 * @return the sortProperty
		 */
		public String getSortProperty() {
			return sortProperty;
		}
		
		
		/**
		 * Modify the sortProperty.
		 *
		 * @param sortProperty the sortProperty to set
		 */
		public void setSortProperty(String sortProperty) {
			this.sortProperty = sortProperty;
		}
		
		
		/**
		 * Retrieve the reverseSort.
		 *
		 * @return the reverseSort
		 */
		public boolean isReverseSort() {
			return reverseSort;
		}
		
		
		/**
		 * Modify the reverseSort.
		 *
		 * @param reverseSort the reverseSort to set
		 */
		public void setReverseSort(boolean reverseSort) {
			this.reverseSort = reverseSort;
		}
		
		@Override
		protected void onRangeChanged(final HasData<FileResource> display) {
			final int start = display.getVisibleRange().getStart();
			final GSS app = GSS.get();
			if(getLastQuery()==null||getLastQuery().equals("")){
				display.setRowCount(0,true);
				return;
				
			}
			String parameters = "?start="+start;
			if(sortProperty!=null){
				parameters=parameters+"&sort="+sortProperty;
				if(reverseSort)
					parameters = parameters+" asc";
				else
					parameters = parameters+" desc";
				
			}
			
			GetCommand<SearchResource> eg = new GetCommand<SearchResource>(SearchResource.class,
						app.getApiPath() + "search/" +URL.encodeComponent(getLastQuery())+parameters, null) {

				@Override
				public void onComplete() {
					SearchResource s = getResult();
					display.setRowCount(s.getSize(),true);
					display.setRowData(start, s.getFiles());
					setFiles(s.getFiles());
					update(true);
					
				}

				@Override
				public void onError(Throwable t) {
					if(t instanceof RestException)
						app.displayError("Unable to perform search:"+((RestException)t).getHttpStatusText());
					else
						app.displayError("System error performing search:"+t.getMessage());
					GWT.log("",t);
					updateFileCache("");
				}

			};
			DeferredCommand.addCommand(eg);
			
		}
		
	}

}
