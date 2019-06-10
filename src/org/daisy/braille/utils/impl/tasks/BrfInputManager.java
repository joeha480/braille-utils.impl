package org.daisy.braille.utils.impl.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.embosser.EmbosserCatalogService;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.daisy.streamline.api.tasks.TaskSystemException;

class BrfInputManager implements TaskGroup {
	private final TaskGroupSpecification specification;
	private final TableCatalogService tableCatalog;
	private final EmbosserCatalogService embosserCatalog;

	BrfInputManager(TaskGroupSpecification specification, TableCatalogService tableCatalog, EmbosserCatalogService embosserCatalog) {
		this.specification = specification;
		this.tableCatalog = tableCatalog;
		this.embosserCatalog = embosserCatalog;
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<>();
		if ("brf".equalsIgnoreCase(specification.getInputType().getIdentifier())) {
			ret.add(new Brf2PefTask(Messages.TITLE_BRF_TO_PEF.localize(), parameters, tableCatalog));
		} else if ("pef".equalsIgnoreCase(specification.getInputType().getIdentifier())) {
			ret.add(new Pef2BrfTask("NAME?", parameters, tableCatalog, embosserCatalog));
		}
		return ret;
	}

}