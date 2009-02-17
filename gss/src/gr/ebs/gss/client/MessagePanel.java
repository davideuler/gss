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

import gr.ebs.gss.client.animation.FadeIn;
import gr.ebs.gss.client.animation.FadeOut;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that displays various system messages.
 */
public class MessagePanel extends Composite {



	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ImageBundle {

		/**
		 * Will bundle the file 'messagebox_info.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/messagebox_info.png")
		AbstractImagePrototype info();

		/**
		 * Will bundle the file 'messagebox_warning.png' residing in the package
		 * 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/messagebox_warning.png")
		AbstractImagePrototype warn();

		/**
		 * Will bundle the file 'messagebox_critical.png' residing in the
		 * package 'gr.ebs.gss.resources'.
		 *
		 * @return the image prototype
		 */
		@Resource("gr/ebs/gss/resources/messagebox_critical.png")
		AbstractImagePrototype error();
	}

	/**
	 * The widget's images.
	 */
	private Images images;

	/**
	 * The system message to be displayed.
	 */
	private HTML message = new HTML("&nbsp;");

	/**
	 * A link to clear the displayed message.
	 */
	private HTML clearMessageLink = new HTML("<a class='gss-clearMessage' href='javascript:;'>Clear</a>");

	/**
	 * The panel that contains the messages.
	 */
	private HorizontalPanel inner = new HorizontalPanel();

	/**
	 * The panel that enables special effects for this widget.
	 */
	//private EffectPanel effects = new EffectPanel();
	private SimplePanel simplePanel = new SimplePanel();
	/**
	 * The widget's constructor.
	 *
	 * @param newImages a bundle that provides the images for this widget
	 */
	public MessagePanel(final Images newImages) {
		images = newImages;
		buildPanel();
		simplePanel.setStyleName("effectPanel");
		inner.setStyleName("effectPanel-inner");
		DOM.setStyleAttribute(simplePanel.getElement(), "zoom", "1");
		simplePanel.add(inner);
		initWidget(simplePanel);


	}

	/**
	 * Build the panel that contains the icon, the message and the 'clear' link.
	 */
	private void buildPanel() {

		inner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		inner.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		inner.setSpacing(4);
		inner.add(message);
		inner.add(clearMessageLink);
		inner.setCellVerticalAlignment(message, HasVerticalAlignment.ALIGN_MIDDLE);

		clearMessageLink.addClickListener(new ClickListener() {

			public void onClick(final Widget sender) {
				FadeOut anim = new FadeOut(simplePanel){
					/* (non-Javadoc)
					 * @see com.google.gwt.animation.client.Animation#onComplete()
					 */
					protected void onComplete() {
						// TODO Auto-generated method stub
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(1000);

			}
		});
	}

	/**
	 * Display an error message.
	 *
	 * @param msg the message to display
	 */
	public void displayError(final String msg) {
		GWT.log(msg, null);
		message = new HTML("<table class='gss-errorMessage'><tr><td>" + images.error().getHTML() + "</td><td>" + msg + "</td></tr></table>");
		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(1000);
	}

	/**
	 * Display a warning message.
	 *
	 * @param msg the message to display
	 */
	public void displayWarning(final String msg) {
		message = new HTML("<table class='gss-warnMessage'><tr><td>" + images.warn().getHTML() + "</td><td>" + msg + "</td></tr></table>");
		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(1000);
	}

	/**
	 * Display an informational message.
	 *
	 * @param msg the message to display
	 */
	public void displayInformation(final String msg) {
		message = new HTML("<table class='gss-infoMessage'><tr><td>" + images.info().getHTML() + "</td><td>" + msg + "</td></tr></table>");
		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(1000);
	}

	/**
	 * Clear the displayed message and hide the panel.
	 */
	public void hideMessage() {
		inner.clear();
		message = new HTML("&nbsp;");
		this.setVisible(false);
	}


}
