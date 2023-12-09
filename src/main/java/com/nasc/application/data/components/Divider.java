package com.nasc.application.data.components;

import com.vaadin.flow.component.html.Span;

public class Divider extends Span {

    public Divider() {
        getStyle().set("background-color", "hsla(214, 47%, 21%, 0.38)");
        getStyle().set("flex", "0 0 2px");
        getStyle().set("align-self", "stretch");
    }
}