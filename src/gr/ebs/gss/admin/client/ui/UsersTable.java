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

import gr.ebs.gss.admin.client.ui.UsersTable.UserSorter.UserComparator;
import gr.ebs.gss.server.domain.dto.StatsDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.AbstractScrollTable.ScrollPolicy;
import com.google.gwt.gen2.table.client.AbstractScrollTable.SortPolicy;
import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultRowRenderer;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.FixedWidthGridBulkRenderer;
import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.PagingOptions;
import com.google.gwt.gen2.table.client.PagingScrollTable;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;
import com.google.gwt.gen2.table.client.TableDefinition;
import com.google.gwt.gen2.table.client.TableDefinition.AbstractCellView;
import com.google.gwt.gen2.table.client.TableModel;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.gen2.table.client.TableModelHelper.Response;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.gen2.table.event.client.TableEvent.Row;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * @author kman
 *
 */
public class UsersTable extends Composite {
	private CachedTableModel<UserDTO> cachedTableModel = null;
	private DefaultTableDefinition<UserDTO> tableDefinition = null;
	private PagingScrollTable<UserDTO> pagingScrollTable = null;
	private Label countLabel = new Label("There are no users to display.");
	private DataSourceTableModel tableModel = null;

	private VerticalPanel vPanel = new VerticalPanel();
	private FlexTable flexTable = new FlexTable();

	/**
	 * Constructor
	 */
	public UsersTable() {
		super();
		pagingScrollTable = createScrollTable();
		pagingScrollTable.setHeight("200px");
		pagingScrollTable.setScrollPolicy(ScrollPolicy.DISABLED);
		PagingOptions pagingOptions = new PagingOptions(pagingScrollTable);

		flexTable.setWidget(0, 0, pagingScrollTable);
		flexTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		flexTable.setWidget(1, 0, pagingOptions);

		countLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.add(countLabel);
		vPanel.add(flexTable);

		vPanel.setWidth("100%");
		flexTable.setWidth("100%");

		super.initWidget(vPanel);
		showUsers(new ArrayList<UserDTO>());
		pagingScrollTable.setFooterGenerated(true);
	}

	/**
	 *
	 * @param newList the list of users to show
	 */
	public void showUsers(List<UserDTO> newList) {
		countLabel.setText("There are "+ newList.size() + " users.");
		tableModel.setData(newList);
		tableModel.setRowCount(newList.size());
		cachedTableModel.clearCache();
		cachedTableModel.setRowCount(newList.size());

		pagingScrollTable.gotoPage(0, true);
		if(newList.size()<2)
			flexTable.setWidget(1, 0, new HTML());
		else{
			PagingOptions pagingOptions = new PagingOptions(pagingScrollTable);
			flexTable.setWidget(1, 0, pagingOptions);
		}
	}

	public UserDTO getUserOnRow(int rowIdx){
		String id = pagingScrollTable.getDataTable().getHTML(rowIdx, 0);
		final UserDTO m = tableModel.getUserById(Long.parseLong(id));
		return m;
	}


	public UserDTO getSelectedRowObject(RowSelectionEvent event){
		Set<Row> set = event.getSelectedRows();
		if(set.size() == 1) {
			int rowIdx = set.iterator().next().getRowIndex();
			String id = pagingScrollTable.getDataTable().getHTML(rowIdx, 0);
			UserDTO m = tableModel.getUserById(Long.parseLong(id));
			return m;
		}
		return null;
	}

	public UserDTO getUser(Long id){

		return tableModel.getUserById(id);
	}

	public DialogBox createDialogBox(UserDTO m, StatsDTO s) {

		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setHTML("User Details: "+m.getUsername());

		// Create a table to layout the content
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogBox.setSize("50%", "50%");

		ClickHandler cancelHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		};

		dialogBox.setWidget(dialogVPanel);
		dialogVPanel.add(new Label("Username: "+m.getUsername()));
		dialogVPanel.add(new Label("Email: "+m.getEmail()));
		dialogVPanel.add(new Label("Name: "+m.getName()));
		if(m.getUserClass()!=null)
			dialogVPanel.add(new Label("Quota: "+m.getUserClass().getQuotaAsString()));
		dialogVPanel.add(new Label("File Count: "+s.getFileCount()));
		dialogVPanel.add(new Label("File Size: "+s.getFileSizeAsString()));
		dialogVPanel.add(new Label("Quota Left: "+s.getQuotaLeftAsString()));

		Button close = new Button("Close");
		close.addClickHandler(cancelHandler);
		dialogVPanel.add(close);

