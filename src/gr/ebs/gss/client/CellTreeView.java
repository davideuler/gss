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

import gr.ebs.gss.client.dnd.DnDTreeItem;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.MultipleGetCommand;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.FolderResource;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.view.client.TreeViewModel.DefaultNodeInfo;


/**
 * @author kman
 *
 */
public class CellTreeView extends Composite{
	public static final boolean DONE = false;
	Images images;
	private final ListDataProvider<FolderResource> rootNodes = new ListDataProvider<FolderResource>();
	SingleSelectionModel<FolderResource> selectionModel = new SingleSelectionModel<FolderResource>();
	/**
	 * Specifies the images that will be bundled for this Composite and other
	 * inherited images that will be included in the same bundle.
	 */
	public interface Images extends ClientBundle, Tree.Resources, FolderContextMenu.Images {

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
	
	/**
	 * 
	 */
	public CellTreeView(final Images _images) {
		images = _images;
		TreeViewModel model = new CustomTreeModel();

	    /*
	     * Create the tree using the model. We use <code>null</code> as the default
	     * value of the root node. The default value will be passed to
	     * CustomTreeModel#getNodeInfo();
	     */
		CellTree.Resources res = GWT.create(CellTree.BasicResources.class);
	    CellTree tree = new CellTree(model,null, res);
	    tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

	    
	    initWidget(tree);
	    DeferredCommand.addCommand(new IncrementalCommand() {

			@Override
			public boolean execute() {
				return fetchRootFolders();
			}
		});
	}
	public boolean fetchRootFolders() {
		UserResource userResource = GSS.get().getCurrentUserResource();
		if (userResource == null)
			return !DONE;

		final String path = userResource.getFilesPath();
		GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, path, null) {

			@Override
			public void onComplete() {
				FolderResource rootResource = getResult();
				rootNodes.setList(Arrays.asList((FolderResource)rootResource));
				
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Error fetching root folder", t);
				GSS.get().displayError("Unable to fetch root folder");
			}

		};
		DeferredCommand.addCommand(gf);
		return DONE;
	}

	public Images getImages() {
		return images;
	}
	
	
	class CustomTreeModel implements TreeViewModel{
		private final Cell<FolderResource> departmentCell = new AbstractCell<FolderResource>(){

			@Override
			public void render(com.google.gwt.cell.client.Cell.Context arg0, FolderResource arg1, SafeHtmlBuilder arg2) {
				if(arg1.getParentName()==null){
					arg2.appendHtmlConstant(AbstractImagePrototype.create(images.home()).getHTML());
				}
				else
					arg2.appendHtmlConstant(AbstractImagePrototype.create(images.folderYellow()).getHTML());
				arg2.appendEscaped(arg1.getName());
				
			}
			
		};
		
		
		@Override
		public <T> NodeInfo<?> getNodeInfo(T value) {
			if(value==null){
				return new DefaultNodeInfo<FolderResource>(rootNodes, departmentCell,
				            selectionModel, null);
			}
			else if (value instanceof FolderResource) {
		        // Second level.
				FolderDataProvider dataProvider = new FolderDataProvider(
		            ((FolderResource) value).getUri());
		        return new DefaultNodeInfo<FolderResource>(dataProvider, departmentCell,
		            selectionModel, null);
			}
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isLeaf(Object value) {
			if(value instanceof FolderResource)
				return ((FolderResource)value).getFolders().size()==0;
			return false;
		}
		
	}
	
	class FolderDataProvider extends AsyncDataProvider<FolderResource>{
		private final String department;

		  public FolderDataProvider(String department) {
		    super(null);
		    this.department = department;
		  }

		  /*@Override
		  public void addDataDisplay(HasData<FolderResource> display) {
		    super.addDataDisplay(display);

		    // Request the count anytime a view is added.
		    requestFactory.employeeRequest().countEmployeesByDepartment(department).fire(
		        new Receiver<Long>() {
		          @Override
		          public void onSuccess(Long response) {
		            updateRowCount(response.intValue(), true);
		          }
		        });
		  }*/

		  @Override
		  protected void onRangeChanged(final HasData<FolderResource> view) {
		    Range range = view.getVisibleRange();
		    GetCommand<FolderResource> gf = new GetCommand<FolderResource>(FolderResource.class, department, null) {

				@Override
				public void onComplete() {
					FolderResource rootResource = getResult();
					MultipleGetCommand<FolderResource> gf2 = new MultipleGetCommand<FolderResource>(FolderResource.class,
								rootResource.getSubfolderPaths().toArray(new String[] {}), null) {

						@Override
						public void onComplete() {
							List<FolderResource> res = getResult();
							updateRowCount(res.size(), true);
							updateRowData(0,res);
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
	
	
}
