/*
 * Copyright 2010 Electronic Business Systems Ltd.
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
package gr.ebs.gss.admin.client.ui;

import gr.ebs.gss.admin.client.TwoAdmin;
import gr.ebs.gss.common.dto.UserClassDTO;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author kman
 *
 */
public class UserClassListPanel extends Composite {

	private static UserClassListPanelUiBinder uiBinder = GWT.create(UserClassListPanelUiBinder.class);

	interface UserClassListPanelUiBinder extends UiBinder<Widget, UserClassListPanel> {
	}
	@UiField(provided=true)
	FlexTable userClassList;
	@UiField(provided=true)
	FlexTable userClassEdit;
	@UiField
	Button saveButton;
	@UiField
	Button newButton;
	TextBox nameBox = new TextBox();
	TextBox quotaBox = new TextBox();
	TextBox bandwithBox = new TextBox();
	UserClassDTO userClass;

	public UserClassListPanel() {
		userClassList = new FlexTable();
		userClassEdit = new FlexTable();
		userClassList.setText(0, 0, "Name");
		userClassList.setText(0, 1, "Quota");
		userClassList.setText(0, 2, "Bandwith Quota");
		userClassList.setText(0, 3, "");
		userClassList.setText(0, 4, "");
		userClassList.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		userClassList.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		userClassList.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		userClassList.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		userClassList.getFlexCellFormatter().setStyleName(0, 4, "props-toplabels");
		userClassList.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		userClassList.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		userClassList.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		userClassList.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
		userClassList.getFlexCellFormatter().setHorizontalAlignment(0, 4, HasHorizontalAlignment.ALIGN_CENTER);
		userClassList.addStyleName("gss-permList");



		userClassEdit.setText(0, 0, "Name");
		userClassEdit.setText(1, 0, "Quota");
		userClassEdit.setText(2, 0, "Bandwith Quota");
		userClassEdit.setText(3, 0, "");

		userClassEdit.setWidget(0, 1, nameBox);
		userClassEdit.setWidget(1, 1, quotaBox);
		userClassEdit.setWidget(2, 1, bandwithBox);
		userClassEdit.setText(3, 0, "");
		userClassList.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		userClassList.getFlexCellFormatter().setStyleName(1, 0, "props-toplabels");
		userClassList.getFlexCellFormatter().setStyleName(2, 0, "props-toplabels");
		userClassList.getFlexCellFormatter().setStyleName(3, 0, "props-toplabels");
		initWidget(uiBinder.createAndBindUi(this));


		updateTable();
	}

	public void updateTable(){
		clearInput();
		clearTable();
		TwoAdmin.get().getAdminService().getUserClasses(new AsyncCallback<List<UserClassDTO>>() {

			@Override
			public void onSuccess(List<UserClassDTO> result) {
				int i=1;
				for(final UserClassDTO dto : result){
					userClassList.setHTML(i, 0, "<span>" + dto.getName() + "</span>");
					userClassList.setHTML(i, 1, "<span>" + dto.getQuotaAsString() + "</span>");
					userClassList.setHTML(i, 2, "<span></span>"); // XXX
					HTML edit = new HTML("<a href='#'>Edit</a>");
					edit.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							userClass = dto;
							updateInput();
						}
					});
					userClassList.setWidget(i, 3, edit);
					HTML delete = new HTML("<a href='#'>Remove</a>");
					delete.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							clearInput();
							TwoAdmin.get().getAdminService().removeUserClass(dto, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									TwoAdmin.get().hideLoadingBox();
									GWT.log("Error deleting class", caught);
									TwoAdmin.get().showErrorBox("Unable to Delete User Class");

								}

								@Override
								public void onSuccess(Void removeResult) {
									updateTable();

								}});
						}
					});
					userClassList.setWidget(i, 4, delete);
					userClassList.getFlexCellFormatter().setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER);
					userClassList.getFlexCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
					userClassList.getFlexCellFormatter().setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_CENTER);
					userClassList.getFlexCellFormatter().setHorizontalAlignment(i, 3, HasHorizontalAlignment.ALIGN_CENTER);
					userClassList.getFlexCellFormatter().setHorizontalAlignment(i, 4, HasHorizontalAlignment.ALIGN_CENTER);
					i++;
				}

			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}
		});
	}
	public void clearTable(){
		int count = userClassList.getRowCount();
		for(int i=1;i<count;i++){
			userClassList.setHTML(i, 0, "<span></span>");
			userClassList.setHTML(i, 1, "<span></span>");
			userClassList.setHTML(i, 2, "<span></span>");
			userClassList.setHTML(i, 3, "<span></span>");
			userClassList.setHTML(i, 4, "<span></span>");
		}
	}
	public void clearInput(){
		userClass= new UserClassDTO();
		nameBox.setText("");
		quotaBox.setText("");
		bandwithBox.setText("");
	}

	public void updateInput(){
		nameBox.setText(userClass.getName());
		quotaBox.setText(String.valueOf(userClass.getQuota()));
		bandwithBox.setText(""); // XXX
	}

	@UiHandler("newButton")
	public void handleNew(@SuppressWarnings("unused") ClickEvent e){
		clearInput();
	}

	@UiHandler("saveButton")
	public void handleSave(@SuppressWarnings("unused") ClickEvent e){
		userClass.setName(nameBox.getText());
		userClass.setQuota(Long.parseLong(quotaBox.getText()));
		TwoAdmin.get().getAdminService().saveOrUpdateUserClass(userClass, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				TwoAdmin.get().hideLoadingBox();
				GWT.log("Error saving class", caught);
				TwoAdmin.get().showErrorBox("Unable to Save User Class");

			}

			@Override
			public void onSuccess(Void result) {
				updateTable();

			}});
	}


}
