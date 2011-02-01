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

import gr.ebs.gss.client.CellTreeView.Images;
import gr.ebs.gss.client.CellTreeView.RefreshHandler;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.RestException;
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
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropNodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DeferredCommand;
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
	final Images images;
	SingleSelectionModel<RestResource> selectionModel;
	Map<String, MyFolderDataProvider> mymap = new HashMap<String, MyFolderDataProvider>();
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
			String html=null;
			String name=null;
			if(arg1 instanceof TrashFolderResource){
				html = AbstractImagePrototype.create(images.folderYellow()).getHTML();
				FolderResource res = ((RestResourceWrapper)arg1).getResource();
				name=res.getName();
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
				
			}
			else if(arg1 instanceof TrashResource){
				html = AbstractImagePrototype.create(images.trash()).getHTML();
				name="Trash";
			}
			
			else if(arg1 instanceof SharedResource){
				html = AbstractImagePrototype.create(images.myShared()).getHTML();
				name="My Shared";
			}
			else if(arg1 instanceof OthersResource){
				html = AbstractImagePrototype.create(images.othersShared()).getHTML();
				name = "Other's Shared";
			}
			else if(arg1 instanceof OtherUserResource){
				html = AbstractImagePrototype.create(images.permUser()).getHTML();
				name = ((OtherUserResource)arg1).getName();
			}
			arg2.appendHtmlConstant(html);
			arg2.appendHtmlConstant("<span class='papala'>");
			arg2.appendEscaped(name);
			arg2.appendHtmlConstant("</span>");
		}
		
		public void onBrowserEvent(Cell.Context context, com.google.gwt.dom.client.Element parent, RestResource value, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<RestResource> valueUpdater) {
			if(event.getType().equals("contextmenu")){
				selectionModel.setSelected(value, true);
				GSS.get().getTreeView().showPopup(event.getClientX(), event.getClientY());
			}
		};
		
	};
	
	
	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		
		if(value==null){
			return new DragAndDropNodeInfo<RestResource>(getRootNodes(), departmentCell,
			            selectionModel, null);
		}
		else if (value instanceof MyFolderResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((MyFolderResource) value),MyFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        //nodeInfos.put(((MyFolderResource) value).getUri(), n);
	        mymap.put(((MyFolderResource) value).getUri(), dataProvider);
	        DroppableOptions options = n.getDroppableOptions();
	        options.setDroppableHoverClass("droppableHover");
	        // use a DroppableFunction here. We can also add a DropHandler in the tree
	        // itself
	        options.setOnDrop(new DroppableFunction() {

	          public void f(DragAndDropContext context) {
	        	  GWT.log("DROPPED");
	            
	          }
	        });
	        // permission cell are not draggable
	        n.setCellDroppableOnly();
	        return n;
		}
		else if (value instanceof SharedResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((SharedResource) value), SharedFolderResource.class);
	        return new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
		}
		else if (value instanceof TrashResource) {
	        // Second level.
			ListDataProvider<RestResource> trashProvider = new ListDataProvider<RestResource>();
			List<RestResource> r = new ArrayList<RestResource>();
			for(FolderResource f : GSS.get().getTreeView().getTrash().getFolders()){
				r.add(new TrashFolderResource(f));
			}
			trashProvider.setList(r);
	        return new DragAndDropNodeInfo<RestResource>(trashProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
		}
		else if (value instanceof OthersResource) {
	        // Second level.
			OthersDataProvider dataProvider = new OthersDataProvider(
	            ((OthersResource) value), SharedFolderResource.class);
	        return new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, null);
		}
		else if (value instanceof SharedFolderResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((SharedFolderResource) value),SharedFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        //nodeInfos.put(((SharedFolderResource) value).getUri(), n);
	        DroppableOptions options = n.getDroppableOptions();
	        options.setDroppableHoverClass("droppableHover");
	        // use a DroppableFunction here. We can also add a DropHandler in the tree
	        // itself
	        options.setOnDrop(new DroppableFunction() {

	          public void f(DragAndDropContext context) {
	        	  GWT.log("DROPPED");
	            
	          }
	        });
	        // permission cell are not draggable
	        n.setCellDroppableOnly();
	        return n;
		}
		else if (value instanceof OthersFolderResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((OthersFolderResource) value),OthersFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        //nodeInfos.put(((OthersFolderResource) value).getUri(), n);
	        DroppableOptions options = n.getDroppableOptions();
	        options.setDroppableHoverClass("droppableHover");
	        // use a DroppableFunction here. We can also add a DropHandler in the tree
	        // itself
	        options.setOnDrop(new DroppableFunction() {

	          public void f(DragAndDropContext context) {
	        	  GWT.log("DROPPED");
	            
	          }
	        });
	        // permission cell are not draggable
	        n.setCellDroppableOnly();
	        return n;
		}
		else if (value instanceof OtherUserResource) {
	        // Second level.
			MyFolderDataProvider dataProvider = new MyFolderDataProvider(
	            ((OtherUserResource) value),OthersFolderResource.class);
	        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
	            selectionModel, new ResourceValueUpdater());
	        //nodeInfos.put(((OtherUserResource) value).getUri(), n);
	        DroppableOptions options = n.getDroppableOptions();
	        options.setDroppableHoverClass("droppableHover");
	        // use a DroppableFunction here. We can also add a DropHandler in the tree
	        // itself
	        options.setOnDrop(new DroppableFunction() {

	          public void f(DragAndDropContext context) {
	        	  GWT.log("DROPPED");
	            
	          }
	        });
	        // permission cell are not draggable
	        n.setCellDroppableOnly();
	        return n;
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLeaf(Object value) {
		if(value instanceof RestResourceWrapper)
			return ((RestResourceWrapper)value).getResource().getFolders().size()==0;
		if(value instanceof TrashResource)
			return ((TrashResource)value).getFolders().size()==0;
		
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
						((MyFolderResource)value).getResource().setFiles(rootResource.getFiles());
						if(GSS.get().getTreeView().getSelection().getUri().equals(value.getUri()))
							selectionModel.setSelected(value, true);
						GWT.log("UPDATYING");
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
			if(value instanceof TrashResource){
				DeferredCommand.addCommand(new GetCommand<TrashResource>(TrashResource.class, GSS.get().getCurrentUserResource().getTrashPath(), null) {
					@Override
					public void onComplete() {
						//trash = getResult();
						for(RestResource r : getRootNodes().getList()){
							if(r instanceof TrashResource)
								getRootNodes().getList().set(getRootNodes().getList().indexOf(r),GSS.get().getTreeView().getTrash());
						}
						GSS.get().getTreeView().updateNodeChildren(GSS.get().getTreeView().getTrash());
					}

					@Override
					public void onError(Throwable t) {
						if(t instanceof RestException){
							int statusCode = ((RestException)t).getHttpStatusCode();
							// On IE status code 1223 may be returned instead of 204.
							if(statusCode == 204 || statusCode == 1223){
								//trash = new TrashResource(GSS.get().getCurrentUserResource().getTrashPath());
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
		    //CellTreeView.this.mymap.put(department.getUri(), MyFolderDataProvider.this);
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
			  GWT.log("******************************************");
			  GWT.log("[REFRESHING]:"+restResource.getUri());
			  GWT.log("******************************************");
			  GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, restResource.getUri(), null) {

					@Override
					public void onComplete() {
						if(restResource instanceof RestResourceWrapper)
							((RestResourceWrapper)restResource).setResource(getResult());//restResource = getResult();
						
						//if(CellTreeView.this.mymap.get(restResource.getUri())!=null)
							//CellTreeView.this.mymap.get(restResource.getUri()).setRestResource(restResource);
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
						MultipleGetCommand<FolderResource> gf2 = new MultipleGetCommand<FolderResource>(FolderResource.class,
									folderPaths, null) {

							@Override
							public void onComplete() {
								List<RestResource> res = new ArrayList<RestResource>();
								for(FolderResource r : getResult()){
									if(r.isDeleted()){
										
									}
									else if(resourceClass.equals(MyFolderResource.class))
										res.add(new MyFolderResource(r));
									else if(resourceClass.equals(SharedFolderResource.class)){
										GWT.log("ADDING:"+r.getUri());
										res.add(new SharedFolderResource(r));
									}
									else if(resourceClass.equals(TrashFolderResource.class))
										res.add(new TrashFolderResource(r));
									else if(resourceClass.equals(OthersFolderResource.class))
										res.add(new OthersFolderResource(r));
								}
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
		    //CellTreeView.this.mymap.put(department.getUri(), OthersDataProvider.this);
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
			  GWT.log("******************************************");
			  GWT.log("[REFRESHING]:"+restResource.getUri());
			  GWT.log("******************************************");
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
	
	
	
	
}