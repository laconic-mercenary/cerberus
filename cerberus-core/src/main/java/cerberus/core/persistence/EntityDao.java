package cerberus.core.persistence;

import java.io.Serializable;
import java.util.List;

public interface EntityDao<T extends Serializable, KEY> extends Serializable, AutoCloseable {

	T findByID(KEY key);
	
	void remove(T item);
	
	void insert(T item);
	
	void update(T item);
	
	List<T> findAll();
	
}
