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
import gr.ebs.gss.client.rest.resource.UserResource;
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTree;
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
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.TreeViewModel.NodeInfo;


/**
 * @author kman
 *
 */
public class CellTreeView extends Composite{
	public static final boolean DONE = false;
	Images images;
	private final ListDataProvider<RestResource> rootNodes = new ListDataProvider<RestResource>();
	SingleSelectionModel<RestResource> selectionModel = new SingleSelectionModel<RestResource>(new ProvidesKey<RestResource>() {

		@Override
		public Object getKey(RestResource item) {
			return item.getClass().getName()+":"+item.getUri();
		}});
	FolderContextMenu menu;
	Map<String,MyFolderDataProvider> mymap=new HashMap<String, MyFolderDataProvider>();
	Map<String,NodeInfo<RestResource>> nodeInfos = new HashMap<String, NodeInfo<RestResource>>();
	MyFolderResource myFolders=null;
	TrashResource trash = null;
	SharedResource myshared = null;
	OthersResource others = null;
	
	TreeViewModel model = new CustomTreeModel();
	
	public interface Images extends ClientBundle,Tree.Resources, FolderContextMenu.Images {

        @Source("gr/ebs/gss/resources/folder_home.png")
        ImageResource home();

        @Source("gr/ebs/gss/resources/folder_yellow.png")
        ImageResource folderYellow();

        @Source("gr/ebs/gss/resources/mimetypes/document.png")
        ImageResource document();

        @Source("gr/ebs/gss/resources/internet.png")
        ImageResource othersShared();

        @Source("gr/ebs/gss/resources/edit_user.png")
        ImageResource myShared();

        @Source("gr/ebs/gss/resources/folder_user.png")
        ImageResource sharedFolder();

        @Source("gr/ebs/gss/resources/trashcan_empty.png")
        ImageResource trash();
	}
	final DragAndDropCellTree tree;
	/*public interface BasicResources extends CellTree.BasicResources{
		@ImageOptions(flipRtl = true)
	    @Source("cellTreeLoadingBasic.gif")
	    ImageResource cellTreeLoading();
		
		@Source({"GssCellTreeBasic.css"})
	    CellTree.Style cellTreeStyle();
	}*/
	public interface BasicResources extends CellTree.Resources {

	    @ImageOptions(flipRtl = true)
	    @Source("cellTreeClosedItem.gif")
	    ImageResource cellTreeClosedItem();

	    @ImageOptions(flipRtl = true)
	    @Source("cellTreeLoadingBasic.gif")
	    ImageResource cellTreeLoading();

	    @ImageOptions(flipRtl = true)
	    @Source("cellTreeOpenItem.gif")
	    ImageResource cellTreeOpenItem();

	    //@Source({CellTree.Style.DEFAULT_CSS,"GssCellTreeBasic.css"})
	    @Source({"GssCellTreeBasic.css"})
	    CellTree.Style cellTreeStyle();
	  }
	/**
	 * 
	 */
	public CellTreeView(final Images _images) {
		images = _images;
		

	    /*
	     * Create the tree using the model. We use <code>null</code> as the default
	     * value of the root node. The default value will be passed to
	     * CustomTreeModel#getNodeInfo();
	     */
		CellTree.Resources res = GWT.create(BasicResources.class);
	    tree = new DragAndDropCellTree(model,null, res){
	    	@Override
	    	public void onBrowserEvent(Event event) {
	    		// TODO Auto-generated method stub
	    		super.onBrowserEvent(event);
	    	}
	    };
	    tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
	    
	    Handler selectionHandler = new SelectionChangeEvent.Handler() { 
            @Override 
            public void onSelectionChange(com.google.gwt.view.client.SelectionChangeEvent event) {
            	NodeInfo<RestResource> nodeInfo = (NodeInfo<RestResource>) getModel().getNodeInfo(selectionModel.getSelectedObject());
            	
            	//GWT.log("SLECTED NODE INFO:"+nodeInfo+"   "+tree.getRootTreeNode().getChildValue(1));
            	//((DefaultNodeInfo<RestResource>)nodeInfo).getValueUpdater().update(selectionModel.getSelectedObject());
            	//if(nodeInfo.getProvidesKey() instanceof FolderDataProvider)
            		//((FolderDataProvider)nodeInfo.getProvidesKey()).onRangeChanged(null);
            	if(nodeInfo==null || nodeInfo.getValueUpdater()==null)
            		GWT.log("VALUE UPDATER IS NULL");
            	else
            		nodeInfo.getValueUpdater().update(selectionModel.getSelectedObject());
            	GSS.get().setCurrentSelection(selectionModel.getSelectedObject());
            	GSS.get().showFileList(true);
            	//tree.fireEvent(new OpenEvent<RestResource>(selectionModel.getSelectedObject()));
            	
            }
        };
        //OpenEvent<RestResource>.fire(tree, selectionModel.getSelectedObject());
        selectionModel.addSelectionChangeHandler(selectionHandler);
	    sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
	    initWidget(tree);
	    DeferredCommand.addCommand(new IncrementalCommand() {

			@Override
			public boolean execute() {
				return fetchRootFolders();
			}
		});
	}
	
