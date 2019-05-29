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
package se.inera.axel.shs.broker.webconsole.directory;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;

import javax.inject.Inject;
import javax.inject.Named;

public class ActorEditFormPanel extends Panel {

    @Inject
	@Named("directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	public ActorEditFormPanel(String id, PageParameters params) {
		super(id);

		add(new FeedbackPanel("feedback"));
		
		final String orgNumber = params.get("orgNumber").toString();
		if (StringUtils.isNotBlank(orgNumber)) {
			Organization organization = getDirectoryAdminService().getOrganization(orgNumber);
			IModel<Organization> actorModel = new CompoundPropertyModel<Organization>(organization);
			Form<Organization> form = new Form<Organization>("actorForm", actorModel) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit() {
					super.onSubmit();
					Organization organization = getModelObject();
					getDirectoryAdminService().saveActor(organization);
					PageParameters params = new PageParameters();
					params.add("orgNumber", orgNumber);
					setResponsePage(ActorPage.class, params);
				}

			};

			TextField<String> name = new TextField<String>("orgName");
			name.setRequired(true);
			form.add(name);
			TextField<String> orgno = new TextField<String>("orgNumber");
			orgno.setRequired(true);
			form.add(orgno);

			form.add(new TextField<String>("streetAddress"));
			form.add(new TextField<String>("postalCode"));
			form.add(new TextField<String>("postalAddress"));
			form.add(new TextField<String>("postOfficeBox"));
			form.add(new TextField<String>("description"));
			form.add(new TextField<String>("phoneNumber"));
			form.add(new TextField<String>("faxNumber"));
			form.add(new TextField<String>("labeledUri"));

			form.add(new SubmitLink("submit"));
			PageParameters cancelParams = new PageParameters();
			cancelParams.add("orgNumber", orgNumber);
			form.add(new BookmarkablePageLink<Void>("cancel", ActorPage.class,
					cancelParams));

			add(form);
		}
	}

    private DirectoryAdminService getDirectoryAdminService() {
        return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);
    }

    private static final long serialVersionUID = 1L;

}
