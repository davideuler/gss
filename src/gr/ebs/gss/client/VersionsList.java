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
import gr.ebs.gss.client.rest.DeleteCommand;
import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.PostCommand;
import gr.ebs.gss.client.rest.RestCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;
import gr.ebs.gss.client.rest.resource.UserResource;
import gr.ebs.gss.client.rest.resource.UserSearchResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author kman
 */
public class VersionsList extends Composite {

	int selectedRow = -1;

	int permissionCount = -1;

	List<FileResource> versions = null;

	final Images images;

	final VerticalPanel permPanel = new VerticalPanel();

	final FlexTable permTable = new FlexTable();

	FileResource toRemove = null;

	FilePropertiesDialog container;

	public VersionsList(FilePropertiesDialog aContainer, final Images theImages, List<FileResource> theVersions) {
		images = theImages;
		container = aContainer;
		versions = theVersions;
		Collections.sort(theVersions, new Comparator<FileResource>(){

			@Override
			public int compare(FileResource o1, FileResource o2) {
				return o1.getVersion().compareTo(o2.getVersion());
			}

		});
		permTable.setText(0, 0, "Version");
		permTable.setText(0, 1, "Created");
		permTable.setText(0, 2, "Modified");
		permTable.setText(0, 3, "Size");
		permTable.setText(0, 4, "");
		permTable.setText(0, 5, "");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		permTable.getFlexCellFormatter().setColSpan(0, 1, 2);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
		permPanel.add(permTable);
		permPanel.addStyleName("gss-TabPanelBottom");
		permTable.addStyleName("gss-permList");
		initWidget(permPanel);
		updateTable();
	}

	public void updateTable() {
		copyListAndContinue(versions);		
	}
	
	public void showVersionsTable(){
		int i = 1;
		if (toRemove != null) {
			versions.remove(toRemove);
			toRemove = null;
		}
		for (final FileResource dto : versions) {
			HTML restoreVersion = new HTML("<a href='#' class='hidden-link info'><span>"+AbstractImagePrototype.create(images.restore()).getHTML()+"</span><div>Restore this Version</div></a>");
			restoreVersion.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					restoreVersion(dto);
				}
			});

			permTable.setHTML(i, 0, "<span>" + dto.getVersion() + "</span>");
			permTable.setHTML(i, 1, "<span>" + formatDate(dto.getCreationDate()) + " by " + GSS.get().findUserFullName(dto.getCreatedBy()) + "</span>");
			permTable.setHTML(i, 2, "<span>" + formatDate(dto.getModificationDate()) + " by " + GSS.get().findUserFullName(dto.getModifiedBy()) + "</span>");
			permTable.setHTML(i, 3, "<span>" + dto.getFileSizeAsString() + "</span>");
			HTML downloadHtml = new HTML("<a class='hidden-link info' href='#'><span>"+AbstractImagePrototype.create(images.download()).getHTML()+"</span><div>View this Version</div></a>");
			downloadHtml.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					GSS app = GSS.get();
					String dateString = RestCommand.getDate();
					String resource = dto.getUri().substring(app.getApiPath().length()-1, dto.getUri().length());
					String sig = app.getCurrentUserResource().getUsername()+" "+RestCommand.calculateSig("GET", dateString, resource, RestCommand.base64decode(app.getToken()));
					String fileUrl = dto.getUri() + "?version=" + dto.getVersion() + "&Authorization=" + URL.encodeComponent(sig) + "&Date="+URL.encodeComponent(dateString);
					Window.open(fileUrl, "_BLANK", "");
				}
			});
			permTable.setWidget(i, 4, downloadHtml);
			permTable.setWidget(i, 5, restoreVersion);
			permTable.getFlexCellFormatter().setStyleName(i, 0, "props-labels");
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setColSpan(i, 1, 2);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 3, HasHorizontalAlignment.ALIGN_CENTER);
			i++;
		}
		for (; i < permTable.getRowCount(); i++)
			permTable.removeRow(i);
	}

	void removeVersion(final FileResource version) {
		DeleteCommand df = new DeleteCommand(version.getUri()){

			@Override
			public void onComplete() {
				toRemove = version;
				updateTable();
				GSS.get().getTreeView().refreshCurrentNode(false);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("Versions does not exist");
					else
						GSS.get().displayError("Unable to remove version:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error removing version:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(df);

	}

	void restoreVersion(final FileResource version) {
		FileResource selectedFile = (FileResource) GSS.get().getCurrentSelection();
		PostCommand ep = new PostCommand(selectedFile.getUri()+"?restoreVersion="+version.getVersion(),"",200){


			@Override
			public void onComplete() {
				container.hide();
                GSS.get().getTreeView().refreshCurrentNode(false);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException)
					GSS.get().displayError("Unable to restore version:"+((RestException)t).getHttpStatusText());
				else
					GSS.get().displayError("System error restoring version:"+t.getMessage());
			}

		};
		DeferredCommand.addCommand(ep);
	}

	private String formatDate(Date date){
		DateTimeFormat format = DateTimeFormat.getFormat("dd/MM/yyyy : HH:mm");
		return format.format(date);
	}
	
	/**
	 * Copies the input List to a new List
	 * @param input
	 */
	private void copyListAndContinue(List<FileResource> input){
		List<FileResource> copiedInput = new ArrayList<FileResource>();		
		for(FileResource dto : input) {
			copiedInput.add(dto);
		}
		handleFullNames(copiedInput);
	}
	
	/**
	 * Examines whether or not the user's full name exists in the 
	 * userFullNameMap in the GSS.java for every element of the input list.
	 * If the user's full name does not exist in the map then a request is being made
	 * for the specific username.  
	 * 
	 * @param input
	 */
	private void handleFullNames(List<FileResource> input){		
		if(input.isEmpty()){
			showVersionsTable();
			return;
		}
		
		if(GSS.get().findUserFullName(input.get(0).getOwner()) == null){
			findFullNameAndUpdate(input);		
			return;
		}
				
		if(input.size() >= 1){
			input.remove(input.get(0));
			if(input.isEmpty()){
				showVersionsTable();			
			}else{
				handleFullNames(input);
			}
		}					
	}
	
	/**
	 * Makes a request to search for full name from a given username
	 * and continues checking the next element of the List.
	 *  
	 * @param input
	 */

	private void findFullNameAndUpdate(final List<FileResource> input){				
		final String aUserName = input.get(0).getOwner();
		String path = GSS.get().getApiPath() + "users/" + aUserName; 

		GetCommand<UserSearchResource> gg = new GetCommand<UserSearchResource>(UserSearchResource.class, path, false,null) {
			@Override
			public void onComplete() {
				final UserSearchResource result = getResult();
				for (UserResource user : result.getUsers()){
					String username = user.getUsername();
					String userFullName = user.getName();
					GSS.get().putUserToMap(username, userFullName);
					if(input.size() >= 1){
						input.remove(input.get(0));						
						if(input.isEmpty()){
							showVersionsTable();
							return;
						}
						handleFullNames(input);										
					}									
				}
			}
			@Override
			public void onError(Throwable t) {				
				GSS.get().displayError("Unable to fetch user's full name from the given username " + aUserName);
				if(input.size() >= 1){
					input.remove(input.get(0));
					handleFullNames(input);					
				}
			}
		};
		DeferredCommand.addCommand(gg);
	
	}

}
