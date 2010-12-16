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

import gr.ebs.gss.admin.client.ui.FilesTable.FileSorter.FileComparator;
import gr.ebs.gss.server.domain.dto.FileHeaderDTO;

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

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * @author kman
 *
 */
public class FilesTable extends Composite {
	private CachedTableModel<FileHeaderDTO> cachedTableModel = null;
	private DefaultTableDefinition<FileHeaderDTO> tableDefinition = null;
	private PagingScrollTable<FileHeaderDTO> pagingScrollTable = null;
	private Label countLabel = new Label("There are no files to display.");
	private DataSourceTableModel tableModel = null;

	private VerticalPanel vPanel = new VerticalPanel();
	private FlexTable flexTable = new FlexTable();

	/**
	 * Constructor
	 */
	public FilesTable() {
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
		showUsers(new ArrayList<FileHeaderDTO>());
		pagingScrollTable.setFooterGenerated(true);
	}

	public void showUsers(List<FileHeaderDTO> newList) {
		countLabel.setText("There are "+ newList.size() + " files.");
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

	public FileHeaderDTO getUserOnRow(int rowIdx){
		String id = pagingScrollTable.getDataTable().getHTML(rowIdx, 0);
		final FileHeaderDTO m = tableModel.getUserById(Long.parseLong(id));
		return m;
	}


	public FileHeaderDTO getSelectedRowObject(RowSelectionEvent event){
		Set<Row> set = event.getSelectedRows();
		if(set.size() == 1) {
			int rowIdx = set.iterator().next().getRowIndex();
			String id = pagingScrollTable.getDataTable().getHTML(rowIdx, 0);
			FileHeaderDTO m = tableModel.getUserById(Long.parseLong(id));
			return m;
		}
		return null;
	}

	public FileHeaderDTO getUser(Long id){

		return tableModel.getUserById(id);
	}



	/**
	 * Initializes the scroll table
	 * @return
	 */
	private PagingScrollTable<FileHeaderDTO> createScrollTable() {
		// create our own table model
		tableModel = new DataSourceTableModel();
		// add it to cached table model
		cachedTableModel = createCachedTableModel(tableModel);

		// create the table definition
		TableDefinition<FileHeaderDTO> tableDef = createTableDefinition();

		// create the paging scroll table
		pagingScrollTable = new PagingScrollTable<FileHeaderDTO>(cachedTableModel, tableDef);
		pagingScrollTable.setPageSize(10);
		pagingScrollTable.setEmptyTableWidget(new HTML("There is no data to display"));
		pagingScrollTable.getDataTable().setSelectionPolicy(SelectionPolicy.ONE_ROW);

		FixedWidthGridBulkRenderer<FileHeaderDTO> bulkRenderer = new FixedWidthGridBulkRenderer<FileHeaderDTO>(pagingScrollTable.getDataTable(), pagingScrollTable);
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
	private CachedTableModel<FileHeaderDTO> createCachedTableModel(DataSourceTableModel aTableModel) {
		CachedTableModel<FileHeaderDTO> tm = new CachedTableModel<FileHeaderDTO>(aTableModel);
		tm.setPreCachedRowCount(20);
		tm.setPostCachedRowCount(20);
		tm.setRowCount(20);
		return tm;
	}

	private DefaultTableDefinition<FileHeaderDTO> createTableDefinition() {
		tableDefinition = new DefaultTableDefinition<FileHeaderDTO>();

		final String[] rowColors = new String[] { "#FFFFDD", "EEEEEE" };
		tableDefinition.setRowRenderer(new DefaultRowRenderer<FileHeaderDTO>(rowColors));

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
			NameColumnDefinition columnDef = new NameColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("File Name"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		{
			UriColumnDefinition columnDef = new UriColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("URI"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		// username
		{
			UserColumnDefinition columnDef = new UserColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Username"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		{
			FilesizeDefinition columnDef = new FilesizeDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("File Size"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		{
			DeletedColumnDefinition columnDef = new DeletedColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Deleted"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			columnDef.setCellRenderer(new CellRenderer<FileHeaderDTO, Boolean>() {

				@Override
				public void renderRowValue(FileHeaderDTO rowValue, ColumnDefinition<FileHeaderDTO, Boolean> aColumnDef, AbstractCellView<FileHeaderDTO> view) {
					CheckBox check = new CheckBox();
					check.setValue(aColumnDef.getCellValue(rowValue));
					check.setEnabled(false);
					view.setWidget(check);

				}
			});
			tableDefinition.addColumnDefinition(columnDef);
		}


		{
			CreationColumnDefinition columnDef = new CreationColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Creation Date"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}

		{
			LastModifiedColumnDefinition columnDef = new LastModifiedColumnDefinition();
			columnDef.setColumnSortable(true);
			columnDef.setColumnTruncatable(true);
			columnDef.setHeader(0, new HTML("Modification Date"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}



		return tableDefinition;
	}


	private class DataSourceTableModel extends MutableTableModel<FileHeaderDTO> {
		private Map<Long, FileHeaderDTO> map;
		private FileSorter sorter = new FileSorter();
		public void setData(List<FileHeaderDTO> list) {
			// toss the list, index by id in a map.
			map = new HashMap<Long, FileHeaderDTO>(list.size());
			for(FileHeaderDTO m : list)
				map.put(m.getId(), m);
		}

		public FileHeaderDTO getUserById(long id) {
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
		protected boolean onSetRowValue(int row, FileHeaderDTO rowValue) {

			return true;
		}

		@Override
		public void requestRows(
				final Request request,
				TableModel.Callback<FileHeaderDTO> callback) {

			callback.onRowsReady(request, new Response<FileHeaderDTO>(){

				@Override
				public Iterator<FileHeaderDTO> getRowValues() {
					final int col = request.getColumnSortList().getPrimaryColumn();
					final boolean ascending = request.getColumnSortList().isPrimaryAscending();
					if(col < 0)
						map = sorter.sort(map, new FileComparator(ascending,0));
					else
						map = sorter.sort(map, new FileComparator(ascending,col));
					return map.values().iterator();
				}});
		}

	}


	private final class IdColumnDefinition extends AbstractColumnDefinition<FileHeaderDTO, Long> {
		@Override
		public Long getCellValue(FileHeaderDTO rowValue) {
			return rowValue.getId();
		}
		@Override
		public void setCellValue(FileHeaderDTO rowValue, Long cellValue) { }
	}


	private final class NameColumnDefinition extends
			AbstractColumnDefinition<FileHeaderDTO, String> {
		@Override
		public String getCellValue(final FileHeaderDTO rowValue) {
			return rowValue.getName();
		}

		@Override
		public void setCellValue(final FileHeaderDTO rowValue, final String cellValue) {}
	}


	private final class UriColumnDefinition extends
		AbstractColumnDefinition<FileHeaderDTO, String> {
		@Override
		public String getCellValue(final FileHeaderDTO rowValue) {
			return rowValue.getURI();
		}

		@Override
		public void setCellValue(final FileHeaderDTO rowValue, final String cellValue) {}
	}


	private final class FilesizeDefinition extends
			AbstractColumnDefinition<FileHeaderDTO, String> {
		@Override
		public String getCellValue(final FileHeaderDTO rowValue) {
			return rowValue.getFileSizeAsString();
		}

		@Override
		public void setCellValue(final FileHeaderDTO rowValue, final String cellValue) {}
	}


	private final class UserColumnDefinition extends
			AbstractColumnDefinition<FileHeaderDTO, String> {
		@Override
		public String getCellValue(final FileHeaderDTO rowValue) {
			return rowValue.getOwner().getUsername();
		}

		@Override
		public void setCellValue(final FileHeaderDTO rowValue, final String cellValue) {}
	}


	private final class DeletedColumnDefinition extends
			AbstractColumnDefinition<FileHeaderDTO, Boolean> {
		@Override
		public Boolean getCellValue(final FileHeaderDTO rowValue) {
			return rowValue.isDeleted();
		}

		@Override
		public void setCellValue(final FileHeaderDTO rowValue, final Boolean cellValue) {

		}
	}

	private final class CreationColumnDefinition extends AbstractColumnDefinition<FileHeaderDTO, String> {

		@Override
		public String getCellValue(final FileHeaderDTO rowValue) {
			return DateTimeFormat.getFormat("dd/MM/yyyy hh:mm:ss tt").format(rowValue.getAuditInfo().getCreationDate());
		}

		@Override
		public void setCellValue(final FileHeaderDTO rowValue, final String cellValue) {}
	}

	private final class LastModifiedColumnDefinition extends AbstractColumnDefinition<FileHeaderDTO, String> {

		@Override
		public String getCellValue(final FileHeaderDTO rowValue) {
			return DateTimeFormat.getFormat("dd/MM/yyyy hh:mm:ss tt").format(rowValue.getAuditInfo().getModificationDate());
		}

		@Override
		public void setCellValue(final FileHeaderDTO rowValue, final String cellValue) {}
	}



	public static class FileSorter {


		public Map<Long, FileHeaderDTO> sort(Map<Long, FileHeaderDTO> map, Comparator<FileHeaderDTO> comparator) {
			final List<FileHeaderDTO> list = new LinkedList<FileHeaderDTO>(map.values());
			Collections.sort(list, comparator);
			Map<Long, FileHeaderDTO> result = new LinkedHashMap<Long, FileHeaderDTO>(list.size());
			for(FileHeaderDTO p : list)
				result.put(p.getId(), p);
			return result;
		}

		public final static class FileComparator implements Comparator<FileHeaderDTO> {

			private final boolean ascending;
			private int property;
			public FileComparator(boolean isAscending, int aProperty) {
				ascending = isAscending;
				property = aProperty;
			}

			@Override
			public int compare(FileHeaderDTO m1, FileHeaderDTO m2) {
				GWT.log("sorting:"+property+" "+m1.getFileSize()+" "+m2.getFileSize());
				switch(property){
					case 0://id
						if(ascending)
							return m1.getId().compareTo(m2.getId());
						return m2.getId().compareTo(m1.getId());
					case 1://name
						if(ascending)
							return m1.getName().compareTo(m2.getName());
						return m2.getName().compareTo(m1.getName());
					case 2://uri
						if(ascending)
							return m1.getURI().compareTo(m2.getURI());
						return m2.getURI().compareTo(m1.getURI());
					case 3://file size
						if(ascending)
							return new Long(m1.getFileSize()).compareTo(new Long(m2.getFileSize()));
						return new Long(m2.getFileSize()).compareTo(new Long(m1.getFileSize()));
					case 4://deleted
						if(ascending)
							return new Boolean(m1.isDeleted()).compareTo(new Boolean(m2.isDeleted()));
						return new Boolean(m2.isDeleted()).compareTo(new Boolean(m1.isDeleted()));
					case 5://created
							return new DateComparator(ascending).compare(m1.getAuditInfo().getCreationDate(), m2.getAuditInfo().getCreationDate());
					case 6://modified
						return new DateComparator(ascending).compare(m1.getAuditInfo().getModificationDate(), m2.getAuditInfo().getModificationDate());
					default:
						if(ascending)
							return m1.getId().compareTo(m2.getId());
						return m2.getId().compareTo(m1.getId());
				}

			}
		}


		public final static class DateComparator implements Comparator<Date> {

			private final boolean ascending;

			public DateComparator(boolean isAscending) {
				ascending = isAscending;
			}

			@Override
			public int compare(Date d1, Date d2) {
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
