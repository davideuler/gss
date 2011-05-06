package org.gss_project.gss.admin.client.ui;

import org.gss_project.gss.admin.client.TwoAdmin;
import org.gss_project.gss.common.dto.FileBodyDTO;
import org.gss_project.gss.common.dto.FileHeaderDTO;
import org.gss_project.gss.common.dto.PermissionDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class FilesPanel extends Composite {

	private static UserPanelUiBinder uiBinder = GWT
			.create(UserPanelUiBinder.class);

	interface UserPanelUiBinder extends UiBinder<Widget, FilesPanel> {
	}



	@UiField TextBox searchBox;
	@UiField Button searchButton;
	@UiField(provided=true) final FilesTable filesTable = new FilesTable();
	@UiField(provided=true) Grid g =new Grid(9,6);
	@UiField(provided=true) PermissionsList permissionsGrid = new PermissionsList(new HashSet<PermissionDTO>(),"",true);
	@UiField(provided=true) VersionsList versionsList = new VersionsList(new ArrayList<FileBodyDTO>());
	private Object lastQuery;

	public FilesPanel() {
		g.setCellPadding(5);
		g.setCellSpacing(5);

		initWidget(uiBinder.createAndBindUi(this));
		searchBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				char keyCode = event.getCharCode();
				if (keyCode == '\r')
					handleClick(null);
			}
		});
		filesTable.addRowSelectionHandler(new RowSelectionHandler() {

			@Override
			public void onRowSelection(RowSelectionEvent event) {
				final FileHeaderDTO user = filesTable.getSelectedRowObject(event);

				TwoAdmin.get().showLoadingBox();
				if(user!=null)
					DeferredCommand.addCommand(new Command() {

						@Override
						public void execute() {
							TwoAdmin.get().showLoadingBox();
							TwoAdmin.get().getAdminService().getFile(user.getId(), new AsyncCallback<FileHeaderDTO>() {

								@Override
								public void onSuccess(final FileHeaderDTO result) {
									TwoAdmin.get().getAdminService().getVersions(result.getOwner().getId(), result.getId(), new AsyncCallback<List<FileBodyDTO>>() {

										@Override
										public void onFailure(Throwable caught) {
											GWT.log("Error requesting  file details", caught);
											TwoAdmin.get().hideLoadingBox();
											TwoAdmin.get().showErrorBox("Error requesting file details");

										}

										@Override
										public void onSuccess(List<FileBodyDTO> versions) {
											clearFileDetails();
											displayFileDetails(result, versions);
											TwoAdmin.get().hideLoadingBox();


										}

									});

								}

								@Override
								public void onFailure(Throwable caught) {
									GWT.log("Error requesting user statistics file", caught);
									TwoAdmin.get().hideLoadingBox();
									TwoAdmin.get().showErrorBox("Error requesting user statistics");

								}
							});

						}
					});

			}
		});


		clearFileDetails();

	}

	@UiHandler("searchButton")
	void handleClick(@SuppressWarnings("unused") ClickEvent e){
		final String toSearch = searchBox.getText();
		if(toSearch == null || "".equals(toSearch.trim())){
			TwoAdmin.get().showErrorBox("You must enter a query");
			return;
		}
		lastQuery = toSearch;
		populateTable(toSearch);
	}



	public void populateTable(String query){
		TwoAdmin.get().showLoadingBox();
		TwoAdmin.get().getAdminService().searchFiles(query,new AsyncCallback<List<FileHeaderDTO>>() {

			@Override
			public void onFailure(Throwable caught) {
				TwoAdmin.get().hideLoadingBox();
				GWT.log("Error fetching files", caught);
				TwoAdmin.get().showErrorBox("Unable to Find any Files");
			}

			@Override
			public void onSuccess(List<FileHeaderDTO> result) {
				filesTable.showUsers(result);
				clearFileDetails();
				TwoAdmin.get().hideLoadingBox();

			}

		});
	}




	public void displayFileDetails(final FileHeaderDTO file, List<FileBodyDTO> versions){
		clearFileDetails();
		versionsList.updateTable(versions);
		g.setHTML(0, 0, "<span>"  + "Name:");
		g.setHTML(0, 1, file.getName()+"</span>");
		g.setHTML(1, 0, "<span>"  + "URI:");
		g.setHTML(1, 1, file.getURI()+"</span>");
		g.setHTML(2, 0, "<span>"  + "Owner:");
		HTML userLabel =new HTML("<a href='#'>"+file.getOwner().getUsername()+"</a></span>");
		g.setWidget(2, 1, userLabel );

		userLabel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				TwoAdmin.get().searchUsers("username:"+file.getOwner().getUsername());

			}
		});
		g.setHTML(3, 0, "<span>"  + "Size:");
		g.setHTML(3, 1, file.getFileSizeAsString()+"</span>");
		g.setHTML(4, 0, "<span>"  + "Content Type:");
		g.setHTML(4, 1, file.getMimeType()+"</span>");

		g.setHTML(5, 0, "<span>"  + "Creation Date:");
		g.setHTML(5, 1, file.getAuditInfo().getCreationDate()+"</span>");

		g.setHTML(6, 0, "<span>"  + "Modification Date:");
		g.setHTML(6, 1, file.getAuditInfo().getModificationDate()+"</span>");
		if(file.isVersioned())
			g.setHTML(7, 0, "<span>"  + "File is Versioned:</span>");
		else
			g.setHTML(7, 0, "<span>"  + "File is NOT Versioned:</span>");
		if(file.isReadForAll()){
			g.setHTML(8, 0, "<span>"  + "Read For All:");
			g.setHTML(8, 1, file.getURI()+"</span>");
		}
		permissionsGrid.update(file.getPermissions(), file.getURI());
	}

	public void clearFileDetails(){
		g.setHTML(0, 0, "<span>"  + "Name:");
		g.setHTML(0, 1, ""+"</span>");
		g.setHTML(1, 0, "<span>"  + "URI:");
		g.setHTML(1, 1, ""+"</span>");
		g.setHTML(2, 0, "<span>"  + "Owner:");
		g.setHTML(2, 1, ""+"</span>");
		g.setHTML(3, 0, "<span>"  + "Size:");
		g.setHTML(3, 1, ""+"</span>");
		g.setHTML(4, 0, "<span>"  + "Content Type:");
		g.setHTML(4, 1, ""+"</span>");
		g.setHTML(5, 0, "<span>"  + "Creation Date:");
		g.setHTML(5, 1, "</span>");

		g.setHTML(6, 0, "<span>"  + "Modification Date:");
		g.setHTML(6, 1, "</span>");
		g.setHTML(7, 0, "<span>"  + "File is Versioned:</span>");
		g.setHTML(8, 0, "<span>"  + "Read For All:");
		g.setHTML(8, 1, "</span>");
		permissionsGrid.clear();
		versionsList.updateTable(new ArrayList<FileBodyDTO>());


	}


	/**
	 * Retrieve the lastQuery.
	 *
	 * @return the lastQuery
	 */
	public Object getLastQuery() {
		return lastQuery;
	}
}
