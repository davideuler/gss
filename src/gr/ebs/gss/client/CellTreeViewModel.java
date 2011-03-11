/*
 * Copyright 2011 Electronic Business Systems Ltd.
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
import gr.ebs.gss.client.CellTreeView.Images;
import gr.ebs.gss.client.CellTreeView.RefreshHandler;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.MyFolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersFolderResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.RestResourceWrapper;
import gr.ebs.gss.client.rest.resource.SharedFolderResource;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashFolderResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gwtquery.plugins.draggable.client.DragAndDropManager;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.StopDragException;
import gwtquery.plugins.draggable.client.DraggableOptions.CursorAt;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
import gwtquery.plugins.draggable.client.events.DragContext;
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropNodeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.query.client.plugins.GQueryUi;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;



/**
 * @author kman
 *
 */
public class CellTreeViewModel implements TreeViewModel{
	
	private final ListDataProvider<RestResource> rootNodes = new ListDataProvider<RestResource>();
	private Map<String,FolderResource> folderCache=new HashMap<String, FolderResource>();
	final Images images;
	SingleSelectionModel<RestResource> selectionModel;
	Map<String, MyFolderDataProvider> mymap = new HashMap<String, MyFolderDataProvider>();
	Map<String, MyFolderDataProvider> sharedmap = new HashMap<String, MyFolderDataProvider>();
	Map<String, MyFolderDataProvider> othersmap = new HashMap<String, MyFolderDataProvider>();
	static interface Templates extends SafeHtmlTemplates {
	    Templates INSTANCE = GWT.create(Templates.class);

	    @Template(" <div id='dragHelper' class='{0}'></div>")
	    SafeHtml outerHelper(String cssClassName);
	  }

	void configureDragOperation(final DraggableOptions options) {

	    // set a custom element as drag helper. The content of the helper will be
	    // set when the drag will start
	    options.setHelper($(Templates.INSTANCE.outerHelper(
	        "drag").asString()));
	    // opacity of the drag helper
	    options.setOpacity((float) 0.9);
	    // cursor during the drag operation
	    options.setCursor(Cursor.MOVE);
	    // the cell being greater than the helper, force the position of the
	    // helper on the mouse cursor.
	    options.setCursorAt(new CursorAt(10, 10, null, null));
	    // append the helper to the body element
	    options.setAppendTo("body");
	    options.setCancel("select");
	    // set the revert option
	    options.setRevert(RevertOption.ON_INVALID_DROP);
	    
	    options.setOnBeforeDragStart(new DragFunction() {
			
			@Override
			public void f(DragContext context) {
				 RestResource value = context.getDraggableData();
			     if(!CellTreeViewModel.this.selectionModel.isSelected(value)){
			       	throw new StopDragException();
			      }
			     if(value instanceof TrashResource || value instanceof SharedResource || value instanceof OthersResource || value instanceof OtherUserResource){
			       	throw new StopDragException();
			      }
				
			}
		});
	    // use a Function to fill the content of the helper
	    // we could also add a DragStartEventHandler on the DragAndDropTreeCell and
	    // DragAndDropCellList.
	    
	    options.setOnDragStart(new DragFunction() {
	      public void f(DragContext context) {
	        RestResourceWrapper memberInfo = context.getDraggableData();
	        context.getHelper().setInnerHTML(memberInfo.getName());
	      }
	    });

	  }

	/**
	 * 
	 */
	public CellTreeViewModel(final Images _images,SingleSelectionModel<RestResource> selectionModel ) {
		super();
		images=_images;
		this.selectionModel=selectionModel;
	}
	