	public void updateNode(RestResource resource){
		NodeInfo<RestResource> nodeInfo = (NodeInfo<RestResource>) getModel().getNodeInfo(resource);
		
		GWT.log("NODE INFO:"+nodeInfo);
		if(nodeInfo!=null){
	    	if(nodeInfo.getValueUpdater()==null)
	    		GWT.log("VALUE UPDATER IS NULL");
	    	else
	    		nodeInfo.getValueUpdater().update(resource);
    	}
	}
	
	public void updateNodeChildren(final RestResource resource){
		if(resource instanceof RestResourceWrapper)
			if(((RestResourceWrapper)resource).getResource().getFolders().size()==0){
				mymap.get(((RestResourceWrapper)resource).getResource().getParentURI()).refresh(null);
				return;
			}
		refreshNodeContainingResource(resource);
	}
	
	public void updateNodeChildren(final String resource){
		refreshNodeContainingResource(resource);
	}
	
	protected void showPopup(final int x, final int y) {
		if (selectionModel.getSelectedObject() == null)
			return;
		if (menu != null)
			menu.hide();
		menu = new FolderContextMenu(images);
		menu.setPopupPosition(x, y);
		menu.show();
	}
	private boolean init=false;
	public boolean fetchRootFolders() {
		UserResource userResource = GSS.get().getCurrentUserResource();
		if (userResource == null)
			return !DONE;
		if(!init){
			final String path = userResource.getFilesPath();
			GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, path, null) {
	
				@Override
				public void onComplete() {
					myFolders = new MyFolderResource(getResult());
					selectionModel.setSelected(myFolders, true);
					//rootNodes.setList(Arrays.asList((RestResource)rootResource));
					//tree.getRootTreeNode().setChildOpen(0, true);
				}
	
				@Override
				public void onError(Throwable t) {
					GWT.log("Error fetching root folder", t);
					GSS.get().displayError("Unable to fetch root folder");
				}
	
			};
			DeferredCommand.addCommand(gf);
			DeferredCommand.addCommand(new GetCommand<TrashResource>(TrashResource.class, GSS.get().getCurrentUserResource().getTrashPath(), null) {
				@Override
				public void onComplete() {
					trash = getResult();
				}

				@Override
				public void onError(Throwable t) {
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						// On IE status code 1223 may be returned instead of 204.
						if(statusCode == 204 || statusCode == 1223){
							trash = new TrashResource(GSS.get().getCurrentUserResource().getTrashPath());
					}
					else{
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch trash folder:"+t.getMessage());
						trash = new TrashResource(GSS.get().getCurrentUserResource().getTrashPath());
					}
				}
			}
			});
			GetCommand<SharedResource> gs = new GetCommand<SharedResource>(SharedResource.class, userResource.getSharedPath(), null) {

				@Override
				public void onComplete() {
					myshared=getResult();
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("Error fetching Shared Root folder", t);
					GSS.get().displayError("Unable to fetch Shared Root folder");
				}
			};
			DeferredCommand.addCommand(gs);
			GetCommand<OthersResource> go = new GetCommand<OthersResource>(OthersResource.class,
						userResource.getOthersPath(), null) {

				@Override
				public void onComplete() {
					others = getResult();
					GSS.get().removeGlassPanel();
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("Error fetching Others Root folder", t);
					GSS.get().displayError("Unable to fetch Others Root folder");
				}
			};
			DeferredCommand.addCommand(go);
		}
		if(myFolders==null||trash==null||myshared==null||others==null)
			return !DONE;
		rootNodes.setList(Arrays.asList((RestResource)myFolders,(RestResource)trash,(RestResource)myshared,(RestResource)others));
		tree.getRootTreeNode().setChildOpen(0, true);
		return DONE;
	}

	public Images getImages() {
		return images;
	}
	
	
	class CustomTreeModel implements TreeViewModel{
		private final Cell<RestResource> departmentCell = new AbstractCell<RestResource>("contextmenu"){
			
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context arg0, RestResource arg1, SafeHtmlBuilder arg2) {
				String html=null;
				String name=null;
				if(arg1 instanceof RestResourceWrapper){
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
				if(arg1 instanceof TrashResource){
					html = AbstractImagePrototype.create(images.trash()).getHTML();
					name="Trash";
				}
				if(arg1 instanceof SharedResource){
					html = AbstractImagePrototype.create(images.myShared()).getHTML();
					name="My Shared";
				}
				if(arg1 instanceof OthersResource){
					html = AbstractImagePrototype.create(images.othersShared()).getHTML();
					name = "Other's Shared";
				}
				if(arg1 instanceof OtherUserResource){
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
					showPopup(event.getClientX(), event.getClientY());
				}
			};
			
		};
		
		
		@Override
		public <T> NodeInfo<?> getNodeInfo(T value) {
			
			if(value==null){
				return new DragAndDropNodeInfo<RestResource>(rootNodes, departmentCell,
				            selectionModel, null);
			}
			else if (value instanceof MyFolderResource) {
		        // Second level.
				MyFolderDataProvider dataProvider = new MyFolderDataProvider(
		            ((MyFolderResource) value),MyFolderResource.class);
		        DragAndDropNodeInfo<RestResource> n =  new DragAndDropNodeInfo<RestResource>(dataProvider, departmentCell,
		            selectionModel, new ResourceValueUpdater());
		        nodeInfos.put(((MyFolderResource) value).getUri(), n);
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
		            selectionModel, null);
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
		        nodeInfos.put(((SharedFolderResource) value).getUri(), n);
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
		        nodeInfos.put(((OthersFolderResource) value).getUri(), n);
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
		        nodeInfos.put(((OtherUserResource) value).getUri(), n);
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
						if(getSelection().getUri().equals(value.getUri()))
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
		    CellTreeView.this.mymap.put(department.getUri(), MyFolderDataProvider.this);
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
						//restResource = getResult();
						
						if(CellTreeView.this.mymap.get(restResource.getUri())!=null)
							CellTreeView.this.mymap.get(restResource.getUri()).setRestResource(restResource);
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
	
	
	public RestResource getSelection(){
		return selectionModel.getSelectedObject();
	}
	
	
	/**
	 * Retrieve the myFolders.
	 *
	 * @return the myFolders
	 */
	public MyFolderResource getMyFolders() {
		return myFolders;
	}
	
	
	/**
	 * Retrieve the myshared.
	 *
	 * @return the myshared
	 */
	public SharedResource getMyshared() {
		return myshared;
	}
	
	
	/**
	 * Retrieve the trash.
	 *
	 * @return the trash
	 */
	public TrashResource getTrash() {
		return trash;
	}
	
	
	/**
	 * Retrieve the others.
	 *
	 * @return the others
	 */
	public OthersResource getOthers() {
		return others;
	}
	
	
	/**
	 * Retrieve the model.
	 *
	 * @return the model
	 */
	public TreeViewModel getModel() {
		return model;
	}
	
	private void refreshNodeContainingResource(RestResource r){
		TreeNode node = tree.getRootTreeNode();
		refreshNodeContainingResource(node,r);
	}
	
	private void refreshNodeContainingResource(String  uri){
		TreeNode node = tree.getRootTreeNode();
		refreshNodeContainingResource(node,uri);
	}
	
	private void refreshNodeContainingResource(TreeNode node, RestResource resource){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			if(node.isChildOpen(i)){
				if(node.getChildValue(i).equals(resource)){
					GWT.log("FOUND RESOURCE");
					node.setChildOpen(i, false, true);
					node.setChildOpen(i, true, true);
					return;
				}
				else{
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null)
						refreshNodeContainingResource(n,resource);
				}
			}
		}
	}
	
	private void refreshNodeContainingResource(TreeNode node, String uri){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			if(node.isChildOpen(i)){
				if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).getUri().equals(uri)){
					GWT.log("FOUND RESOURCE");
					node.setChildOpen(i, false, true);
					node.setChildOpen(i, true, true);
					return;
				}
				else{
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null)
						refreshNodeContainingResource(n,uri);
				}
			}
		}
	}
	
	
	public interface RefreshHandler{
		void onRefresh();		
	}
}
