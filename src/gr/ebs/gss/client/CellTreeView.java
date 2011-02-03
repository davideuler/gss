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

import gr.ebs.gss.client.CellTreeViewModel.MyFolderDataProvider;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.MyFolderResource;
import gr.ebs.gss.client.rest.resource.OthersResource;
import gr.ebs.gss.client.rest.resource.RestResource;
import gr.ebs.gss.client.rest.resource.RestResourceWrapper;
import gr.ebs.gss.client.rest.resource.SharedResource;
import gr.ebs.gss.client.rest.resource.TrashResource;
import gr.ebs.gss.client.rest.resource.UserResource;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTree;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.IncrementalCommand;
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
				if(model.getMymap().get(((RestResourceWrapper)resource).getResource().getParentURI())!=null){
					model.getMymap().get(((RestResourceWrapper)resource).getResource().getParentURI()).refresh(null);
					return;
				}
				
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
		GWT.log("PAPALA");
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
				model.getRootNodes().getList().set(1, trash);
				//model.getRootNodes().refresh();
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
	public void openNodeContainingResource(RestResource resource){
		TreeNode node = tree.getRootTreeNode();
		openNodeContainingResource(node,resource);
	}
	private void openNodeContainingResource(TreeNode node, RestResource resource){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
				if(node.getChildValue(i).equals(resource)){
					GWT.log("FOUND RESOURCE");
					//node.setChildOpen(i, false, true);
					node.setChildOpen(i, true, true);
					return;
				}
				else{
					if(node.isChildOpen(i)){
						TreeNode n = node.setChildOpen(i, true);
						if(n!=null)
							openNodeContainingResource(n,resource);
					}
				}
			
		}
	}
	
	public interface RefreshHandler{
		void onRefresh();		
	}
	
	
	
	
	
}
