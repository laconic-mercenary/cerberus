package cerberus.app.inc;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.EntityDao;
import cerberus.core.persistence.entities.KeyValuePairProperty;

// view scoped bean exclusively used with the 
// includable kvp-properties-table.xhtml 
@Named
@ViewScoped
public class AppPropertiesModel implements Serializable {

	private static final long serialVersionUID = -795198939646982692L;

	private static final Logger LOGGER = Logger
			.getLogger(AppPropertiesModel.class);

	public List<KeyValuePairProperty> getProperties() {
		return queryProperties();
	}

	private List<KeyValuePairProperty> queryProperties() {
		List<KeyValuePairProperty> result = Collections.emptyList();
		try (EntityDao<KeyValuePairProperty, ?> dm = DaoManager.Factory
				.connect(KeyValuePairProperty.class)) {
			result = dm.findAll();
		} catch (Exception e) {
			LOGGER.error("Failed to query for all KvpProperties (populating data table)");
			e.printStackTrace();
		}
		return result;
	}
}
