package gr.ebs.gss.admin.client.ui;

import gr.ebs.gss.admin.client.TwoAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class HeaderPanel extends Composite {

	interface Binder extends UiBinder<Widget, HeaderPanel> { }
	  private static final Binder binder = GWT.create(Binder.class);

	  @UiField Anchor signOutLink;

	  public HeaderPanel() {
	    initWidget(binder.createAndBindUi(this));
	  }

	  @UiHandler("signOutLink")
	  void onSignOutClicked(@SuppressWarnings("unused") ClickEvent event) {
	    TwoAdmin.get().getAdminService().logout(new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
				Window.open("/admin", "_self", null);

			}

			@Override
			public void onFailure(Throwable caught) {
				TwoAdmin.get().showErrorBox(caught.getMessage());

			}
		});
	  }

}
