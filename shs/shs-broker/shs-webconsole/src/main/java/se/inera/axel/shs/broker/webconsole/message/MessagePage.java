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
package se.inera.axel.shs.broker.webconsole.message;

import org.apache.wicket.Application;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.webconsole.base.BasePage;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * List LDAP Directory
 */
@PaxWicketMountPoint(mountPoint = "/shs/message/view")
public class MessagePage extends BasePage {
	private static final long serialVersionUID = 1L;

    @Inject
    @Named("messageLogAdminService")
    @SpringBean(name = "messageLogAdminService")
    MessageLogAdminService messageLogAdminService;

	public MessagePage(final PageParameters parameters) {
		super(parameters);

        final String messageId = parameters.get("messageId").toString();


        MessageModel model = new MessageModel(messageLogAdminService, messageId);

        add(new StatusPanel("status", model));
		add(new MessageViewPanel("message", model));
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
