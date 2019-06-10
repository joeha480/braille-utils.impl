package org.daisy.braille.utils.impl.tasks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.embosser.EmbosserCatalog;
import org.daisy.dotify.api.embosser.EmbosserCatalogService;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupFactory;
import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Provides a task group factory for brf input.
 * @author Joel Håkansson
 */
@Component
public class BrfInputManagerFactory implements TaskGroupFactory {
	private final Set<TaskGroupInformation> information;
	private TableCatalogService tableCatalog;
	private EmbosserCatalogService embosserCatalog;
	
	/**
	 * Creates a new text input manager factory.
	 */
	public BrfInputManagerFactory() {

		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder("brf", "pef").build());
		tmp.add(TaskGroupInformation.newConvertBuilder("pef", "brf").build());
		information = Collections.unmodifiableSet(tmp);
	}
	
	@Override
	public void setCreatedWithSPI() {
		tableCatalog = TableCatalog.newInstance();
		embosserCatalog = EmbosserCatalog.newInstance();
	}
	
	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setTableCatalog(TableCatalogService service) {
		this.tableCatalog = service;
	}
	
	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetTableCatalog(TableCatalogService service) {
		this.tableCatalog = null;
	}
	
	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setEmbosserCatalog(EmbosserCatalogService service) {
		this.embosserCatalog = service;
	}
	
	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetEmbosserCatalog(EmbosserCatalogService service) {
		this.embosserCatalog = null;
	}
	
	@Override
	public boolean supportsSpecification(TaskGroupInformation spec) {
		return listAll().contains(spec);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new BrfInputManager(spec, tableCatalog, embosserCatalog);
	}

	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
	}

}
