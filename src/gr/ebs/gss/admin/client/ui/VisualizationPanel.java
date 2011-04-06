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

import gr.ebs.gss.common.dto.SystemStatsDTO;
import gr.ebs.gss.common.dto.UserClassDTO;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.PieChart;


/**
 * @author kman
 *
 */
public class VisualizationPanel extends Composite{

	PieChart userCountChart;
	PieChart fileCountChart;
	PieChart fileSizeChart;

	PieChart lastWeekChart;
	PieChart lastMonthChart;
	PieChart bandwithChart;

	/**
	 *
	 */
	public VisualizationPanel() {
		final VerticalPanel vp = new VerticalPanel();
	    vp.getElement().getStyle().setPropertyPx("margin", 15);
	    userCountChart = new PieChart();
	    userCountChart.addStyleName("stats");
	    fileCountChart = new PieChart();
	    fileCountChart.addStyleName("stats");
	    fileSizeChart = new PieChart();
	    fileSizeChart.addStyleName("stats");

	    lastWeekChart = new PieChart();
	    lastWeekChart.addStyleName("stats");
	    lastMonthChart = new PieChart();
	    lastMonthChart.addStyleName("stats");
	    bandwithChart = new PieChart();
	    bandwithChart.addStyleName("stats");
	    HorizontalPanel row1 = new HorizontalPanel();
	    HorizontalPanel row2 = new HorizontalPanel();

	    row1.add(userCountChart);
	    row1.add(fileCountChart);
	    row1.add(fileSizeChart);

	    row2.add(lastWeekChart);
	    row2.add(lastMonthChart);
	    row2.add(bandwithChart);
	    vp.add(row1);
	    vp.add(row2);
	    initWidget(vp);
	  }

	public void updateData(SystemStatsDTO stats){
		DataTable data = DataTable.create();
	    data.addColumn(ColumnType.STRING, "UserClass");
	    data.addColumn(ColumnType.NUMBER, "UserCount");
	    data.addRows(stats.getUserClasses().size());
	    for(int i=0;i<stats.getUserClasses().size();i++){
	    	UserClassDTO dto = stats.getUserClasses().get(i);
	    	data.setValue(i, 0, dto.getName()+":"+dto.getStatistics().getUserCount()+ " users");
		    data.setValue(i, 1, dto.getStatistics().getUserCount());
	    }

	    PieChart.Options options = PieChart.Options.create();
	    options.setWidth(350);
	    options.setHeight(240);
	    options.set3D(true);
	    options.setColors("green","red","blue");
	    options.setTitle("User Count:"+stats.getUserCount()+ " users");
	    userCountChart.draw(data, options);

	    data = DataTable.create();
	    data.addColumn(ColumnType.STRING, "UserClass");
	    data.addColumn(ColumnType.NUMBER, "FileCount");
	    data.addRows(stats.getUserClasses().size());
	    for(int i=0;i<stats.getUserClasses().size();i++){
	    	UserClassDTO dto = stats.getUserClasses().get(i);
	    	data.setValue(i, 0, dto.getName()+":"+dto.getStatistics().getFileCount()+" files");
		    data.setValue(i, 1, dto.getStatistics().getFileCount());
	    }

	    options = PieChart.Options.create();
	    options.setWidth(350);
	    options.setHeight(240);
	    options.set3D(true);
	    options.setColors("red","green","blue");
	    options.setTitle("File Count:"+stats.getFileCount()+" files");
	    fileCountChart.draw(data, options);

	    data = DataTable.create();
	    data.addColumn(ColumnType.STRING, "UserClass");
	    data.addColumn(ColumnType.NUMBER, "FileSize");
	    data.addRows(stats.getUserClasses().size());
	    for(int i=0;i<stats.getUserClasses().size();i++){
	    	UserClassDTO dto = stats.getUserClasses().get(i);
	    	data.setValue(i, 0, dto.getName()+":"+dto.getStatistics().getFileSizeAsString());
		    data.setValue(i, 1, dto.getStatistics().getFileSize());
	    }

	    options = PieChart.Options.create();
	    options.setWidth(350);
	    options.setHeight(240);
	    options.set3D(true);
	    options.setColors("blue","red","green");
	    options.setTitle("Total File Size:"+stats.getFileSizeAsString());
	    fileSizeChart.draw(data, options);

	    data = DataTable.create();
	    data.addColumn(ColumnType.STRING, "UserClass");
	    data.addColumn(ColumnType.NUMBER, "Active Last Month");
	    data.addRows(2);
	    data.setValue(0, 0, "Active"+":"+stats.getLastMonthUsers()+" users");
		data.setValue(0, 1, stats.getLastMonthUsers());
		data.setValue(1, 0, "Inactive"+":"+(stats.getUserCount()-stats.getLastMonthUsers())+" users");
		data.setValue(1, 1, (stats.getUserCount()-stats.getLastMonthUsers()));


	    options = PieChart.Options.create();
	    options.setWidth(350);
	    options.setHeight(240);
	    options.set3D(true);
	    options.setTitle("Last Month Users:"+stats.getLastMonthUsers());
	    lastMonthChart.draw(data, options);

	    data = DataTable.create();
	    data.addColumn(ColumnType.STRING, "UserClass");
	    data.addColumn(ColumnType.NUMBER, "Last Week Users");
	    data.addRows(2);
	    data.setValue(0, 0, "Active"+":"+stats.getLastWeekUsers() +" users");
		data.setValue(0, 1, stats.getLastWeekUsers());
		data.setValue(1, 0, "Inactive"+":"+(stats.getUserCount()-stats.getLastWeekUsers())+" users");
		data.setValue(1, 1, (stats.getUserCount()-stats.getLastWeekUsers()));

	    options = PieChart.Options.create();
	    options.setWidth(350);
	    options.setHeight(240);
	    options.set3D(true);
	    options.setTitle("Last Week Users:"+stats.getLastWeekUsers());
	    lastWeekChart.draw(data, options);

	    data = DataTable.create();
	    data.addColumn(ColumnType.STRING, "UserClass");
	    data.addColumn(ColumnType.NUMBER, "Bandwith Used");
	    data.addRows(stats.getUserClasses().size());
	    for(int i=0;i<stats.getUserClasses().size();i++){
	    	UserClassDTO dto = stats.getUserClasses().get(i);
	    	data.setValue(i, 0, dto.getName()+":"+dto.getStatistics().getBandwithUsedAsString());
		    data.setValue(i, 1, dto.getStatistics().getBandwithUsed());
	    }

	    options = PieChart.Options.create();
	    options.setWidth(350);
	    options.setHeight(240);
	    options.set3D(true);
	    options.setTitle("Bandwith Used:"+stats.getBandwithUsedAsString());
	    bandwithChart.draw(data, options);
	}

}
