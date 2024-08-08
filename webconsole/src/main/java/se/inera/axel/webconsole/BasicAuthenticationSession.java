package se.inera.axel.webconsole;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.Request;
import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.riv.authentication.LoginService;

import javax.inject.Inject;
import javax.inject.Named;

public class BasicAuthenticationSession extends AuthenticatedWebSession {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasicAuthenticationSession.class);
	private static final long serialVersionUID = 4391047631182868491L;


	public BasicAuthenticationSession(Request request) {
		super(request);
		Injector.get().inject(this);
	}

	@Inject
	@Named("loginService")
	@SpringBean(name = "loginService")
	private LoginService loginService;


	@Override
	public boolean authenticate(String username, String password) {
		if(username == null || password == null)
			return false;

		return loginService.authenticate(username, password);
	}

	
	@Override
	public Roles getRoles() {
		return null;
	}
}