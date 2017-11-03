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
package se.inera.axel.riv.webconsole.base;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;

public class ControlGroupContainer extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	public ControlGroupContainer(final FormComponent<?> field) {
		super("control." + field.getId());
		
		add(new Behavior() {

			private static final long serialVersionUID = 2013166880406979130L;

			@Override
			public void onComponentTag(Component component, ComponentTag tag) {
				if (!field.isValid()) {
					tag.append("class", "error", " ");
				}
			}
		});
		
		add(field);
	}
}
