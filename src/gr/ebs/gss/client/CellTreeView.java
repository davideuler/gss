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

import gr.ebs.gss.client.CellTreeViewModel.ClearSelection;
import gr.ebs.gss.client.CellTreeViewModel.MyFolderDataProvider;
import gr.ebs.gss.client.CellTreeViewModel.ResourceValueUpdater;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.MyFolderResource;
import gr.ebs.gss.client.rest.resource.OtherUserResource;
import gr.ebs.gss.client.rest.resource.OthersFolderResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.RestResourceWrapper;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashFolderResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UserResource;
import gwtquery.plugins.draggable.client.DragAndDropManager;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.events.OverDroppableEvent;
import gwtquery.plugins.droppable.client.events.OverDroppableEvent.OverDroppableEventHandler;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTree;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.query.client.plugins.GQueryUi;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
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
	
	SingleSelectionModel<RestResource> selectionModel = new SingleSelectionModel<RestResource>(new ProvidesKey<RestResource>() {

		@Override
		public Object getKey(RestResource item) {
			return item.getClass().getName()+":"+item.getUri();
		}});
	FolderContextMenu menu;
	
	
	MyFolderResource myFolders=null;
	TrashResource trash = null;
	SharedResource myshared = null;
	OthersResource others = null;
	
	CellTreeViewModel model;
	CellTreeViewUtils utils;
	
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
	DragAndDropCellTree tree;
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
		model = new CellTreeViewModel(images,selectionModel);
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
	    utils=new CellTreeViewUtils(tree);
	    tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
	    /*tree.addOpenHandler(new OpenHandler<TreeNode>() {
			
			@Override
			public void onOpen(OpenEvent<TreeNode> event) {
				Window.alert("[NODE OPENED]"+event.getTarget());
				
			}
		});
	    tree.addOverDroppableHandler(new OverDroppableEventHandler() {
			
			@Override
			public void onOverDroppable(OverDroppableEvent event) {
				GWT.log("OVER:"+event);
				
			}
		});
	    tree.addAttachHandler(new AttachEvent.Handler() {
			
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				GWT.log("ATTACH:"+event.getSource());
				
			}
		});*/
	    Handler selectionHandler = new SelectionChangeEvent.Handler() { 
            @Override 
            public void onSelectionChange(com.google.gwt.view.client.SelectionChangeEvent event) {
            	NodeInfo<RestResource> nodeInfo = (NodeInfo<RestResource>) getModel().getNodeInfo(selectionModel.getSelectedObject());
            	if(nodeInfo==null || nodeInfo.getValueUpdater()==null){
            		GSS.get().showFileList(getSelection());
            	}
            	else
            		nodeInfo.getValueUpdater().update(selectionModel.getSelectedObject());
            	GSS.get().setCurrentSelection(selectionModel.getSelectedObject());
            	
            	
            }
        };
        selectionModel.addSelectionChangeHandler(selectionHandler);
	    sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
	    initWidget(tree);
	    FolderResource loadingResource = new FolderResource("loading");
	    loadingResource.setName("Loading....");
	    loadingResource.setShared(false);
	    RestResourceWrapper loading = new RestResourceWrapper(loadingResource);
	    model.getRootNodes().setList(Arrays.asList((RestResource)loading));
		
	    DeferredCommand.addCommand(new IncrementalCommand() {

			@Override
			public boolean execute() {
				return fetchRootFolders();
			}
		});
	}
	
	public void updateNode(RestResource resource){
		NodeInfo<RestResource> nodeInfo = (NodeInfo<RestResource>) getModel().getNodeInfo(resource);
		
		if(nodeInfo!=null){
	    	if(nodeInfo.getValueUpdater()==null){}
	    	else
	    		nodeInfo.getValueUpdater().update(resource);
    	}
	}
	
	public void updateNodeChildren(final RestResource resource){
		
		if(resource instanceof RestResourceWrapper){
			boolean updated=false;
			if(((RestResourceWrapper)resource).getResource().getFolders().size()==0){
				if(((RestResourceWrapper)resource).getResource().getParentURI().equals(getMyFolders().getUri())){
					updateNodeChildren(getMyFolders().getUri());
					return;
				}
				if(model.getMymap().get(((RestResourceWrapper)resource).getResource().getParentURI())!=null){
					model.getMymap().get(((RestResourceWrapper)resource).getResource().getParentURI()).refresh(null);
					updated=true;
				}
				if(model.getOthersmap().get(((RestResourceWrapper)resource).getResource().getParentURI())!=null){
					model.getOthersmap().get(((RestResourceWrapper)resource).getResource().getParentURI()).refresh(null);
					updated=true;
				}
				if(model.getSharedmap().get(((RestResourceWrapper)resource).getResource().getParentURI())!=null){
					model.getSharedmap().get(((RestResourceWrapper)resource).getResource().getParentURI()).refresh(null);
					updated=true;
				}
				if(updated){
					if(utils.doesSharedNodeContainsResourceIn1stLevel(resource.getUri())){
						updateMySharedNode();
					}
					else if(tree.getRootTreeNode().isChildOpen(2)){
						utils.refreshNodeContainingResource(tree.getRootTreeNode().setChildOpen(2,true),resource.getUri());
					}
					//return;
				}
			}
		}
		
		utils.refreshNodeContainingResource(resource);
		if(utils.doesSharedNodeContainsResourceIn1stLevel(resource.getUri())){
			updateMySharedNode();
		}
		else if(tree.getRootTreeNode().isChildOpen(2)){
			GWT.log("REFRESH THE OTHER WAY 2:"+resource);
			utils.refreshNodeContainingResource(tree.getRootTreeNode().setChildOpen(2,true),resource.getUri());
		}
		
	}
	public void updateNodeChildrenForRemove(final String resource){
		GWT.log("********************************");
		GWT.log("[UPDATENODECHILDREN]"+resource);
		GWT.log("********************************");
		boolean updated=false;
		TreeNode node=null;
		TreeNode sharedNode=null;
		if(tree.getRootTreeNode().isChildOpen(0)){
			node = utils.getNodeContainingResource2(tree.getRootTreeNode().setChildOpen(0,true), resource);
		}
			GWT.log("CHECK NODE1:"+node+" "+resource);
		
			if(tree.getRootTreeNode().isChildOpen(2)){
				GWT.log("CHECK NODE2:"+node);
				if(node==null)
					node = utils.getNodeContainingResource2(tree.getRootTreeNode().setChildOpen(2,true), resource);
				
			}
		if(node==null)
			if(tree.getRootTreeNode().isChildOpen(3)){
				GWT.log("CHECK NODE3:"+node);
				node = utils.getNodeContainingResource2(tree.getRootTreeNode().setChildOpen(3,true), resource);
		}
		if(node != null && node.getValue() instanceof RestResourceWrapper){
			GWT.log("*********************"+((RestResourceWrapper) node.getValue()).getResource().getFolders().size());
			RestResourceWrapper wrapper  = (RestResourceWrapper) node.getValue();
			if(wrapper.getResource().countNotDeletedSubfolders()==1||wrapper.getResource().countNotDeletedSubfolders()==0){
				updateNodeChildren(((RestResourceWrapper) node.getValue()).getResource().getParentURI());
				if(((RestResourceWrapper) node.getValue()).getResource().getParentURI().equals(myFolders.getUri())){
					if(utils.doesSharedNodeContainsResourceIn1stLevel(resource)||utils.doesSharedNodeContainsResourceIn2ndLevel(resource)){
						updateMySharedNode();
					}
				}
							
			}
			else
				updateNodeChildren(((RestResource) node.getValue()).getUri());
			return;
		}
		updateNodeChildren(resource);
	}
	public void updateNodeChildren(final String resource){
		
			
		GWT.log("REFRESH THE OTHER WAY");
		utils.refreshNodeContainingResource(resource);
		if(utils.doesSharedNodeContainsResourceIn1stLevel(resource)||utils.doesSharedNodeContainsResourceIn2ndLevel(resource)){
			GWT.log("REFRESH THE OTHER WAY 1:"+resource);
			updateMySharedNode();
		}
		else if(tree.getRootTreeNode().isChildOpen(2)){
			GWT.log("REFRESH THE OTHER WAY 2:"+resource);
			utils.refreshNodeContainingResource(tree.getRootTreeNode().setChildOpen(2,true),resource);
		}
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
					//selectionModel.setSelected(myFolders, true);
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
		model.getRootNodes().setList(Arrays.asList((RestResource)myFolders,(RestResource)trash,(RestResource)myshared,(RestResource)others));
		tree.getRootTreeNode().setChildOpen(0, true);
		selectionModel.setSelected(myFolders, true);
		return DONE;
	}

	public Images getImages() {
		return images;
	}
	
	
	public void updateTrashNode(){
		DeferredCommand.addCommand(new GetCommand<TrashResource>(TrashResource.class, GSS.get().getCurrentUserResource().getTrashPath(), null) {
			@Override
			public void onComplete() {
				trash = getResult();
				boolean trashIsOpen = tree.getRootTreeNode().isChildOpen(1);
				model.getRootNodes().getList().set(1, trash);
				model.getRootNodes().refresh();
				tree.getRootTreeNode().setChildOpen(1, true);
			}

			@Override
			public void onError(Throwable t) {
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					// On IE status code 1223 may be returned instead of 204.
					if(statusCode == 204 || statusCode == 1223){
						trash = new TrashResource(GSS.get().getCurrentUserResource().getTrashPath());
						model.getRootNodes().getList().set(1, trash);
						//model.getRootNodes().refresh();
				}
				else{
					GWT.log("", t);
					GSS.get().displayError("Unable to fetch trash folder:"+t.getMessage());
					trash = new TrashResource(GSS.get().getCurrentUserResource().getTrashPath());
					model.getRootNodes().getList().set(1, trash);
					//model.getRootNodes().refresh();
				}
			}
		}
		});
	}
	
	public void updateRootNode(){
		final String path = GSS.get().getCurrentUserResource().getFilesPath();
		GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, path, null) {

			@Override
			public void onComplete() {
				myFolders = new MyFolderResource(getResult());
				model.getRootNodes().getList().set(0, myFolders);
				model.getRootNodes().refresh();
				tree.getRootTreeNode().setChildOpen(0, true);
				
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Error fetching root folder", t);
				GSS.get().displayError("Unable to fetch root folder");
			}

		};
		DeferredCommand.addCommand(gf);
	}
	
	public void updateMySharedNode(){
		GetCommand<SharedResource> gs = new GetCommand<SharedResource>(SharedResource.class, GSS.get().getCurrentUserResource().getSharedPath(), null) {

			@Override
			public void onComplete() {
				myshared=getResult();
				model.getRootNodes().getList().set(2, myshared);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Error fetching Shared Root folder", t);
				GSS.get().displayError("Unable to fetch Shared Root folder");
			}
		};
		DeferredCommand.addCommand(gs);
	}
	
	public void updateOtherNode(){
		GetCommand<OthersResource> go = new GetCommand<OthersResource>(OthersResource.class,
					GSS.get().getCurrentUserResource().getOthersPath(), null) {

			@Override
			public void onComplete() {
				others = getResult();
				model.getRootNodes().getList().set(3, others);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Error fetching Others Root folder", t);
				GSS.get().displayError("Unable to fetch Others Root folder");
			}
		};
		DeferredCommand.addCommand(go);
	}
	
	
	public RestResource getSelection(){
		return selectionModel.getSelectedObject();
	}
	
	public void clearSelection(){
		if(GSS.get().getCurrentSelection().equals(getSelection()))
			GSS.get().setCurrentSelection(null);
		selectionModel.setSelected(getSelection(), false);
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
	
	
	
	/**
	 * Retrieve the utils.
	 *
	 * @return the utils
	 */
	public CellTreeViewUtils getUtils() {
		return utils;
	}
	
	public interface RefreshHandler{
		void onRefresh();		
	}
	
	
	public boolean isTrashOrTrashFolderSelected(){
		return (getSelection() instanceof TrashResource) || (getSelection() instanceof TrashFolderResource);
	}
	
	public OtherUserResource getOtherUserResourceOfOtherFolder(OthersFolderResource res){
		TreeNode n = utils.getNodeContainingResource(tree.getRootTreeNode().setChildOpen(3, true), res);
		
		if(n!=null){
			if(n.getValue() instanceof OtherUserResource)
				return (OtherUserResource) n.getValue();
			TreeNode parent = n.getParent();
			
			while (parent!=null){
				if(parent.getValue() instanceof OtherUserResource)
					return (OtherUserResource) parent.getValue();
				parent = parent.getParent();
			}
		}
		return null;
	}
	
	public void refreshCurrentNode(boolean clearSelection){
		NodeInfo<RestResource> nodeInfo = (NodeInfo<RestResource>) getModel().getNodeInfo(selectionModel.getSelectedObject());
    	if(nodeInfo==null || nodeInfo.getValueUpdater()==null){
    		GSS.get().showFileList(getSelection(),clearSelection);
    	}
    	else{
    		if(!clearSelection)
    			((ClearSelection)nodeInfo.getValueUpdater()).setClearSelection(clearSelection);
    		nodeInfo.getValueUpdater().update(selectionModel.getSelectedObject());
    	}
	}
	
}
