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
package gr.ebs.gss.client;

import gr.ebs.gss.client.FilePropertiesDialog.Images;
import gr.ebs.gss.client.rest.resource.PermissionHolder;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author kman
 *
 */
public class PermissionsList extends Composite {

	int selectedRow = -1;
	int permissionCount=-1;
	Set<PermissionHolder> permissions = null;
	final Images images;
	final VerticalPanel permPanel = new VerticalPanel();
	final FlexTable permTable = new FlexTable();
	final String owner;
	PermissionHolder toRemove = null;
	private boolean hasChanges = false;
	private boolean hasAddition = false;

	public PermissionsList(final Images images, Set<PermissionHolder> permissions, String owner){
		this.images = images;
		this.owner = owner;
		this.permissions =  new HashSet<PermissionHolder>();
		this.permissions.addAll(permissions);
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
		permPanel.addStyleName("gwt-TabPanelBottom");
		initWidget(permPanel);
		updateTable();
	}

	public boolean hasChanges(){
		return hasChanges || hasAddition;
	}


	public void updateTable(){
		int i=1;
		if(toRemove != null){
			permissions.remove(toRemove);
			toRemove = null;
		}
		for(final PermissionHolder dto : permissions){

			PushButton removeButton = new PushButton(images.delete().createImage(), new ClickListener() {

				public void onClick(Widget sender) {
					toRemove = dto;
					updateTable();
					hasChanges = true;
				}
			});

			if(dto.getUser() !=null)
				if(dto.getUser()!=null && dto.getUser().equals(owner)){
					permTable.setHTML(i, 0, "<span>" + images.permUser().getHTML() + "&nbsp;Owner</span>");
					removeButton.setVisible(false);
				}
				else
					permTable.setHTML(i, 0, "<span>" + images.permUser().getHTML() + "&nbsp;"+dto.getUser()+"</span>");
			else if(dto.getGroup() != null)
				permTable.setHTML(i, 0, "<span>" + images.permGroup().getHTML() + "&nbsp;"+dto.getGroup()+"</span>");
			CheckBox read = new CheckBox();
			read.setChecked(dto.isRead());
			CheckBox write = new CheckBox();
			write.setChecked(dto.isWrite());
			CheckBox modify = new CheckBox();
			modify.setChecked(dto.isModifyACL());
			if (dto.getUser()!=null && dto.getUser().equals(owner)) {
				read.setEnabled(false);
				write.setEnabled(false);
				modify.setEnabled(false);
			}
			permTable.setWidget(i, 1, read);
			permTable.setWidget(i, 2, write);
			permTable.setWidget(i, 3, modify);
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
		for(PermissionHolder dto : permissions){
			/*if(dto.getId() == null)
				hasChanges =true;*/
			CheckBox r = (CheckBox) permTable.getWidget(i, 1);
			CheckBox w = (CheckBox) permTable.getWidget(i, 2);
			CheckBox m = (CheckBox) permTable.getWidget(i, 3);
			if(dto.isRead() != r.isChecked() || dto.isWrite() != w.isChecked() || dto.isModifyACL() != m.isChecked())
				hasChanges = true;
			dto.setRead(r.isChecked());
			dto.setWrite(w.isChecked());
			dto.setModifyACL(m.isChecked());
			i++;
		}
	}


	/**
	 * Retrieve the permissions.
	 *
	 * @return the permissions
	 */
	public Set<PermissionHolder> getPermissions() {
		return permissions;
	}

	public void addPermission(PermissionHolder permission){
		permissions.add(permission);
		hasAddition = true;
	}


}
