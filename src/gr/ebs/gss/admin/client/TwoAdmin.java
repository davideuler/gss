package gr.ebs.gss.admin.client;

import gr.ebs.gss.admin.client.ui.HeaderPanel;
import gr.ebs.gss.admin.client.ui.UserPanel;
import gr.ebs.gss.admin.client.ui.VisualizationPanel;
import gr.ebs.gss.common.dto.SystemStatsDTO;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TwoAdmin implements EntryPoint {

	interface Binder extends UiBinder<DockLayoutPanel, TwoAdmin> {
	}

	private final AdminServiceAsync adminService = GWT.create(AdminService.class);

	private static final Binder binder = GWT.create(Binder.class);

	/**
	 * The single TwoAdmin instance.
	 */
	private static TwoAdmin singleton;

	/**
	 * Gets the singleton TwoAdmin instance.
	 *
	 * @return the TwoAdmin object
	 */
	public static TwoAdmin get() {
		if (TwoAdmin.singleton == null)
			TwoAdmin.singleton = new TwoAdmin();
		return TwoAdmin.singleton;
	}

	@UiField(provided = true)
	HeaderPanel headerPanel = new HeaderPanel();

	@UiField UserPanel userPanel;

	@UiField
	TabLayoutPanel tabPanel;

	@UiField VisualizationPanel chart2;

	final DialogBox loadingBox = new DialogBox();

	@Override
	public void onModuleLoad() {
		TwoAdmin.singleton=this;
		loadingBox.setHTML("Loading data");
		//scroll.setAlwaysShowScrollBars(true);
		VerticalPanel vPanel = new VerticalPanel();
		loadingBox.setSize("50%", "50%");
		loadingBox.setWidget(vPanel);
		vPanel.add(new Label("Fetching Requested Data"));

		DockLayoutPanel outer = binder.createAndBindUi(this);
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				switch (event.getSelectedItem()) {
					case 0:
						loadStatistics();
						break;
					default:
						break;
				}

			}
		});


		// Get rid of scrollbars, and clear out the window's built-in margin,
		// because we want to take advantage of the entire client area.
		//Window.enableScrolling(true);
		//Window.setMargin("0px");
		Window.addResizeHandler(new ResizeHandler(){

			@Override
			public void onResize(ResizeEvent event) {
				int height=event.getHeight();
				resize(height);

			}

		});
		RootLayoutPanel root = RootLayoutPanel.get();
		root.add(outer);
		// RootPanel.get().add(outer);
		loadStatistics();
		tabPanel.selectTab(0);

		resize(Window.getClientHeight());
	}

	private void resize(int height){

		int newHeight = height - tabPanel.getAbsoluteTop()-100;
		if (newHeight < 1)
			newHeight = 1;
		tabPanel.setHeight("" + newHeight);
		GWT.log("New Height:"+ newHeight);
	}
	/**
	 * Retrieve the adminService.
	 *
	 * @return the adminService
	 */
	public AdminServiceAsync getAdminService() {
		return adminService;
	}

	public void showErrorBox(String message) {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setHTML("An error occured");
		VerticalPanel vPanel = new VerticalPanel();
		dialogBox.setSize("50%", "50%");
		ClickHandler cancelHandler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		};
		dialogBox.setWidget(vPanel);
		vPanel.add(new Label(message));
		Button close = new Button("Close");
		close.addClickHandler(cancelHandler);
		vPanel.add(close);
		dialogBox.center();
		dialogBox.show();
	}



	public void showLoadingBox() {
		loadingBox.center();
		loadingBox.show();
	}

	public void hideLoadingBox(){
		loadingBox.hide();
	}


	public void loadStatistics(){
		DeferredCommand.addCommand(new Command() {

			@Override
			public void execute() {
				showLoadingBox();
				getAdminService().getSystemStatistics(new AsyncCallback<SystemStatsDTO>() {

					@Override
					public void onFailure(Throwable caught) {
						hideLoadingBox();
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(SystemStatsDTO result) {
						chart2.updateData(result);
						hideLoadingBox();
					}});

			}
		});
	}

	public void searchUsers(String query){
		tabPanel.selectTab(1);
		userPanel.populateTable(query);
	}
}
