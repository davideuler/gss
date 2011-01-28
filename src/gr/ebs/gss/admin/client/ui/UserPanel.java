package gr.ebs.gss.admin.client.ui;

import gr.ebs.gss.admin.client.TwoAdmin;
import gr.ebs.gss.server.domain.dto.StatsDTO;
import gr.ebs.gss.server.domain.dto.UserClassDTO;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class UserPanel extends Composite {

	private static UserPanelUiBinder uiBinder = GWT
			.create(UserPanelUiBinder.class);

	interface UserPanelUiBinder extends UiBinder<Widget, UserPanel> {
	}


	@UiField(provided=true) DateBox dateBox = new DateBox();
	@UiField(provided=true) Button showLastLoginButton = new Button();
	@UiField TextBox searchBox;
	@UiField Button searchButton;
	@UiField Button showInactiveButton;
	@UiField(provided=true) final UsersTable usersTable = new UsersTable();
	@UiField(provided=true) Grid g =new Grid(8,6);
	private Object lastQuery;

	public UserPanel() {
		DateTimeFormat dateFormat = DateTimeFormat.getLongDateFormat();
	    dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
		g.setCellPadding(5);
		g.setCellSpacing(5);
        g.setText( 0, 0, "Username:" );
        g.getCellFormatter().setStyleName(0, 0, "props-toplabels");
        g.setText( 1, 0, "Name:" );
        g.getCellFormatter().setStyleName(1, 0, "props-toplabels");
        g.setText( 2, 0, "Email:" );
        g.getCellFormatter().setStyleName(2, 0, "props-toplabels");
        g.setText( 3, 0, "User Class:" );
        g.getCellFormatter().setStyleName(3, 0, "props-toplabels");
        g.setText( 4, 0, "Active:" );
        g.getCellFormatter().setStyleName(4, 0, "props-toplabels");
        g.setText( 0, 2, "Quota:" );
        g.getCellFormatter().setStyleName(0, 2, "props-toplabels");
        g.setText( 1, 2, "Bandwith Quota:" );
        g.getCellFormatter().setStyleName(1, 2, "props-toplabels");
        g.setText( 2, 2, "File Count:" );
        g.getCellFormatter().setStyleName(2, 2, "props-toplabels");
        g.setText( 3, 2, "Total File Size:" );
        g.getCellFormatter().setStyleName(3, 2, "props-toplabels");
        g.setText( 4, 2, "Quota Left:" );
        g.getCellFormatter().setStyleName(4, 2, "props-toplabels");
        g.setText( 5, 2, "Bandwith Quota Used:" );
        g.getCellFormatter().setStyleName(5, 2, "props-toplabels");
        g.setText( 0, 4, "Quota Used%:" );
        g.getCellFormatter().setStyleName(0, 4, "props-toplabels");
        g.setText( 1, 4, "Bandwith Quota Used%:" );
        g.getCellFormatter().setStyleName(1, 4, "props-toplabels");
		initWidget(uiBinder.createAndBindUi(this));
		searchBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				char keyCode = event.getCharCode();
				if (keyCode == '\r')
					handleClick(null);
			}
		});
		usersTable.addRowSelectionHandler(new RowSelectionHandler() {

			@Override
			public void onRowSelection(RowSelectionEvent event) {
				final UserDTO user = usersTable.getSelectedRowObject(event);
				clearProfile();
				if(user!=null)
					DeferredCommand.addCommand(new Command() {

						@Override
						public void execute() {
							TwoAdmin.get().showLoadingBox();
							TwoAdmin.get().getAdminService().getUserStatistics(user.getId(), new AsyncCallback<StatsDTO>() {

								@Override
								public void onSuccess(StatsDTO result) {
									displayProfile(user,result);
									TwoAdmin.get().hideLoadingBox();

								}

								@Override
								public void onFailure(Throwable caught) {
									GWT.log("Error requesting user statistics file", caught);
									TwoAdmin.get().hideLoadingBox();
									TwoAdmin.get().showErrorBox("Error requesting user statistics");

								}
							});

						}
					});

			}
		});


		clearProfile();

	}

	@UiHandler("showInactiveButton")
	void handleWaitingClick(@SuppressWarnings("unused") ClickEvent e){
		lastQuery = true;
		populateTable();
	}

	@UiHandler("searchButton")
	void handleClick(@SuppressWarnings("unused") ClickEvent e){
		final String toSearch = searchBox.getText();
		if(toSearch == null || "".equals(toSearch.trim())){
			TwoAdmin.get().showErrorBox("You must enter a query");
			return;
		}
		lastQuery = toSearch;
		populateTable(toSearch);
	}

	@UiHandler("showLastLoginButton")
	void handleDateClick(@SuppressWarnings("unused") ClickEvent e){
		final Date toSearch = dateBox.getValue();
		if(toSearch == null){
			TwoAdmin.get().showErrorBox("You must enter a query");
			return;
		}
		lastQuery = toSearch;
		populateTable(toSearch);
	}


	public void populateTable(String query){
		TwoAdmin.get().showLoadingBox();
		if(query.startsWith("username:")){
			String username = query.replaceAll("username:","");
			TwoAdmin.get().getAdminService().getUser(username, new AsyncCallback<UserDTO>() {

							@Override
							public void onFailure(Throwable caught) {
								TwoAdmin.get().hideLoadingBox();
								GWT.log("Error fetching users", caught);
								TwoAdmin.get().showErrorBox("Unable to Find any Users");

							}

							@Override
							public void onSuccess(final UserDTO user) {
								List<UserDTO> res = new ArrayList<UserDTO>();
								res.add(user);
								usersTable.showUsers(res);
								TwoAdmin.get().getAdminService().getUserStatistics(user.getId(), new AsyncCallback<StatsDTO>() {

									@Override
									public void onSuccess(StatsDTO result) {
										displayProfile(user,result);
										TwoAdmin.get().hideLoadingBox();

									}

									@Override
									public void onFailure(Throwable caught) {
										GWT.log("Error requesting user statistics file", caught);
										TwoAdmin.get().hideLoadingBox();
										TwoAdmin.get().showErrorBox("Error requesting user statistics");

									}
								});
							}
							});
		} else
			TwoAdmin.get().getAdminService().searchUsers(query,new AsyncCallback<List<UserDTO>>() {

				@Override
				public void onFailure(Throwable caught) {
					TwoAdmin.get().hideLoadingBox();
					GWT.log("Error fetching users", caught);
					TwoAdmin.get().showErrorBox("Unable to Find any Users");
				}

				@Override
				public void onSuccess(List<UserDTO> result) {
					usersTable.showUsers(result);
					TwoAdmin.get().hideLoadingBox();

				}

			});
	}

	private void populateTable(){
		TwoAdmin.get().showLoadingBox();
		TwoAdmin.get().getAdminService().getUsersWaitingActivation(new AsyncCallback<List<UserDTO>>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error fetching users", caught);
				TwoAdmin.get().showErrorBox("Unable to Find any Users");
				TwoAdmin.get().hideLoadingBox();
			}

			@Override
			public void onSuccess(List<UserDTO> result) {
				usersTable.showUsers(result);
				TwoAdmin.get().hideLoadingBox();
			}

		});
	}

	private void populateTable(Date query){
		TwoAdmin.get().showLoadingBox();
		TwoAdmin.get().getAdminService().getLastLoggedInUsers(query,new AsyncCallback<List<UserDTO>>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error fetching users", caught);
				TwoAdmin.get().showErrorBox("Unable to Find any Users");
				TwoAdmin.get().hideLoadingBox();
			}

			@Override
			public void onSuccess(List<UserDTO> result) {
				usersTable.showUsers(result);
				TwoAdmin.get().hideLoadingBox();
			}

		});
	}


	public void clearProfile(){
		g.getColumnFormatter().setWidth(1, "200px");
		g.setWidget( 0, 1, new HTML("") );
        g.setWidget( 1, 1, new HTML("") );
        g.setWidget( 2, 1, new HTML("") );
        g.setWidget( 3, 1, new HTML("") );

        CheckBox ck = new CheckBox();
        ck.setValue(false);
        ck.setEnabled(false);
        g.setWidget( 4, 1, ck );
        g.getColumnFormatter().setWidth(3, "200px");
        g.setWidget( 0, 3, new HTML("") );
        g.setWidget( 1, 3, new HTML("") );
        g.setWidget( 2, 3, new HTML("") );
        g.setWidget( 3, 3, new HTML("") );
        g.setWidget( 4, 3, new HTML("") );
        g.setWidget( 5, 3, new HTML("") );
        g.setWidget( 0, 5, new HTML("") );
        g.setWidget( 1, 5, new HTML("") );
        g.setWidget( 2, 5, new HTML("") );

	}

	public void displayProfile(final UserDTO user, StatsDTO stats){
		g.setWidget( 0, 1, new HTML(user.getUsername()) );
        g.setWidget( 1, 1, new HTML(user.getName()) );

        g.setWidget( 2, 1, new HTML(user.getEmail()) );
        if(user.getUserClass()!=null){
        	HTML userClass;
        	g.setWidget( 3, 1,userClass=new HTML("<a href='#'>"+user.getUserClass().getName()+"</a>") );
        	userClass.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					TwoAdmin.get().getAdminService().getUserClasses(new AsyncCallback<List<UserClassDTO>>() {

						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onSuccess(List<UserClassDTO> result) {
							DialogBox cbox =createDialogBox(user, result);
							cbox.center();
							cbox.show();

						}

					});

				}
			});
        }
        final CheckBox ck = new CheckBox();
        ck.setValue(user.isActive());
        ck.setEnabled(false);
        g.setWidget( 4, 1, ck );
        Button status = new Button("Toggle Active");
        status.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				ConfirmationDialog confirm = new ConfirmationDialog("Are you sure you want to toggle user State?","Toggle") {

					@Override
					public void confirm() {
						TwoAdmin.get().showLoadingBox();
						TwoAdmin.get().getAdminService().toggleActiveUser(user.getId(), new AsyncCallback<Void>() {

							@Override
							public void onSuccess(Void result) {
								ck.setValue(!user.isActive());
								if(getLastQuery() instanceof Date)
									populateTable((Date)getLastQuery());
								else if(getLastQuery() instanceof String)
									populateTable((String)getLastQuery());
								else
									populateTable();
								TwoAdmin.get().hideLoadingBox();

							}

							@Override
							public void onFailure(Throwable caught) {
								TwoAdmin.get().hideLoadingBox();

							}
						});

					}

					@Override
					public void cancel() {
						// TODO Auto-generated method stub

					}
				};
				confirm.center();
				confirm.show();

			}
		});
        g.setWidget(5,1,status);

        if(user.getUserClass()!=null){
	        g.setWidget( 0, 3, new HTML(user.getUserClass().getQuotaAsString()) );
	        g.setWidget( 1, 3, new HTML("") ); // XXX
	        g.setWidget( 1, 5, new HTML("") ); // XXX
        }
        g.setWidget( 2, 3, new HTML(""+stats.getFileCount()) );
        g.setWidget( 3, 3, new HTML(stats.getFileSizeAsString()) );
        g.setWidget( 4, 3, new HTML(stats.getQuotaLeftAsString()) );
        g.setWidget( 5, 3, new HTML("") ); // XXX
        g.setWidget( 0, 5, new HTML(100-stats.percentOfFreeSpace()+"%"));
        Button remove = new Button("Remove User");
        remove.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				ConfirmationDialog confirm = new ConfirmationDialog("Are you" +
						" sure you want to <strong>permanently</strong> " +
						"remove user " + user.getUsername() + "?", "Remove") {

					@Override
					public void confirm() {
						TwoAdmin.get().showLoadingBox();
						TwoAdmin.get().getAdminService().removeUser(user.getId(), new AsyncCallback<Void>() {

							@Override
							public void onSuccess(Void result) {
								if(getLastQuery() instanceof Date)
									populateTable((Date)getLastQuery());
								else if(getLastQuery() instanceof String)
									populateTable((String)getLastQuery());
								else
									populateTable();
								TwoAdmin.get().hideLoadingBox();

							}

							@Override
							public void onFailure(Throwable caught) {
								GWT.log("",caught);
								TwoAdmin.get().hideLoadingBox();

							}
						});

					}

					@Override
					public void cancel() {
						// TODO Auto-generated method stub

					}
				};
				confirm.center();
				confirm.show();

			}
		});
        g.setWidget(2,5,remove);



	}


	/**
	 * Retrieve the lastQuery.
	 *
	 * @return the lastQuery
	 */
	public Object getLastQuery() {
		return lastQuery;
	}

	public DialogBox createDialogBox(final UserDTO m, List<UserClassDTO> classes) {

		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setHTML("User Class: "+m.getUserClass().getName());

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
		final ListBox listbox = new ListBox();
		int i=0;
		for(UserClassDTO u : classes){
			listbox.addItem(u.getName(), u.getId().toString());
			if(u.getName().equals(m.getUserClass().getName()))
				listbox.setSelectedIndex(i);
			i++;
		}
		dialogVPanel.add(listbox);
		Button change = new Button("Change");
		change.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				TwoAdmin.get().getAdminService().changeUserClass(m.getId(), Long.parseLong(listbox.getValue(listbox.getSelectedIndex())), new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {


					}

					@Override
					public void onSuccess(Void result) {
						if(getLastQuery() instanceof Date)
							populateTable((Date)getLastQuery());
						else if(getLastQuery() instanceof String)
							populateTable((String)getLastQuery());
						else
							populateTable();
						TwoAdmin.get().hideLoadingBox();

					}

				});
				dialogBox.hide();

			}
		});

		Button close = new Button("Close");
		close.addClickHandler(cancelHandler);
		dialogVPanel.add(change);
		dialogVPanel.add(close);

		// Return the dialog box
		return dialogBox;
	}
}