	private final Cell<RestResource> departmentCell = new AbstractCell<RestResource>("contextmenu"){
		
		@Override
		public void render(com.google.gwt.cell.client.Cell.Context arg0, RestResource arg1, SafeHtmlBuilder arg2) {
			String id = null;
			String html = null;
			String name = null;
			if(arg1 instanceof TrashFolderResource){
				html = AbstractImagePrototype.create(images.folderYellow()).getHTML();
				FolderResource res = ((RestResourceWrapper)arg1).getResource();
				name = res.getName();
				id = res.getParentName() +"."+name;
			}
			else if(arg1 instanceof RestResourceWrapper){
				FolderResource res = ((RestResourceWrapper)arg1).getResource();
				if(res.isShared())
					html = AbstractImagePrototype.create(images.sharedFolder()).getHTML();
				else if(res.getParentName()==null){
					html = AbstractImagePrototype.create(images.home()).getHTML();
				}
				else
					html = AbstractImagePrototype.create(images.folderYellow()).getHTML();
				name = res.getName();
				if(res.getParentName() != null){					
					id = res.getParentName()+"."+name;
				}else{					
					id = name;
				}
				
			}
			else if(arg1 instanceof TrashResource){
				html = AbstractImagePrototype.create(images.trash()).getHTML();
				name="Trash";
				id = name;				
			}
			
			else if(arg1 instanceof SharedResource){
				html = AbstractImagePrototype.create(images.myShared()).getHTML();
				name = "My Shared";
				id = name;
			}
			else if(arg1 instanceof OthersResource){
				html = AbstractImagePrototype.create(images.othersShared()).getHTML();
				name = "Other's Shared";
				id = "others";				
			}
			else if(arg1 instanceof OtherUserResource){
				html = AbstractImagePrototype.create(images.permUser()).getHTML();
				name = ((OtherUserResource)arg1).getName();
				id = name;
			}
			arg2.appendHtmlConstant(html);
			arg2.appendHtmlConstant("<span id='"+id +"'"+ " class='papala'>");
			arg2.appendEscaped(name);
			arg2.appendHtmlConstant("</span>");
		}
		
		public void onBrowserEvent(Cell.Context context, com.google.gwt.dom.client.Element parent, RestResource value, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<RestResource> valueUpdater) {
			if(event.getType().equals("contextmenu")){
				selectionModel.setSelected(value, true);
				GSS.get().setCurrentSelection(value);
				GSS.get().getTreeView().showPopup(event.getClientX(), event.getClientY());
			}
		};
		
	};
	
	
	@Override
	public <T> NodeInfo<?> getNodeInfo(final T value) {
		
		if(value==null){
			DragAndDropNodeInfo n = new DragAndDropNodeInfo<RestResource>(getRootNodes(), departmentCell,
			            selectionModel, null);
			configureFolderDrop(n);
	        configureDragOperation(n.getDraggableOptions());
			return n;
		}
		else if (value instanceof MyFolderResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((MyFolderResource) value),MyFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        mymap.put(((MyFolderResource) value).getUri(), dataProvider);
	        
	        // permission cell are not draggable
	        //n.setCellDroppableOnly();
	        configureFolderDrop(n);
	        configureDragOperation(n.getDraggableOptions());
	        
	        return n;
		}
		else if (value instanceof SharedResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((SharedResource) value), SharedFolderResource.class);
			sharedmap.put(((SharedResource) value).getUri(), dataProvider);
			DragAndDropNodeInfo<RestResource> n = new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
			 configureFolderDrop(n);
		        configureDragOperation(n.getDraggableOptions());
			return n;
		}
		else if (value instanceof TrashResource) {
	        // Second level.
			ListDataProvider<RestResource> trashProvider = new ListDataProvider<RestResource>();
			List<RestResource> r = new ArrayList<RestResource>();
			for(FolderResource f : GSS.get().getTreeView().getTrash().getFolders()){
				r.add(new TrashFolderResource(f));
			}
			trashProvider.setList(r);
			DragAndDropNodeInfo<RestResource> n = new DragAndDropNodeInfo<RestResource>(trashProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
			configureFolderDrop(n);
	        configureDragOperation(n.getDraggableOptions());
			return n;
		}
		else if (value instanceof OthersResource) {
	        // Second level.
			OthersDataProvider dataProvider = new OthersDataProvider(
	            ((OthersResource) value), SharedFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n = new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, null);
	        configureFolderDrop(n);
	        configureDragOperation(n.getDraggableOptions());
	        return n;
		}
		else if (value instanceof SharedFolderResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((SharedFolderResource) value),SharedFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        sharedmap.put(((SharedFolderResource) value).getUri(), dataProvider);
	        configureFolderDrop(n);
	        configureDragOperation(n.getDraggableOptions());
	        return n;
		}
		else if (value instanceof OthersFolderResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((OthersFolderResource) value),OthersFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        //nodeInfos.put(((OthersFolderResource) value).getUri(), n);
	        othersmap.put(((OthersFolderResource) value).getUri(), dataProvider);
	        configureFolderDrop(n);
	        configureDragOperation(n.getDraggableOptions());
	        return n;
		}
		else if (value instanceof OtherUserResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((OtherUserResource) value),OthersFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        configureFolderDrop(n);
	        configureDragOperation(n.getDraggableOptions());
	        return n;
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	private void configureFolderDrop(DragAndDropNodeInfo<RestResource> n){
		DroppableOptions options = n.getDroppableOptions();
        options.setDroppableHoverClass("droppableHover");
        // use a DroppableFunction here. We can also add a DropHandler in the tree
        // itself
        
        options.setOnOver(new DroppableFunction() {
			
			@Override
			public void f(final DragAndDropContext context) {
				if(context.getDroppableData()!=null && context.getDroppableData() instanceof RestResource){

					GSS.get().getTreeView().getUtils().openNodeContainingResource((RestResource) context.getDroppableData(), new RefreshHandler() {
						
						@Override
						public void onRefresh() {
							
							DragAndDropManager.getInstance().update(context);//initialize(context, GQueryUi.Event.create(com.google.gwt.user.client.Event.getCurrentEvent()));
							
						}
					});
					
				}
				
				
				
			}
		});
        options.setOnDrop(new DroppableFunction() {

          public void f(DragAndDropContext context) {
        	  
        	  DnDFolderPopupMenu popup ;
        	  if(context.getDraggableData() instanceof FileResource){
        		  if(context.getDroppableData() instanceof RestResourceWrapper)
        			  popup = new DnDFolderPopupMenu(images, ((RestResourceWrapper) context.getDroppableData()).getResource(), Arrays.asList(context.getDraggableData()));
        		  else
        			  popup = new DnDFolderPopupMenu(images, null, Arrays.asList(context.getDraggableData()));
        	  }
        	  
        	  else{
        		  if(context.getDroppableData() instanceof RestResourceWrapper)
        			  popup = new DnDFolderPopupMenu(images, ((RestResourceWrapper) context.getDroppableData()).getResource(), context.getDraggableData());
        		  else
        			  popup = new DnDFolderPopupMenu(images, null, context.getDraggableData());
        	  }
        	  
        	  int left = context.getDroppable().getAbsoluteLeft() + 40;
              int top = context.getDroppable().getAbsoluteTop() + 20;
              popup.setPopupPosition(left, top);
        	 
        	  popup.show();
          }
        });
	}

	@Override
	public boolean isLeaf(Object value) {
		if(value instanceof RestResourceWrapper)
			return ((RestResourceWrapper)value).getResource().getFolders().size()==0;
		else if(value instanceof TrashResource)
			return ((TrashResource)value).getFolders().size()==0;
		else if(value instanceof SharedResource)
			return ((SharedResource)value).getFolders().size()==0;
		else if(value instanceof OthersResource)
			return ((OthersResource)value).getOtherUsers().size()==0;
		else if(value instanceof OtherUserResource)
			return ((OtherUserResource)value).getFolders().size()==0;
		return false;
	}
	
	class ResourceValueUpdater implements  ValueUpdater<RestResource>{

		@Override
		public void update(final RestResource value) {
			if(value instanceof MyFolderResource){
				GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, value.getUri(), null) {

					@Override
					public void onComplete() {
						FolderResource rootResource = getResult();
						//((MyFolderResource)value).getResource().setFiles(rootResource.getFiles());
						((MyFolderResource)value).setResource(rootResource);
						if(GSS.get().getTreeView().getSelection().getUri().equals(value.getUri()))
							selectionModel.setSelected(value, true);
						GSS.get().onResourceUpdate(value);
						
					}
	
					@Override
					public void onError(Throwable t) {
						GWT.log("Error fetching root folder", t);
						GSS.get().displayError("Unable to fetch root folder");
					}
	
				};
				DeferredCommand.addCommand(gf);
			}
			else if(value instanceof TrashResource){
				DeferredCommand.addCommand(new GetCommand<TrashResource>(TrashResource.class, GSS.get().getCurrentUserResource().getTrashPath(), null) {
					@Override
					public void onComplete() {
						//trash = getResult();
						((TrashResource)value).setFolders(getResult().getFolders());
						((TrashResource)value).setFiles(getResult().getFiles());
						for(RestResource r : getRootNodes().getList()){
							if(r instanceof TrashResource)
								getRootNodes().getList().set(getRootNodes().getList().indexOf(r),GSS.get().getTreeView().getTrash());
						}
						GSS.get().getTreeView().updateNodeChildren(GSS.get().getTreeView().getTrash());
						//GSS.get().showFileList(true);
						GSS.get().onResourceUpdate(value);
					}

					@Override
					public void onError(Throwable t) {
						if(t instanceof RestException){
							int statusCode = ((RestException)t).getHttpStatusCode();
							// On IE status code 1223 may be returned instead of 204.
							if(statusCode == 204 || statusCode == 1223){
								((TrashResource)value).setFolders(new ArrayList<FolderResource>());
								((TrashResource)value).setFiles(new ArrayList<FileResource>());
						}
						else{
							GWT.log("", t);
							GSS.get().displayError("Unable to fetch trash folder:"+t.getMessage());
							//GSS.get().getTreeView().getTrash() = new TrashResource(GSS.get().getCurrentUserResource().getTrashPath());
						}
					}
				}
				});
			}
			else if(value instanceof OthersFolderResource){
				GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, value.getUri(), null) {

					@Override
					public void onComplete() {
						FolderResource rootResource = getResult();
						//((MyFolderResource)value).getResource().setFiles(rootResource.getFiles());
						((OthersFolderResource)value).setResource(rootResource);
						if(GSS.get().getTreeView().getSelection().getUri().equals(value.getUri()))
							selectionModel.setSelected(value, true);
						GSS.get().onResourceUpdate(value);
						
					}
	
					@Override
					public void onError(Throwable t) {
						GWT.log("Error fetching root folder", t);
						GSS.get().displayError("Unable to fetch root folder");
					}
	
				};
				DeferredCommand.addCommand(gf);
			}
			else if(value instanceof SharedFolderResource){
				GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, value.getUri(), null) {

					@Override
					public void onComplete() {
						FolderResource rootResource = getResult();
						//((MyFolderResource)value).getResource().setFiles(rootResource.getFiles());
						((SharedFolderResource)value).setResource(rootResource);
						if(GSS.get().getTreeView().getSelection().getUri().equals(value.getUri()))
							selectionModel.setSelected(value, true);
						GSS.get().onResourceUpdate(value);
						
					}
	
					@Override
					public void onError(Throwable t) {
						GWT.log("Error fetching root folder", t);
						GSS.get().displayError("Unable to fetch root folder");
					}
	
				};
				DeferredCommand.addCommand(gf);
			}
			else if(value instanceof SharedResource){
				GetCommand<SharedResource> gf = new GetCommand<SharedResource>(SharedResource.class, value.getUri(), null) {

					@Override
					public void onComplete() {
						SharedResource rootResource = getResult();
						((SharedResource)value).setFolders(getResult().getFolders());
						((SharedResource)value).setFiles(getResult().getFiles());
						
						if(GSS.get().getTreeView().getSelection().getUri().equals(value.getUri()))
							selectionModel.setSelected(value, true);
						GSS.get().onResourceUpdate(value);
						
					}
	
					@Override
					public void onError(Throwable t) {
						GWT.log("Error fetching root folder", t);
						GSS.get().displayError("Unable to fetch root folder");
					}
	
				};
				DeferredCommand.addCommand(gf);
			}
			else if(value instanceof OtherUserResource){
				GetCommand<OtherUserResource> gf = new GetCommand<OtherUserResource>(OtherUserResource.class, value.getUri(), null) {

					@Override
					public void onComplete() {
						OtherUserResource rootResource = getResult();
						((OtherUserResource)value).setFolders(getResult().getFolders());
						((OtherUserResource)value).setFiles(getResult().getFiles());
						
						if(GSS.get().getTreeView().getSelection().getUri().equals(value.getUri()))
							selectionModel.setSelected(value, true);
						GSS.get().onResourceUpdate(value);
						
					}
	
					@Override
					public void onError(Throwable t) {
						GWT.log("Error fetching root folder", t);
						GSS.get().displayError("Unable to fetch root folder");
					}
	
				};
				DeferredCommand.addCommand(gf);
			}
			
		}
		
	}
	class MyFolderDataProvider extends AsyncDataProvider<RestResource>{
		private RestResource restResource;
		private Class resourceClass;
		  public MyFolderDataProvider(RestResource department, Class resourceClass) {
		    super(new ProvidesKey<RestResource>() {

				@Override
				public Object getKey(RestResource item) {
					return item.getUri();
				}});
		    this.restResource = department;
		    this.resourceClass=resourceClass;
		  }

		  @Override
		  protected void onRangeChanged(final HasData<RestResource> view) {
			refresh(null);
		  }
		  
		/**
		 * Retrieve the restResource.
		 *
		 * @return the restResource
		 */
		public RestResource getRestResource() {
			return restResource;
		}
		
		
		/**
		 * Modify the restResource.
		 *
		 * @param restResource the restResource to set
		 */
		public void setRestResource(RestResource restResource) {
			this.restResource = restResource;
		}
		List<RestResource> res =null;
		  public void refresh(final RefreshHandler refresh){
			  FolderResource cache = null;
			  if(restResource instanceof RestResourceWrapper && !((RestResourceWrapper)restResource).getResource().isNeedsExpanding())
				  cache = ((RestResourceWrapper)restResource).getResource();
			  GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, restResource.getUri(),cache ) {

					@Override
					public void onComplete() {
						if(restResource instanceof RestResourceWrapper){
							((RestResourceWrapper)restResource).setResource(getResult());//restResource = getResult();
							((RestResourceWrapper)restResource).getResource().setNeedsExpanding(false);
						}
						if(usedCachedVersion()&&res!=null){
							
								updateRowCount(res.size(), true);
								updateRowData(0,res);
							return;
						}
						String[] folderPaths = null;
						if(resourceClass.equals(MyFolderResource.class))
							folderPaths=((MyFolderResource) restResource).getResource().getSubfolderPaths().toArray(new String[] {});
						else if(resourceClass.equals(SharedFolderResource.class) && restResource instanceof SharedResource)
							folderPaths=((SharedResource) restResource).getSubfolderPaths().toArray(new String[] {});
						else if(resourceClass.equals(SharedFolderResource.class)){
							folderPaths=((SharedFolderResource) restResource).getResource().getSubfolderPaths().toArray(new String[] {});
							GWT.log("------------>"+folderPaths);
						}
						else if(resourceClass.equals(TrashFolderResource.class))
							folderPaths=((TrashFolderResource) restResource).getResource().getSubfolderPaths().toArray(new String[] {});
						else if(resourceClass.equals(OthersFolderResource.class) && restResource instanceof OtherUserResource)
							folderPaths=((OtherUserResource) restResource).getSubfolderPaths().toArray(new String[] {});
						else if(resourceClass.equals(OthersFolderResource.class))
							folderPaths=((OthersFolderResource) restResource).getResource().getSubfolderPaths().toArray(new String[] {});
						MultipleGetCommand.Cached[] cached = null;
						if(restResource instanceof RestResourceWrapper)
							cached=((RestResourceWrapper)restResource).getResource().getCache();
						MultipleGetCommand<FolderResource> gf2 = new MultipleGetCommand<FolderResource>(FolderResource.class,
									folderPaths, cached) {

							@Override
							public void onComplete() {
								res = new ArrayList<RestResource>();
								for(FolderResource r : getResult()){
									if(r.isDeleted()){
										
									}
									else if(resourceClass.equals(MyFolderResource.class))
										res.add(new MyFolderResource(r));
									else if(resourceClass.equals(SharedFolderResource.class)){
										res.add(new SharedFolderResource(r));
									}
									else if(resourceClass.equals(TrashFolderResource.class))
										res.add(new TrashFolderResource(r));
									else if(resourceClass.equals(OthersFolderResource.class))
										res.add(new OthersFolderResource(r));
								}
								if(restResource instanceof RestResourceWrapper)
									((RestResourceWrapper)restResource).getResource().setFolders(getResult());
								updateRowCount(res.size(), true);
								updateRowData(0,res);
								if(refresh!=null)
									refresh.onRefresh();
							}

							@Override
							public void onError(Throwable t) {
								GSS.get().displayError("Unable to fetch subfolders");
								GWT.log("Unable to fetch subfolders", t);
							}

							@Override
							public void onError(String p, Throwable throwable) {
								GWT.log("Path:"+p, throwable);
							}

						};
						DeferredCommand.addCommand(gf2);
						
					}

					@Override
					public void onError(Throwable t) {
						
						GWT.log("Error fetching root folder", t);
						GSS.get().displayError("Unable to fetch root folder");
					}

				};
				DeferredCommand.addCommand(gf);
		  }		  
	}
	
	
	class OthersDataProvider extends AsyncDataProvider<RestResource>{
		private RestResource restResource;
		private Class resourceClass;
		  public OthersDataProvider(RestResource department, Class resourceClass) {
		    super(new ProvidesKey<RestResource>() {

				@Override
				public Object getKey(RestResource item) {
					return item.getUri();
				}});
		    this.restResource = department;
		    this.resourceClass=resourceClass;
		  }

		  @Override
		  protected void onRangeChanged(final HasData<RestResource> view) {
			refresh(null);
		  }
		  
		/**
		 * Retrieve the restResource.
		 *
		 * @return the restResource
		 */
		public RestResource getRestResource() {
			return restResource;
		}
		
		
		/**
		 * Modify the restResource.
		 *
		 * @param restResource the restResource to set
		 */
		public void setRestResource(RestResource restResource) {
			this.restResource = restResource;
		}
		
		  public void refresh(final RefreshHandler refresh){
			  GetCommand<OthersResource> go = new GetCommand<OthersResource>(OthersResource.class,
                          restResource.getUri(), null) {

			          @Override
			          public void onComplete() {
			        	  final OthersResource others = getResult();
                          MultipleGetCommand<OtherUserResource> gogo = new MultipleGetCommand<OtherUserResource>(OtherUserResource.class,
                                                  others.getOthers().toArray(new String[] {}), null) {

                                  @Override
                                  public void onComplete() {
                                          List<OtherUserResource> res = getResult();
                                          updateRowCount(res.size(), true);
                                          List<RestResource> r = new ArrayList<RestResource>();
                                          r.addAll(res);
          								  updateRowData(0,r);
                                  }

                                  @Override
                                  public void onError(Throwable t) {
                                          GWT.log("Error fetching Others Root folder", t);
                                          GSS.get().displayError("Unable to fetch Others Root folder");
                                  }

                                  @Override
                                  public void onError(String p, Throwable throwable) {
                                          GWT.log("Path:"+p, throwable);
                                  }
                          };
                          DeferredCommand.addCommand(gogo);
			          }
			
			          @Override
			          public void onError(Throwable t) {
			                  GWT.log("Error fetching Others Root folder", t);
			                  GSS.get().displayError("Unable to fetch Others Root folder");
			          }
			  };
			  DeferredCommand.addCommand(go);
		  }		  
	}


	
	/**
	 * Retrieve the rootNodes.
	 *
	 * @return the rootNodes
	 */
	public ListDataProvider<RestResource> getRootNodes() {
		return rootNodes;
	}

	
	/**
	 * Retrieve the mymap.
	 *
	 * @return the mymap
	 */
	public Map<String, MyFolderDataProvider> getMymap() {
		return mymap;
	}

	
	/**
	 * Retrieve the sharedmap.
	 *
	 * @return the sharedmap
	 */
	public Map<String, MyFolderDataProvider> getSharedmap() {
		return sharedmap;
	}

	
	/**
	 * Retrieve the othersmap.
	 *
	 * @return the othersmap
	 */
	public Map<String, MyFolderDataProvider> getOthersmap() {
		return othersmap;
	}
	
	
	
	
	
	
}