		// Return the dialog box
		return dialogBox;
	}

	/**
	 * Initializes the scroll table
	 * @return
	 */
	private PagingScrollTable<UserDTO> createScrollTable() {
		// create our own table model
		tableModel = new DataSourceTableModel();
		// add it to cached table model
		cachedTableModel = createCachedTableModel(tableModel);

		// create the table definition
		TableDefinition<UserDTO> tableDef = createTableDefinition();

		// create the paging scroll table
		pagingScrollTable = new PagingScrollTable<UserDTO>(cachedTableModel, tableDef);
		pagingScrollTable.setPageSize(10);
		pagingScrollTable.setEmptyTableWidget(new HTML("There is no data to display"));
		pagingScrollTable.getDataTable().setSelectionPolicy(SelectionPolicy.ONE_ROW);

		FixedWidthGridBulkRenderer<UserDTO> bulkRenderer = new FixedWidthGridBulkRenderer<UserDTO>(pagingScrollTable.getDataTable(), pagingScrollTable);
		pagingScrollTable.setBulkRenderer(bulkRenderer);


		pagingScrollTable.setCellPadding(3);
		pagingScrollTable.setCellSpacing(0);
		pagingScrollTable.setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH);

		pagingScrollTable.setSortPolicy(SortPolicy.SINGLE_CELL);

		return pagingScrollTable;
	}

	public void addRowSelectionHandler(RowSelectionHandler handler){
		pagingScrollTable.getDataTable().addRowSelectionHandler(handler);
	}

	/**
	 * Create the {@link CachedTableModel}
	 * @param aTableModel
	 * @return
	 */
	private CachedTableModel<UserDTO> createCachedTableModel(DataSourceTableModel aTableModel) {
		CachedTableModel<UserDTO> tm = new CachedTableModel<UserDTO>(aTableModel);
		tm.setPreCachedRowCount(20);
		tm.setPostCachedRowCount(20);
		tm.setRowCount(20);
		return tm;
	}

	private DefaultTableDefinition<UserDTO> createTableDefinition() {
		tableDefinition = new DefaultTableDefinition<UserDTO>();

		final String[] rowColors = new String[] { "#FFFFDD", "EEEEEE" };
		tableDefinition.setRowRenderer(new DefaultRowRenderer<UserDTO>(rowColors));

		// id
		{
			IdColumnDefinition columnDef = new IdColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(false);
			columnDef.setPreferredColumnWidth(35);
			columnDef.setHeader(0, new HTML("Id"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		{
			UserClassColumnDefinition columnDef = new UserClassColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("User Class"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		{
			ActiveColumnDefinition columnDef = new ActiveColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Active"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			columnDef.setCellRenderer(new CellRenderer<UserDTO, Boolean>() {

				@Override
				public void renderRowValue(UserDTO rowValue, ColumnDefinition<UserDTO, Boolean> aColumnDef, AbstractCellView<UserDTO> view) {
					CheckBox check = new CheckBox();
					check.setValue(aColumnDef.getCellValue(rowValue));
					check.setEnabled(false);
					view.setWidget(check);

				}
			});
			tableDefinition.addColumnDefinition(columnDef);
		}
		// username
		{
			UsernameColumnDefinition columnDef = new UsernameColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Username"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		{
			EmailColumnDefinition columnDef = new EmailColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Email"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		{
			FullNameColumnDefinition columnDef = new FullNameColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Name"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}

		{
			LastLoginColumnDefinition columnDef = new LastLoginColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Last Login"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}



		return tableDefinition;
	}


	private class DataSourceTableModel extends MutableTableModel<UserDTO> {
		private Map<Long, UserDTO> map;
		private UserSorter sorter = new UserSorter();
		public void setData(List<UserDTO> list) {
			// toss the list, index by id in a map.
			map = new HashMap<Long, UserDTO>(list.size());
			for(UserDTO m : list)
				map.put(m.getId(), m);
		}

		public UserDTO getUserById(long id) {
			return map.get(id);
		}

		@Override
		protected boolean onRowInserted(int beforeRow) {
			return true;
		}

		@Override
		protected boolean onRowRemoved(int row) {
			return true;
		}

		@Override
		protected boolean onSetRowValue(int row, UserDTO rowValue) {

			return true;
		}

		@Override
		public void requestRows(
				final Request request,
				TableModel.Callback<UserDTO> callback) {

			callback.onRowsReady(request, new Response<UserDTO>(){

				@Override
				public Iterator<UserDTO> getRowValues() {
					final int col = request.getColumnSortList().getPrimaryColumn();
					final boolean ascending = request.getColumnSortList().isPrimaryAscending();
					if(col < 0)
						map = sorter.sort(map, new UserComparator(ascending,0));
					else
						map = sorter.sort(map, new UserComparator(ascending,col));
					return map.values().iterator();
				}});
		}

	}


	private final class IdColumnDefinition extends AbstractColumnDefinition<UserDTO, Long> {
		@Override
		public Long getCellValue(UserDTO rowValue) {
			return rowValue.getId();
		}
		@Override
		public void setCellValue(UserDTO rowValue, Long cellValue) { }
	}


	private final class UsernameColumnDefinition extends
			AbstractColumnDefinition<UserDTO, String> {
		@Override
		public String getCellValue(final UserDTO rowValue) {
			return rowValue.getUsername();
		}

		@Override
		public void setCellValue(final UserDTO rowValue, final String cellValue) {}
	}


	private final class FullNameColumnDefinition extends
			AbstractColumnDefinition<UserDTO, String> {
		@Override
		public String getCellValue(final UserDTO rowValue) {
			return rowValue.getName();
		}

		@Override
		public void setCellValue(final UserDTO rowValue, final String cellValue) {}
	}


	private final class EmailColumnDefinition extends
			AbstractColumnDefinition<UserDTO, String> {
		@Override
		public String getCellValue(final UserDTO rowValue) {
			return rowValue.getEmail();
		}

		@Override
		public void setCellValue(final UserDTO rowValue, final String cellValue) {}
	}


	private final class UserClassColumnDefinition extends
			AbstractColumnDefinition<UserDTO, String> {
		@Override
		public String getCellValue(final UserDTO rowValue) {
			if(rowValue.getUserClass() != null)
				return rowValue.getUserClass().getName();
			return "";
		}

		@Override
		public void setCellValue(final UserDTO rowValue, final String cellValue) {}
	}


	private final class ActiveColumnDefinition extends
			AbstractColumnDefinition<UserDTO, Boolean> {
		@Override
		public Boolean getCellValue(final UserDTO rowValue) {
			if(rowValue.isActive() == null)
				return true;
			return rowValue.isActive();
		}

		@Override
		public void setCellValue(final UserDTO rowValue, final Boolean cellValue) {
			rowValue.setActive(cellValue);
		}
	}


	private final class LastLoginColumnDefinition extends AbstractColumnDefinition<UserDTO, String> {

		@Override
		public String getCellValue(final UserDTO rowValue) {
			if(rowValue.getCurrentLoginDate()==null)
				return  "no data";
			return DateTimeFormat.getFormat("dd/MM/yyyy hh:mm:ss tt").format(rowValue.getCurrentLoginDate());
		}

		@Override
		public void setCellValue(final UserDTO rowValue, final String cellValue) {}
	}



	public static class UserSorter {


		public Map<Long, UserDTO> sort(Map<Long, UserDTO> map, Comparator<UserDTO> comparator) {
			final List<UserDTO> list = new LinkedList<UserDTO>(map.values());
			Collections.sort(list, comparator);
			Map<Long, UserDTO> result = new LinkedHashMap<Long, UserDTO>(list.size());
			for(UserDTO p : list)
				result.put(p.getId(), p);
			return result;
		}

		public final static class UserComparator implements Comparator<UserDTO> {

			private final boolean ascending;
			private int property;
			public UserComparator(boolean isAscending, int aProperty) {
				ascending = isAscending;
				property = aProperty;
			}

			@Override
			public int compare(UserDTO m1, UserDTO m2) {
				switch(property){
					case 0:
						if(ascending)
							return m1.getId().compareTo(m2.getId());
						return m2.getId().compareTo(m1.getId());
					case 1:
						if(ascending)
							return m1.getUserClass().getName().compareTo(m2.getUserClass().getName());
						return m2.getUserClass().getName().compareTo(m1.getUserClass().getName());
					case 2:
						if(ascending)
							return m1.isActive().compareTo(m2.isActive());
						return m2.isActive().compareTo(m1.isActive());
					case 3:
						if(ascending)
							return m1.getUsername().compareTo(m2.getUsername());
						return m2.getUsername().compareTo(m1.getUsername());
					case 4:
						if(ascending)
							return m1.getEmail().compareTo(m2.getEmail());
						return m2.getEmail().compareTo(m1.getEmail());
					case 5:
						if(ascending)
							return m1.getName().compareTo(m2.getName());
						return m2.getName().compareTo(m1.getName());
					case 6:
						return new DateComparator(ascending).compare(m2, m1);
					default:

				}
				String s1 = m1.getUsername();
				String s2 = m2.getUsername();
				if(ascending)
					return s1.compareTo(s2);
				return s2.compareTo(s1);
			}
		}


		public final static class DateComparator implements Comparator<UserDTO> {

			private final boolean ascending;

			public DateComparator(boolean isAscending) {
				ascending = isAscending;
			}

			@Override
			public int compare(UserDTO m1, UserDTO m2) {
				final Date d1 = m1.getLastLoginDate();
				final Date d2 = m2.getLastLoginDate();
				if(d1==null && d2==null)
					return 0;
				if(d1==null && d2 != null)
					return -1;
				if(d2==null && d1 != null)
					return 1;
				if(ascending)
					return d1.compareTo(d2);
				return d2.compareTo(d1);
			}
		}
	}



}
