package org.webpieces.base;

import org.webpieces.router.api.ObjectStringConverter;
import org.webpieces.router.api.SimpleStorage;
import org.webpieces.router.api.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import org.webpieces.base.libs.EducationEnum;
import org.webpieces.base.libs.RemoteService;
import org.webpieces.base.libs.RemoteServiceImpl;
import org.webpieces.base.libs.RoleEnum;
import org.webpieces.base.libs.SimpleStorageImpl;
import org.webpieces.base.libs.SomeLibrary;
import org.webpieces.base.libs.SomeLibraryImpl;

public class GuiceModule implements Module {

	//This is where you would put the guice bindings you need though generally if done
	//right, you won't have much in this file.
	
	//If you need more Guice Modules as you want to scale, just modify WebpiecesExampleMeta which returns
	//the list of all the Guice Modules in your application
	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Binder binder) {
		//all modules have access to adding their own Startable objects to be run on server startup
		Multibinder<Startable> uriBinder = Multibinder.newSetBinder(binder, Startable.class);
	    uriBinder.addBinding().to(PopulateDatabase.class);

		Multibinder<ObjectStringConverter> conversionBinder = Multibinder.newSetBinder(binder, ObjectStringConverter.class);
		conversionBinder.addBinding().to(EducationEnum.WebConverter.class);
		conversionBinder.addBinding().to(RoleEnum.WebConverter.class);
	    
		binder.bind(SimpleStorage.class).to(SimpleStorageImpl.class);
		
	    binder.bind(SomeLibrary.class).to(SomeLibraryImpl.class);
	    binder.bind(RemoteService.class).to(RemoteServiceImpl.class);
	}

}
