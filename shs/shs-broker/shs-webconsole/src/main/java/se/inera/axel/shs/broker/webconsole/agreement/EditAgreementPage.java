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
package se.inera.axel.shs.broker.webconsole.agreement;

import org.apache.wicket.Application;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

import se.inera.axel.shs.broker.webconsole.base.BasePage;

@PaxWicketMountPoint(mountPoint = "/shs/agreement/edit")
public class EditAgreementPage extends BasePage {
	private static final long serialVersionUID = 1L;

	public EditAgreementPage(final PageParameters parameters) {
		super(parameters);

		Panel panel = null;
		String viewtype = parameters.get("view").toString();
		if (viewtype != null && viewtype.equals("xml")) {
			panel = new AgreementXmlPanel("agreementPanel", parameters);
		} else {
			panel = new AgreementFormPanel("agreementPanel", parameters);
		}
		add(panel);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		Application app = Application.get();
		if(app instanceof  AuthenticatedWebApplication){
			AuthenticatedWebApplication myApp = (AuthenticatedWebApplication) Application.get();
			//if user is not signed in, redirect him to sign in page
			if(!AuthenticatedWebSession.get().isSignedIn())
				myApp.restartResponseAtSignInPage();
		}
	}


}
