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

import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.pages.SignInPage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import se.inera.axel.riv.webconsole.RivShsServiceMappingEditPage;
import se.inera.axel.riv.webconsole.RivShsServiceMappingsPage;


public class WicketApplication extends AuthenticatedWebApplication {

	@Override
	public Class<RivShsServiceMappingsPage> getHomePage() {
		return RivShsServiceMappingsPage.class;
	}

	@Override
	public void init() {
        super.init();
        SpringComponentInjector injector = new SpringComponentInjector(this);
        getComponentInstantiationListeners().add(injector);

        mountPage("/mappings", RivShsServiceMappingsPage.class);
        mountPage("/mappings/edit", RivShsServiceMappingEditPage.class);

	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass(){
		return BasicAuthenticationSession.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return SignInPage.class;
	}

}
