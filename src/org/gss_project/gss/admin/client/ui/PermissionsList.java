/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
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
package org.gss_project.gss.admin.client.ui;

import org.gss_project.gss.admin.client.TwoAdmin;
import org.gss_project.gss.common.dto.PermissionDTO;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * @author kman
 *
 */
public class PermissionsList extends Composite {

	int selectedRow = -1;
	int permissionCount=-1;
	Set<PermissionDTO> permissions = null;

	final VerticalPanel permPanel = new VerticalPanel();
	final FlexTable permTable = new FlexTable();
	final String owner;
	PermissionDTO toRemove = null;
	private boolean hasChanges = false;
	private boolean hasAddition = false;
	private boolean allowEditPermissions = false;
	private String uri;
	public PermissionsList(Set<PermissionDTO> thePermissions, String anOwner, boolean _allowEditPermissions){
		owner = anOwner;
		permissions =  new HashSet<PermissionDTO>();
		permissions.addAll(thePermissions);
		permTable.setText(0, 0, "Users/Groups");
		permTable.setText(0, 1, "Read");
		permTable.setText(0, 2, "Write");
		permTable.setText(0, 3, "Modify Access");
		permTable.setText(0, 4, "");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		permPanel.add(permTable);
		Button savePermissions = new Button("Save");
		permPanel.add(savePermissions);

		savePermissions.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				updatePermissionsAccordingToInput();
				if(hasChanges())
					TwoAdmin.get().getAdminService().setFilePermissions(uri,permissions, new AsyncCallback<Void>() {

						@Override
						public void onSuccess(Void result) {


						}

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Error fetching file", caught);
							TwoAdmin.get().showErrorBox("Unable to Find File");

						}
					});

			}
		});
		permPanel.addStyleName("gss-TabPanelBottom");
		allowEditPermissions = _allowEditPermissions;
		initWidget(permPanel);
		updateTable();
	}

	public boolean hasChanges(){
		return hasChanges || hasAddition;
	}

	public void clear(){
		permissions= new HashSet<PermissionDTO>();
		uri=null;
		updateTable();
	}
	public void update(Set<PermissionDTO> per, String aUri){
		permissions=per;
		uri=aUri;
		updateTable();
	}
	public void updateTable(){
		int i=1;
		if(toRemove != null){
			permissions.remove(toRemove);
			toRemove = null;
		}
		for(final PermissionDTO dto : permissions){

			Button removeButton = new Button("remove", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					toRemove = dto;
					updateTable();
					hasChanges = true;
				}
			});

			if(dto.getUser() !=null)
				if(dto.getUser()!=null && dto.getUser().getUsername().equals(owner)){
					permTable.setHTML(i, 0, "<span>"  + "&nbsp;Owner</span>");
					removeButton.setVisible(false);
				}
				else{
					HTML userLabel = new HTML("<a href='#'>"+dto.getUser().getUsername()+"</a></span>");
					permTable.setWidget(i, 0, userLabel);
					userLabel.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							TwoAdmin.get().searchUsers("username:"+dto.getUser().getUsername());

						}
					});
				}
			else if(dto.getGroup() != null) {
				//String user = GSS.get().getCurrentUserResource().getUsername();
				String[] names = dto.getGroup().getName().split("/");
				String name = URL.decodeComponent(names[names.length - 1]);
				//String ownr = names.length>1 ? URL.decodeComponent(names[names.length - 3]) : user;
				String groupName =  name;
				permTable.setHTML(i, 0, "<span>" +  "&nbsp;"+groupName+"</span>");
			}
			CheckBox read = new CheckBox();
			read.setValue(dto.getRead());
			CheckBox write = new CheckBox();
			write.setValue(dto.getWrite());
			CheckBox modify = new CheckBox();
			modify.setValue(dto.getModifyACL());
			permTable.setWidget(i, 1, read);
			permTable.setWidget(i, 2, write);
			permTable.setWidget(i, 3, modify);
			if (dto.getUser()!=null && dto.getUser().getUsername().equals(owner) || !allowEditPermissions) {
				read.setEnabled(false);
				write.setEnabled(false);
				modify.setEnabled(false);
			}
			else
				permTable.setWidget(i, 4, removeButton);
			permTable.getFlexCellFormatter().setStyleName(i, 0, "props-labels");
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 3, HasHorizontalAlignment.ALIGN_CENTER);
			i++;
		}
		for(; i<permTable.getRowCount(); i++)
			permTable.removeRow(i);
		hasChanges = false;

	}

	public void updatePermissionsAccordingToInput(){
		int i=1;
		for(PermissionDTO dto : permissions){
			/*if(dto.getId() == null)
				hasChanges =true;*/
			CheckBox r = (CheckBox) permTable.getWidget(i, 1);
			CheckBox w = (CheckBox) permTable.getWidget(i, 2);
			CheckBox m = (CheckBox) permTable.getWidget(i, 3);
			if(dto.getRead() != r.getValue() || dto.getWrite() != w.getValue() || dto.getModifyACL() != m.getValue())
				hasChanges = true;
			dto.setRead(r.getValue());
			dto.setWrite(w.getValue());
			dto.setModifyACL(m.getValue());
			i++;
		}
	}


	/**
	 * Retrieve the permissions.
	 *
	 * @return the permissions
	 */
	public Set<PermissionDTO> getPermissions() {
		return permissions;
	}

	public void addPermission(PermissionDTO permission){
		permissions.add(permission);
		hasAddition = true;
	}


}
