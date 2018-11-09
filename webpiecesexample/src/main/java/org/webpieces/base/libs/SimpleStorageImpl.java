package org.webpieces.base.libs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.webpieces.router.api.SimpleStorage;

public class SimpleStorageImpl implements SimpleStorage {
	
	private EntityManagerFactory factory;

	@Inject
	public SimpleStorageImpl(EntityManagerFactory factory) {
		this.factory = factory;
	}

	@Override
	public CompletableFuture<Void> save(String key, String subKey, String value) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		mgr.persist(new SimpleStorageDbo(key, subKey, value));
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> save(String key, Map<String, String> properties) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		properties.forEach((mapKey, value) ->
			mgr.persist(new SimpleStorageDbo(key, mapKey, value))
		);
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Map<String, String>> read(String key) {
		EntityManager mgr = factory.createEntityManager();
		//mgr.getTransaction().begin();
		
		List<SimpleStorageDbo> rows = SimpleStorageDbo.findAll(mgr, key);
		Map<String, String> properties = new HashMap<>();
		for(SimpleStorageDbo row: rows) {
			properties.put(row.getMapKey(), row.getValue());
		}
		
		mgr.close();
		
		return CompletableFuture.completedFuture(properties);
	}

	@Override
	public CompletableFuture<Void> delete(String key) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		List<SimpleStorageDbo> rows = SimpleStorageDbo.findAll(mgr, key);
		for(SimpleStorageDbo row: rows) {
			mgr.remove(row);
		}
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> delete(String key, String subKey) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		SimpleStorageDbo row = SimpleStorageDbo.find(mgr, key, subKey);
		if(row != null)
			mgr.remove(row);
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return CompletableFuture.completedFuture(null);
	}
}
