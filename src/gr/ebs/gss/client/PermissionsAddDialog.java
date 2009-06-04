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

import gr.ebs.gss.client.rest.GetCommand;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.GroupResource;
import gr.ebs.gss.client.rest.resource.PermissionHolder;
import gr.ebs.gss.client.rest.resource.UserResource;
import gr.ebs.gss.client.rest.resource.UserSearchResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListenerAdapter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author kman
 */
public class PermissionsAddDialog extends DialogBox {

	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private SuggestBox suggestBox = new SuggestBox(oracle);

	private String selectedUser = null;

	private List<GroupResource> groups;

	private ListBox groupBox = new ListBox();

	private CheckBox read = new CheckBox();

	private CheckBox write = new CheckBox();

	private CheckBox modifyACL = new CheckBox();

	private final PermissionsList permList;

	boolean userAdd;

	public PermissionsAddDialog(List<GroupResource> _groups, PermissionsList _permList, boolean _userAdd) {
		groups = _groups;
		userAdd = _userAdd;
		permList = _permList;
		for (GroupResource group : _groups)
			groupBox.addItem(group.getName(), group.getName());
		final VerticalPanel panel = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();
		setWidget(panel);
		final FlexTable permTable = new FlexTable();
		permTable.setText(0, 0, "Users/Groups");
		permTable.setText(0, 1, "Read");
		permTable.setText(0, 2, "Write");
		permTable.setText(0, 3, "Modify Access");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		if (userAdd) {
			suggestBox.addFocusListener(new FocusListenerAdapter() {
				@Override
				public void onFocus(Widget sender) {
					if (selectedUser != null && selectedUser.endsWith("@"))
						updateSuggestions();
				}
			});
			suggestBox.addKeyboardListener(new KeyboardListenerAdapter() {
				@Override
				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					// Ignore the arrow keys.
					if (keyCode==KEY_UP || keyCode==KEY_DOWN || keyCode==KEY_LEFT || keyCode==KEY_RIGHT)
						return;
					String text = suggestBox.getText().trim();
					// Avoid useless queries for keystrokes that do not modify the text.
					if (text.equals(selectedUser))
						return;
					selectedUser = text;
					// Go to the server only if the user typed the @ character.
					if (selectedUser.endsWith("@"))
						updateSuggestions();
				}
			});
			permTable.setWidget(1, 0, suggestBox);
		} else
			permTable.setWidget(1, 0, groupBox);
		permTable.setWidget(1, 1, read);
		permTable.setWidget(1, 2, write);
		permTable.setWidget(1, 3, modifyACL);

		permTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		permTable.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(1, 3, HasHorizontalAlignment.ALIGN_CENTER);
		panel.add(permTable);

		final Button ok = new Button("OK", new ClickListener() {

			public void onClick(Widget sender) {
				addPermission();
				hide();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("gwt-TabPanelBottom");
		panel.add(buttons);
		panel.addStyleName("gwt-TabPanelBottom");
	}

	private void addPermission() {
		PermissionHolder perm = new PermissionHolder();
		if (userAdd) {
			selectedUser = suggestBox.getText();
			for(PermissionHolder p : permList.permissions)
				if (selectedUser.equals(p.getUser())){
					GSS.get().displayError("User already exists");
					return;
				}
			perm.setUser(selectedUser);
		} else {
			String groupId = groupBox.getValue(groupBox.getSelectedIndex());
			GroupResource selected = null;
			for (GroupResource g : groups)
				if (g.getName().equals(groupId))
					selected = g;
			for(PermissionHolder p : permList.permissions)
				if (selected.equals(p.getGroup())){
					GSS.get().displayError("Group already exists");
					return;
				}
			perm.setGroup(selected.getName());
		}
		boolean readValue = read.isChecked();
		boolean writeValue = write.isChecked();
		boolean modifyValue = modifyACL.isChecked();

		perm.setRead(readValue);
		perm.setWrite(writeValue);
		perm.setModifyACL(modifyValue);
		permList.addPermission(perm);
		permList.updateTable();
	}

	@Override
	public boolean onKeyDownPreview(final char key, final int modifiers) {
		// Use the popup's key preview hooks to close the dialog when either
		// enter or escape is pressed.
		switch (key) {
			case KeyboardListener.KEY_ENTER:
				addPermission();
				hide();
				break;
			case KeyboardListener.KEY_ESCAPE:
				hide();
				break;
		}
		return true;
	}

	@Override
	public void center() {
		super.center();
		if (userAdd)
			suggestBox.setFocus(true);
	}

	/**
	 * Update the list of suggestions.
	 */
	protected void updateSuggestions() {
		final GSS app = GSS.get();
		String query = selectedUser.substring(0, selectedUser.length()-1);
		GWT.log("Searching for " + query, null);

		GetCommand<UserSearchResource> eg = new GetCommand<UserSearchResource>(UserSearchResource.class,
					app.getApiPath() + "users/" + URL.encodeComponent(query)) {

			@Override
			public void onComplete() {
				DisplayHelper.hideSuggestions(suggestBox);
				oracle.clear();
				UserSearchResource s = getResult();
				for (UserResource user : s.getUsers()) {
					GWT.log("Found " + user.getUsername(), null);
					oracle.add(user.getUsername());
				}
				DisplayHelper.showSuggestions(suggestBox, selectedUser);
			}

			@Override
			public void onError(Throwable t) {
				if(t instanceof RestException)
					app.displayError("Unable to perform search: "+((RestException)t).getHttpStatusText());
				else
					app.displayError("System error while searching for users: "+t.getMessage());
				GWT.log("", t);
				DisplayHelper.log(t.getMessage());
			}

		};
		DeferredCommand.addCommand(eg);
	}
}
