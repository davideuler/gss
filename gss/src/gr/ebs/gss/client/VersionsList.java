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
import gr.ebs.gss.client.domain.FileBodyDTO;
import gr.ebs.gss.client.exceptions.RpcException;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

	List<FileBodyDTO> versions = null;

	private List<FileBodyDTO> newVersions = null;

	final Images images;

	final VerticalPanel permPanel = new VerticalPanel();

	final FlexTable permTable = new FlexTable();

	FileBodyDTO toRemove = null;

	FilePropertiesDialog container;

	public VersionsList(FilePropertiesDialog container, final Images images, List<FileBodyDTO> versions) {
		this.images = images;
		this.container = container;
		this.versions = versions;
		permTable.setText(0, 0, "Version");
		permTable.setText(0, 1, "Created");
		permTable.setText(0, 2, "Modified");
		permTable.setText(0, 3, "Size");
		permTable.setText(0, 4, "");
		permTable.setText(0, 5, "");
		//permTable.setText(0, 6, "");
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
		for (final FileBodyDTO dto : versions) {
			HTML restoreVersion = new HTML("<a class='hidden-link info'>"+images.restore().getHTML()+"<span>Restore this Version</span></a>");
			restoreVersion.addClickListener( new ClickListener() {

				public void onClick(Widget sender) {
					restoreVersion(dto);
				}
			});

			permTable.setHTML(i, 0, "<span>" + dto.getVersion() + "</span>");
			permTable.setHTML(i, 1, "<span>" + formatDate(dto.getAuditInfo().getCreationDate()) + "</span>");
			permTable.setHTML(i, 2, "<span>" + formatDate(dto.getAuditInfo().getModificationDate()) + "</span>");
			permTable.setHTML(i, 3, "<span>" + dto.getFileSizeAsString() + "</span>");
			String[] link = {"", ""};
			createDownloadLink(link, dto);
			HTML downloadHtml = new HTML(link[0]+images.download().getHTML()+"<span>View This Version</span>"+link[1]);
			permTable.setWidget(i, 4, downloadHtml);
			permTable.setWidget(i, 5, restoreVersion);
			//permTable.setWidget(i, 6, removeButton);
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

	void createDownloadLink(String[] link, FileBodyDTO dto) {
		link[0] = "<a class='hidden-link info' href='" + FileMenu.FILE_DOWNLOAD_PATH + "?userId=" + GSS.get().getCurrentUser().getId().toString() + "&fileId=" + dto.getFileHeaderId() +  "&bodyId=" + dto.getId() + "' target='_blank'>";
		link[1] = "</a>";
	}

	void removeVersion(final FileBodyDTO version) {
		GSS.get().getRemoteService().removeVersion(GSS.get().getCurrentUser().getId(), version.getFileHeaderId(), version.getId(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				toRemove = version;
				updateTable();
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});

	}

	void restoreVersion(final FileBodyDTO version) {
		GSS.get().getRemoteService().restoreVersion(GSS.get().getCurrentUser().getId(), version.getFileHeaderId(), version.getId(), new AsyncCallback() {

			public void onSuccess(final Object result) {
				GWT.log("RESTORE CALLED", null);
				container.hide();
				GSS.get().getFileList().updateFileCache(GSS.get().getCurrentUser().getId());
			}

			public void onFailure(final Throwable caught) {
				GWT.log("", caught);
				if (caught instanceof RpcException)
					GSS.get().displayError("An error occurred while " + "communicating with the server: " + caught.getMessage());
				else
					GSS.get().displayError(caught.getMessage());
			}
		});

	}

	private String formatDate(Date date){
		DateTimeFormat format = DateTimeFormat.getFormat("dd/MM/yyyy : HH:mm");
		return format.format(date);
	}

}
