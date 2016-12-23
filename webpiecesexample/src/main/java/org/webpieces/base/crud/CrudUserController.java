package org.webpieces.base.crud;

import static org.webpieces.base.crud.CrudUserRouteId.GET_ADD_USER_FORM;
import static org.webpieces.base.crud.CrudUserRouteId.GET_EDIT_USER_FORM;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.hibernate.Em;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.FlashAndRedirect;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import org.webpieces.base.libs.UserDbo; 

@Singleton
public class CrudUserController {

	private static Logger log = LoggerFactory.getLogger(CrudUserController.class);
	
	public Action userList() {
		EntityManager mgr = Em.get();
		Query query = mgr.createNamedQuery("findAllUsers");
		@SuppressWarnings("unchecked")
		List<UserDbo> users = query.getResultList();
		return Actions.renderThis("users", users);
	}
	
	public Action userAddEdit(Integer id) {
		if(id == null) {
			return Actions.renderThis("entity", new UserDbo());
		}
		
		UserDbo user = Em.get().find(UserDbo.class, id);
		return Actions.renderThis("entity", user);
	}

	public Redirect postSaveUser(UserDbo entity, String password) {
		//TODO: if we wire in JSR303 bean validation into the platform, it could be 
		//done there as well though would
		//need to figure out how to do i18n for the messages in that case
		if(password == null) {
			Current.validation().addError("password", "password is required");
		} else if(password.length() < 4) {
			Current.validation().addError("password", "Value is too short");
		}
		
		if(entity.getFirstName() == null) {
			Current.validation().addError("entity.firstName", "First name is required");
		} else if(entity.getFirstName().length() < 3) {
			Current.validation().addError("entity.firstName", "First name must be more than 2 characters");
		}

		//all errors are grouped and now if there are errors redirect AND fill in
		//the form with what the user typed in along with errors
		if(Current.validation().hasErrors()) {
			log.info("page has errors");
			FlashAndRedirect redirect = new FlashAndRedirect(Current.getContext(), "Errors in form below");
			redirect.setSecureFields("entity.password"); //make sure secure fields are not put in flash cookie!!!
			redirect.setIdFieldAndValue("id", entity.getId());
			return Actions.redirectFlashAll(GET_ADD_USER_FORM, GET_EDIT_USER_FORM, redirect);
		}
		
		Current.flash().setMessage("User successfully saved");
		Current.flash().keep();
		
		Em.get().merge(entity);
        Em.get().flush();
        
		return Actions.redirect(CrudUserRouteId.LIST_USERS);
	}

	public Render confirmDeleteUser(int id) {
		UserDbo user = Em.get().find(UserDbo.class, id);
		return Actions.renderThis("entity", user);
	}
	
	public Redirect postDeleteUser(int id) {
		UserDbo ref = Em.get().getReference(UserDbo.class, id);
		Em.get().remove(ref);
		Em.get().flush();
		Current.flash().setMessage("User deleted");
		Current.flash().keep();
		return Actions.redirect(CrudUserRouteId.LIST_USERS);
	}
}
