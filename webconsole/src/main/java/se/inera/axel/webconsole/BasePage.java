/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.webconsole;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class BasePage extends WebPage {
	private static final long serialVersionUID = 1L;


	public BasePage(final PageParameters parameters) {
		super(parameters);

		add(new Label("title", "Axel - " + getTitle()));
		System.out.println(":::: TRACE 1: " + this + ", " + getTitle());
		HeaderPanel header = new HeaderPanel("header");
		System.out.println(":::: TRACE 2: " + this + ", " + getTitle());
		header.add(new Label("title", getTitle()));
		System.out.println(":::: TRACE 3: " + this + ", " + getTitle());
		add(header);
		add(new FooterPanel("footer"));
	}

	abstract public String getTitle();


}
