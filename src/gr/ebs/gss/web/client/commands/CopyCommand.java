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
package gr.ebs.gss.web.client.commands;

import gr.ebs.gss.web.client.GSS;
import gr.ebs.gss.web.client.clipboard.ClipboardItem;
import gr.ebs.gss.web.client.rest.resource.FileResource;
import gr.ebs.gss.web.client.rest.resource.GroupUserResource;
import gr.ebs.gss.web.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
/**
 *
 * Command for copying a file, folder or user to GSS Clipboard
 * @author kman
 *
 */
public class CopyCommand implements Command{
	private PopupPanel containerPanel;

	public CopyCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;

		if (selection instanceof RestResourceWrapper) {
			ClipboardItem clipboardItem = new ClipboardItem((RestResourceWrapper) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof FileResource) {
			ClipboardItem clipboardItem = new ClipboardItem((FileResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof GroupUserResource) {
			ClipboardItem clipboardItem = new ClipboardItem((GroupUserResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		}
		 else if (selection instanceof List){
			 ClipboardItem clipboardItem = new ClipboardItem((List<FileResource>) selection);
			 GSS.get().getClipboard().setItem(clipboardItem);
		 }

	}

}
