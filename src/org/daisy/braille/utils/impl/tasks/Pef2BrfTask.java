package org.daisy.braille.utils.impl.tasks;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.utils.pef.PEFConverterFacade;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.daisy.dotify.api.embosser.EmbosserCatalogService;
import org.daisy.dotify.api.embosser.EmbosserFactoryException;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;
import org.xml.sax.SAXException;

class Pef2BrfTask extends ReadWriteTask {
	private final Map<String, String> params;
	private final TableCatalogService tableCatalog;
	private final EmbosserCatalogService embosserCatalog;
	private static List<UserOption> options = null;
	
	Pef2BrfTask(String name, Map<String, Object> params, TableCatalogService tableCatalog, EmbosserCatalogService embosserCatalog) {
		super(name);
		this.tableCatalog = tableCatalog;
		this.embosserCatalog = embosserCatalog;
		this.params = new HashMap<>();
		for (Map.Entry<String, Object> v : params.entrySet()) {
			// Filter the parameters, as the PEFConverterFacade throws an error if it gets something it doesn't support.
			switch (v.getKey()) {
				case PEFConverterFacade.KEY_ALIGN:
				case PEFConverterFacade.KEY_ALIGNMENT_OFFSET:
				case PEFConverterFacade.KEY_BREAKS:
				case PEFConverterFacade.KEY_CELL_HEIGHT:
				case PEFConverterFacade.KEY_CELL_WIDTH:
				case PEFConverterFacade.KEY_EMBOSSER:
				case PEFConverterFacade.KEY_FALLBACK:
				case PEFConverterFacade.KEY_PADDING:
				case PEFConverterFacade.KEY_RANGE:
				case PEFConverterFacade.KEY_REPLACEMENT:
				case PEFConverterFacade.KEY_TABLE:
					this.params.put(v.getKey(), v.getValue().toString());
					break;
				default: 
					break;
			}
		}
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input.toPath()).build(), output);
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try (OutputStream os = Files.newOutputStream(output.toPath())) {
			new PEFConverterFacade(embosserCatalog).parsePefFile(input.getPath().toFile(), os, null, params);
			return new DefaultAnnotatedFile.Builder(output.toPath())
					.extension("brf")
					.mediaType("text/plain")
					.build();
		} catch (IOException | NumberFormatException | ParserConfigurationException | SAXException | EmbosserFactoryException | UnsupportedWidthException e) {
			throw new InternalTaskException(e);
		}
	}
	
	@Override
	public List<UserOption> getOptions() {
		if (options==null) {
			options = new ArrayList<>();
			UserOption.Builder table = new UserOption.Builder(PEFConverterFacade.KEY_TABLE)
					.displayName(Messages.LABEL_TABLE.localize());
			TaskUtils.listAsUserOptionValues(tableCatalog)
				.forEach(table::addValue);
			options.add(table.build());
		}
		return options;
	}
}
