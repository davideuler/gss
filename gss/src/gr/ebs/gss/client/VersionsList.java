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
import gr.ebs.gss.client.rest.AbstractRestCommand;
import gr.ebs.gss.client.rest.ExecuteDelete;
import gr.ebs.gss.client.rest.ExecutePost;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.FileResource;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
		permPanel.addStyleName("gwt-TabPanelBottom");
		permTable.addStyleName("gss-permList");
		initWidget(permPanel);
		updateTable();
	}

	public void updateTable() {
		int i = 1;
		if (toRemove != null) {
			versions.remove(toRemove);
			toRemove = null;
		}
		for (final FileResource dto : versions) {
			HTML restoreVersion = new HTML("<a class='hidden-link info'>"+images.restore().getHTML()+"<span>Restore this Version</span></a>");
			restoreVersion.addClickListener( new ClickListener() {

				public void onClick(Widget sender) {
					restoreVersion(dto);
				}
			});

			permTable.setHTML(i, 0, "<span>" + dto.getVersion() + "</span>");
			permTable.setHTML(i, 1, "<span>" + formatDate(dto.getCreationDate()) + "</span>");
			permTable.setHTML(i, 2, "<span>" + formatDate(dto.getModificationDate()) + "</span>");
			permTable.setHTML(i, 3, "<span>" + dto.getFileSizeAsString() + "</span>");
			String[] link = {"", ""};
			createDownloadLink(link, dto);
			HTML downloadHtml = new HTML(link[0]+images.download().getHTML()+"<span>View This Version</span>"+link[1]);
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

	void createDownloadLink(String[] link, FileResource file) {
		String dateString = AbstractRestCommand.getDate();
		String resource = file.getUri().substring(GSS.GSS_REST_PATH.length()-1,file.getUri().length());
		String sig = GSS.get().getCurrentUserResource().getUsername()+" "+AbstractRestCommand.calculateSig("GET", dateString, resource, AbstractRestCommand.base64decode(GSS.get().getToken()));
		link[0] = "<a class='hidden-link info' href='" + file.getUri() + "&Authorization=" + URL.encodeComponent(sig) + "&Date="+URL.encodeComponent(dateString) + "' target='_blank'>";
		link[1] = "</a>";
	}

	void removeVersion(final FileResource version) {
		ExecuteDelete df = new ExecuteDelete(version.getUri()){

			@Override
			public void onComplete() {
				toRemove = version;
				updateTable();
				GSS.get().getFileList().updateFileCache(false, true /*clear selection*/);
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
						GSS.get().displayError("Unable to remove  version:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error removing version:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(df);

	}

	void restoreVersion(final FileResource version) {
		FileResource selectedFile = (FileResource) GSS.get().getCurrentSelection();
		ExecutePost ep = new ExecutePost(selectedFile.getUri()+"?restoreVersion="+version.getVersion(),"",200){


			@Override
			public void onComplete() {
				container.hide();
                GSS.get().getFileList().updateFileCache(true, true /*clear selection*/);
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

}
