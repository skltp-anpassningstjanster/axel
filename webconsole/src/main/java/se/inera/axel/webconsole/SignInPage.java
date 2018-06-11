package se.inera.axel.webconsole;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.string.Strings;

public class SignInPage extends WebPage {

	private static final long serialVersionUID = 1743741632382206814L;
	private String username;
	   private String password;
	   @Override
	   protected void onInitialize() {
	      super.onInitialize();

	      StatelessForm<?> form = new StatelessForm<Object>("form"){

			private static final long serialVersionUID = -6760391241629225824L;

			@Override
	         protected void onSubmit() {
	            if(Strings.isEmpty(username))
	               return;

	            boolean authResult = AuthenticatedWebSession.get().signIn(username, password);
	            //if authentication succeeds redirect user to the requested page
	            if(authResult)
	               continueToOriginalDestination();
	         }
	      };

	      form.setDefaultModel(new CompoundPropertyModel<SignInPage>(this));

	      form.add(new TextField<Object>("username"));
	      form.add(new PasswordTextField("password"));

	      add(form);
	   }
	}
