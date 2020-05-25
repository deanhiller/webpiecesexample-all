package org.webpieces;

import org.webpieces.db.DbSettingsInMemory;
import org.webpieces.services.DevConfig;

public class OurDevConfig implements DevConfig {

	@Override
	public String[] getExtraArguments() {
		return null;
	}

	@Override
	public String getHibernateSettingsClazz() {
		return DbSettingsInMemory.class.getName();
	}

	@Override
	public int getHttpsPort() {
		return 8443;
	}

	@Override
	public int getHttpPort() {
		return 8080;
	}

}
