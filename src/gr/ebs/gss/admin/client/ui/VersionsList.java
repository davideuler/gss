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
import gr.ebs.gss.common.dto.FileBodyDTO;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * @author kman
 *
 */
public class VersionsList extends Composite{


	final FlexTable permTable = new FlexTable();
	final VerticalPanel permPanel = new VerticalPanel();

	/**
	 *
	 */
	public VersionsList(List<FileBodyDTO> versions) {

		permTable.setText(0, 0, "Version");
		permTable.setText(0, 1, "Created By");
		permTable.setText(0, 2, "Created");
		permTable.setText(0, 3, "Modified By");
		permTable.setText(0, 4, "Modified");
		permTable.setText(0, 5, "Size");
		permTable.setText(0, 6, "");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 4, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 5, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 6, "props-toplabels");
		permTable.getFlexCellFormatter().setColSpan(0, 1, 2);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 4, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 5, HasHorizontalAlignment.ALIGN_CENTER);
		permPanel.add(permTable);
		permPanel.addStyleName("gss-TabPanelBottom");
		permTable.addStyleName("gss-permList");
		initWidget(permPanel);
		updateTable(versions);
	}

	public void updateTable(List<FileBodyDTO> versions) {
		Collections.sort(versions, new Comparator<FileBodyDTO>(){

			@Override
			public int compare(FileBodyDTO o1, FileBodyDTO o2) {
				return new Integer(o1.getVersion()).compareTo(new Integer(o2.getVersion()));
			}

		});
		int i = 1;
		for (final FileBodyDTO dto : versions) {

			permTable.setHTML(i, 0, "<span>" + dto.getVersion() + "</span>");
			HTML createdByLabel = new HTML("<a href='#'>"+dto.getAuditInfo().getCreatedBy().getUsername()+"</a></span>");
			permTable.setWidget(i, 1, createdByLabel);
			createdByLabel.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					TwoAdmin.get().searchUsers("username:"+dto.getAuditInfo().getCreatedBy().getUsername());

				}
			});
			permTable.setHTML(i, 2, "<span>" + formatLocalDateTime(dto.getAuditInfo().getCreationDate()) + "</span>");
			HTML modifiedByLabel = new HTML("<a href='#'>"+dto.getAuditInfo().getModifiedBy().getUsername()+"</a></span>");
			permTable.setWidget(i, 3, modifiedByLabel);
			modifiedByLabel.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					TwoAdmin.get().searchUsers("username:"+dto.getAuditInfo().getModifiedBy().getUsername());

				}
			});

			permTable.setHTML(i, 4, "<span>" + formatLocalDateTime(dto.getAuditInfo().getModificationDate()) + "</span>");
			permTable.setHTML(i, 5, "<span>" + dto.getFileSizeAsString() + "</span>");
			HTML downloadHtml = new HTML("<a class='hidden-link info' href='#'><span>"+"</span><div>View this Version</div></a>");
			downloadHtml.addClickHandler( new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {


				}
			});
			permTable.setWidget(i, 6, downloadHtml);

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

	public static String formatLocalDateTime(Date date) {
		Date convertedDate = new Date(date.getTime() - date.getTimezoneOffset());
		final DateTimeFormat dateFormatter = DateTimeFormat.getShortDateFormat();
		final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm");
		String datePart = dateFormatter.format(convertedDate);
		String timePart = timeFormatter.format(convertedDate);
		return datePart + " " + timePart;
	}

}
