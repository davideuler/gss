package gr.ebs.gss.client.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Checkbox with three states (checked, unchecked and undefined).
 *
 */
public class TristateCheckBox extends FocusWidget implements HasValue<Boolean> {

    private static final String UNCHECKED_IMG = "images/tristate_unchecked.gif";

    private static final String UNKNOWN_IMG = "images/tristate_intermediate.gif";

    private static final String CHECKED_IMG = "images/tristate_checked.gif";

    private final Element buttonElement = DOM.createElement("input");

    private boolean valueChangeHandlerInitialized;

    private Boolean value;

    private Boolean initialValue;

    public TristateCheckBox(final Boolean state) {
        DOM.setElementProperty(buttonElement, "type", "image");
        setElement(buttonElement);
        setStyleName("tristateCheckbox");
        DOM.setElementAttribute(buttonElement, "src", UNCHECKED_IMG);

        addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
                final String img = DOM.getElementAttribute(buttonElement, "src");
                String newImg;
                if (img.endsWith(UNCHECKED_IMG))
					newImg = CHECKED_IMG;
				else if (img.endsWith(UNKNOWN_IMG))
					newImg = UNCHECKED_IMG;
				else if (img.endsWith(CHECKED_IMG))
					if (initialValue==null) // Only show unknown choice if there is a reason for it
						newImg = UNKNOWN_IMG;
					else
						newImg = UNCHECKED_IMG;
				else
					throw new IllegalArgumentException("unknown checkbox state");

                DOM.setElementAttribute(buttonElement, "src", newImg);
            }

        });

        setState(state);
        initialValue = state;
    }

    public void setState(final Boolean state) {
    	DOM.setElementAttribute(buttonElement, "src", state == null ?
            		UNKNOWN_IMG : state.booleanValue() ? CHECKED_IMG : UNCHECKED_IMG);
    }

    public Boolean getState() {
        final String img = DOM.getElementAttribute(buttonElement, "src");
        if (img.endsWith(UNCHECKED_IMG))
			return Boolean.FALSE;
		else if (img.endsWith(UNKNOWN_IMG))
			return null;
		else if (img.endsWith(CHECKED_IMG))
			return Boolean.TRUE;
		else
			throw new IllegalArgumentException("unknown checkbox state");
    }

    @Override
	public Boolean getValue() {
        return value;
    }

    @Override
	public void setValue(final Boolean _value) {
        value = _value;
    }

    @Override
	public HandlerRegistration addValueChangeHandler(
	    ValueChangeHandler<Boolean> handler) {
	    // Is this the first value change handler? If so, time to add handlers
	    if (!valueChangeHandlerInitialized) {
	    	ensureDomEventHandlers();
	    	valueChangeHandlerInitialized = true;
	    }
	    return addHandler(handler, ValueChangeEvent.getType());
	  }

    protected void ensureDomEventHandlers() {
        addClickHandler(new ClickHandler() {
        	@Override
			public void onClick(ClickEvent event) {
        		ValueChangeEvent.fire(TristateCheckBox.this, getValue());
        	}
        });
    }

    @Override
	public void setValue(Boolean _value, boolean fireEvents) {
        Boolean oldValue = getValue();
        setValue(_value);
        if (_value.equals(oldValue))
			return;
        if (fireEvents)
			ValueChangeEvent.fire(this, _value);
    }

}