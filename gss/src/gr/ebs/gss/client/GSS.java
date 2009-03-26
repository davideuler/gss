/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
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

import gr.ebs.gss.client.clipboard.Clipboard;
import gr.ebs.gss.client.dnd.DnDFocusPanel;
import gr.ebs.gss.client.rest.ExecuteGet;
import gr.ebs.gss.client.rest.RestException;
import gr.ebs.gss.client.rest.resource.UserResource;

import java.util.Iterator;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GSS implements EntryPoint, WindowResizeListener {
	/**
	 * The name of the authentication cookie.
	 */
	private static final String AUTH_COOKIE = "_gss_a";

	/**
	 * The separator character for the authentication cookie.
	 */
	private static final char COOKIE_SEPARATOR = '|';

	/**
	 * The URL for logging into the system.
	 */
	private static final String GSS_LOGIN_URL = "https://gss.grnet.gr/gss/login";

	/**
	 * The URL for logging out of the system.
	 */
	private static final String GSS_LOGOUT_URL = "http://gss.grnet.gr/Shibboleth.sso/Logout";

	public static final String GSS_REST_PATH = GWT.getModuleBaseURL() + "rest/";

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	public static final int VISIBLE_FILE_COUNT = 100;

	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programmatic access to all the images needed by widgets.
	 */
	private static Images images = (Images) GWT.create(Images.class);
	private GlassPanel glassPanel = new GlassPanel();

	/**
	 * An aggregate image bundle that pulls together all the images for this
	 * application into a single bundle.
	 */
	public interface Images extends TopPanel.Images, StatusPanel.Images, FileMenu.Images, EditMenu.Images, SettingsMenu.Images, GroupMenu.Images, FilePropertiesDialog.Images, MessagePanel.Images, FileList.Images,  SearchResults.Images, Search.Images, Groups.Images, Folders.Images {

		@Resource("gr/ebs/gss/resources/document.png")
		AbstractImagePrototype folders();

		@Resource("gr/ebs/gss/resources/edit_group_22.png")
		AbstractImagePrototype groups();

		@Resource("gr/ebs/gss/resources/search.png")
		AbstractImagePrototype search();
	}

	/**
	 * The single GSS instance.
	 */
	private static GSS singleton;

	/**
	 * Gets the singleton GSS instance.
	 *
	 * @return the GSS object
	 */
	public static GSS get() {
		if (GSS.singleton == null)
			GSS.singleton = new GSS();
		return GSS.singleton;
	}

	/**
	 * The Application Clipboard implementation;
	 */
	private Clipboard clipboard = new Clipboard();

	private UserResource currentUserResource;

	/**
	 * The top panel that contains the menu bar.
	 */
	private TopPanel topPanel;

	/**
	 * The panel that contains the various system messages.
	 */
	private MessagePanel messagePanel = new MessagePanel(GSS.images);

	/**
	 * The bottom panel that contains the status bar.
	 */
	private StatusPanel statusPanel = new StatusPanel(GSS.images);

	/**
	 * The top right panel that displays the logged in user details
	 */
	private UserDetailsPanel userDetailsPanel = new UserDetailsPanel();

	/**
	 * The file list widget.
	 */
	private FileList fileList;

	/**
	 * The group list widget.
	 */
	private Groups groups  = new Groups(images);

	/**
	 * The search result widget.
	 */
	private SearchResults searchResults;

	/**
	 * A widget that displays a message indicating that communication with the
	 * server is underway.
	 */
	private LoadingIndicator loading;

	/**
	 * The tab panel that occupies the right side of the screen.
	 */
	private TabPanel inner = new TabPanel();

	/**
	 * The split panel that will contain the left and right panels.
	 */
	private HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();

	/**
	 * The horizontal panel that will contain the search and status panels.
	 */
	private DockPanel searchStatus = new DockPanel();

	/**
	 * The search widget.
	 */
	private Search search;

	/**
	 * The widget that displays the tree of folders.
	 */
	private Folders folders = new Folders(images);

	/**
	 * The currently selected item in the application, for use by the Edit menu
	 * commands. Potential types are Folder, File, User and Group.
	 */
	private Object currentSelection;

	/**
	 * The authentication token of the current user.
	 */
	private String token;

	private PickupDragController dragController;

	public void onModuleLoad() {
		// Initialize the singleton before calling the constructors of the
		// various widgets that might call GSS.get().
		singleton = this;
		RootPanel.get().add(glassPanel, 0, 0);
		parseUserCredentials();
		dragController = new PickupDragController(RootPanel.get(), false) {

			@Override
			public void previewDragStart() throws VetoDragException {
			    super.previewDragStart();
			    if (context.selectedWidgets.isEmpty())
					throw new VetoDragException();

			    if(context.draggable != null){
					DnDFocusPanel toDrop = (DnDFocusPanel) context.draggable;
					//prevent drag and drop for trashed files and for unselected tree items
					if(toDrop.getFiles() != null && folders.isTrashItem(folders.getCurrent()))
						throw new VetoDragException();
					else if(toDrop.getItem() != null && !toDrop.getItem().equals(folders.getCurrent()))
						throw new VetoDragException();
					else if(toDrop.getItem() != null && !toDrop.getItem().isDraggable())
						throw new VetoDragException();

			    }
			  }

			@Override
			protected Widget newDragProxy(DragContext aContext) {
				AbsolutePanel container = new AbsolutePanel();
				DOM.setStyleAttribute(container.getElement(), "overflow", "visible");
				for (Iterator iterator = aContext.selectedWidgets.iterator(); iterator.hasNext();) {
					Widget widget = (Widget) iterator.next();
					DnDFocusPanel book = (DnDFocusPanel) widget;
					HTML html = book.cloneHTML();
					if(html == null)
						container.add(new Label("Drag ME"));
					else
						container.add(html);
				}
				return container;
			}
		};
		dragController.setBehaviorDragProxy(true);
		dragController.setBehaviorMultipleSelection(false);
		topPanel = new TopPanel(GSS.images);
		topPanel.setWidth("100%");

		messagePanel.setWidth("100%");
		messagePanel.setVisible(false);

		search = new Search(images);
		searchStatus.add(search, DockPanel.WEST);
		searchStatus.add(userDetailsPanel, DockPanel.EAST);
		searchStatus.setCellHorizontalAlignment(userDetailsPanel, HasHorizontalAlignment.ALIGN_RIGHT);
		searchStatus.setCellVerticalAlignment(search, HasVerticalAlignment.ALIGN_MIDDLE);
		searchStatus.setCellVerticalAlignment(userDetailsPanel, HasVerticalAlignment.ALIGN_MIDDLE);
		searchStatus.setWidth("100%");

		fileList = new FileList(images);

		searchResults = new SearchResults(images);

		// Inner contains the various lists.
		inner.getTabBar().setStyleName("gss-TabBar");
		inner.setStyleName("gss-TabPanel");
		inner.add(fileList, createHeaderHTML(images.folders(), "Files"), true);

		inner.add(groups, createHeaderHTML(images.groups(), "Groups"), true);
		inner.add(searchResults, createHeaderHTML(images.search(), "Search Results"), true);
		inner.setWidth("100%");
		inner.selectTab(0);

		inner.addTabListener(new TabListener() {
			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
		    	switch (tabIndex) {
		    		case 0:
		    			fileList.updateCurrentlyShowingStats();
		    			break;
		    		case 1:
		    			groups.updateCurrentlyShowingStats();
		    			break;
		    		case 2:
		    			searchResults.updateCurrentlyShowingStats();
		    			break;
		    	}
			}

			public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
				return true;
			}
		});

		// Add the left and right panels to the split panel.
		splitPanel.setLeftWidget(folders);
		splitPanel.setRightWidget(inner);
		splitPanel.setSplitPosition("25%");
		splitPanel.setSize("100%", "100%");

		// Create a dock panel that will contain the menu bar at the top,
		// the shortcuts to the left, the status bar at the bottom and the
		// right panel taking the rest.
		VerticalPanel outer = new VerticalPanel();
		outer.add(topPanel);
		outer.add(searchStatus);
		outer.add(messagePanel);
		outer.add(splitPanel);
		outer.add(statusPanel);
		outer.setWidth("100%");
		outer.setCellHorizontalAlignment(messagePanel, HasHorizontalAlignment.ALIGN_CENTER);

		outer.setSpacing(4);

		loading = new LoadingIndicator();

		// Hook the window resize event, so that we can adjust the UI.
		Window.addWindowResizeListener(this);

		// Clear out the window's built-in margin, because we want to take
		// advantage of the entire client area.
		Window.setMargin("0px");

		// Finally, add the outer panel to the RootPanel, so that it will be
		// displayed.
		RootPanel.get().add(outer);

		// Call the window resized handler to get the initial sizes setup. Doing
		// this in a deferred command causes it to occur after all widgets'
		// sizes have been computed by the browser.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window.getClientHeight());
			}
		});
	}

	/**
	 * Fetches the User object for the specified username.
	 *
	 * @param username the username of the user
	 */
	private void fetchUser(final String username) {
		String path = GSS_REST_PATH+username+"/";
		ExecuteGet<UserResource> getUserCommand = new ExecuteGet<UserResource>(UserResource.class, username, path){

			@Override
			public void onComplete() {
				currentUserResource = getResult();
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("Fetching user error", t);
				if(t instanceof RestException)
					GSS.get().displayError("No user found:"+((RestException)t).getHttpStatusText());
				else
					GSS.get().displayError("System error fetching user data:"+t.getMessage());
				authenticateUser();
			}
		};
		DeferredCommand.addCommand(getUserCommand);
	}

	/**
	 * Parse and store the user credentials to the appropriate fields.
	 */
	private void parseUserCredentials() {
		//------------------------
		// XXX This part is only for development environments!
		// XXX Remove/comment for production deployment!
		final String _username = Window.Location.getParameter("user");
		token = Window.Location.getParameter("token");
		if (_username != null) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					fetchUser(_username);
				}
			});
			return;
		}
		//------------------------
		String auth = Cookies.getCookie(AUTH_COOKIE);
		String domain = Window.Location.getHostName();
		String path = Window.Location.getPath();
		Cookies.setCookie(AUTH_COOKIE, "", null, domain, path, false);
		if (auth == null) {
			authenticateUser();
			// Redundant, but silences warnings about possible auth NPE, below.
			return;
		}
		int sepIndex = auth.indexOf(COOKIE_SEPARATOR);
		if (sepIndex == -1)
			authenticateUser();
		token = auth.substring(sepIndex + 1, auth.length());
		final String username = auth.substring(0, sepIndex);
		if (username == null)
			authenticateUser();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				fetchUser(username);
			}
		});
	}

	/**
	 * Redirect the user to the login page for authentication.
	 */
	protected void authenticateUser() {
		Window.Location.assign(GSS_LOGIN_URL + "?next=" + GWT.getModuleBaseURL());
	}

	/**
	 * Redirect the user to the logout page.
	 */
	void logout() {
		Window.Location.assign(GSS_LOGOUT_URL);
	}

	/**
	 * Creates an HTML fragment that places an image & caption together, for use
	 * in a group header.
	 *
	 * @param imageProto an image prototype for an image
	 * @param caption the group caption
	 * @return the header HTML fragment
	 */
	private String createHeaderHTML(AbstractImagePrototype imageProto, String caption) {
		String captionHTML = "<table class='caption' cellpadding='0' " +
				"cellspacing='0'>" + "<tr><td class='lcaption'>" + imageProto.getHTML() +
				"</td><td class='rcaption'><b style='white-space:nowrap'>&nbsp;" +
				caption + "</b></td></tr></table>";
		return captionHTML;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.WindowResizeListener#onWindowResized(int,int)
	 */
	public void onWindowResized(int width, int height) {
		// Adjust the split panel to take up the available room in the window.
		int newHeight = height - splitPanel.getAbsoluteTop() - 44;
		if (newHeight < 1)
			newHeight = 1;
		splitPanel.setHeight("" + newHeight);
	}

	public boolean isFileListShowing(){
		int tab = inner.getTabBar().getSelectedTab();
		if(tab == 0) return true;
		return false;
	}

	public boolean isSearchResultsShowing(){
		int tab = inner.getTabBar().getSelectedTab();
		if(tab == 2) return true;
		return false;
	}

	/**
	 * Make the user list visible.
	 */
	public void showUserList() {
		inner.selectTab(1);
	}

	/**
	 * Make the file list visible.
	 */
	public void showFileList() {
		fileList.updateFileCache(false);
		inner.selectTab(0);
	}

	/**
	 * Make the file list visible.
	 * @param update
	 */
	public void showFileList(boolean update) {
		fileList.updateFileCache(update);
		inner.selectTab(0);
	}

	/**
	 * Make the search results visible.
	 * @param query the search query string
	 */
	public void showSearchResults(String query) {
		searchResults.updateFileCache(query);
		searchResults.updateCurrentlyShowingStats();
		inner.selectTab(2);
	}

	/**
	 * Display the 'loading' indicator.
	 */
	public void showLoadingIndicator() {
		loading.center();
	}

	/**
	 * Hide the 'loading' indicator.
	 */
	public void hideLoadingIndicator() {
		loading.hide();
	}

	/**
	 * A native JavaScript method to reach out to the browser's window and
	 * invoke its resizeTo() method.
	 *
	 * @param x the new width
	 * @param y the new height
	 */
	public static native void resizeTo(int x, int y) /*-{
	 $wnd.resizeTo(x,y);
	}-*/;

	/**
	 * A helper method that returns true if the user's list is currently visible
	 * and false if it is hidden.
	 *
	 * @return true if the user list is visible
	 */
	public boolean isUserListVisible() {
		return inner.getTabBar().getSelectedTab() == 1;
	}

	/**
	 * Display an error message.
	 *
	 * @param msg the message to display
	 */
	public void displayError(String msg) {
		messagePanel.displayError(msg);
	}

	/**
	 * Display a warning message.
	 *
	 * @param msg the message to display
	 */
	public void displayWarning(String msg) {
		messagePanel.displayWarning(msg);
	}

	/**
	 * Display an informational message.
	 *
	 * @param msg the message to display
	 */
	public void displayInformation(String msg) {
		messagePanel.displayInformation(msg);
	}

	/**
	 * Retrieve the folders.
	 *
	 * @return the folders
	 */
	public Folders getFolders() {
		return folders;
	}

	/**
	 * Retrieve the search.
	 *
	 * @return the search
	 */
	Search getSearch() {
		return search;
	}

	/**
	 * Retrieve the currentSelection.
	 *
	 * @return the currentSelection
	 */
	public Object getCurrentSelection() {
		return currentSelection;
	}

	/**
	 * Modify the currentSelection.
	 *
	 * @param newCurrentSelection the currentSelection to set
	 */
	public void setCurrentSelection(Object newCurrentSelection) {
		currentSelection = newCurrentSelection;
	}

	/**
	 * Retrieve the groups.
	 *
	 * @return the groups
	 */
	public Groups getGroups() {
		return groups;
	}

	/**
	 * Retrieve the fileList.
	 *
	 * @return the fileList
	 */
	public FileList getFileList() {
		return fileList;
	}

	public SearchResults getSearchResults(){
		return searchResults;
	}

	/**
	 * Retrieve the topPanel.
	 *
	 * @return the topPanel
	 */
	TopPanel getTopPanel() {
		return topPanel;
	}

	/**
	 * Retrieve the clipboard.
	 *
	 * @return the clipboard
	 */
	public Clipboard getClipboard() {
		return clipboard;
	}


	public StatusPanel getStatusPanel(){
		return statusPanel;
	}

	/**
	 * Retrieve the dragController.
	 *
	 * @return the dragController
	 */
	public PickupDragController getDragController() {
		return dragController;
	}

	public String getToken(){
		return token;
	}

	public void removeGlassPanel(){
		glassPanel.removeFromParent();
	}

	/**
	 * Retrieve the currentUserResource.
	 *
	 * @return the currentUserResource
	 */
	public UserResource getCurrentUserResource() {
		return currentUserResource;
	}

}
