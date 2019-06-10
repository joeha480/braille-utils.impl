package org.daisy.braille.utils.impl.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.braille.utils.pef.TextHandler;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.option.UserOptionValue;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;

class Brf2PefTask extends ReadWriteTask {
	private final Map<String, String> params;
	private final TableCatalogService tableCatalog;
	private static List<UserOption> options = null;

	Brf2PefTask(String name, Map<String, Object> params, TableCatalogService tableCatalog) {
		super(name);
		this.tableCatalog = tableCatalog;
		this.params = new HashMap<>();
		for (Map.Entry<String, Object> v : params.entrySet()) {
			switch (v.getKey()) {
				case TextHandler.KEY_AUTHOR:
				case TextHandler.KEY_DATE:
				case TextHandler.KEY_DUPLEX:
				case TextHandler.KEY_IDENTIFIER:
				case TextHandler.KEY_LANGUAGE:
				case TextHandler.KEY_MODE:
				case TextHandler.KEY_TITLE:
					this.params.put(v.getKey(), v.getValue().toString());
					break;
				default:
					break;
			}
		}
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {
			TextHandler.with(input.getPath().toFile(), output, tableCatalog)
					.options(params)
					.parse();
		} catch (IOException e) {
			throw new InternalTaskException(e);
		}
		return new DefaultAnnotatedFile.Builder(output.toPath()).extension("pef").mediaType("application/x-pef+xml").build();
	}

	@Override
	@Deprecated
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public List<UserOption> getOptions() {
		if (options==null) {
			options = new ArrayList<>();
			options.add(new UserOption.Builder(TextHandler.KEY_AUTHOR).displayName(Messages.LABEL_AUTHOR.localize()).build());
			options.add(new UserOption.Builder(TextHandler.KEY_DATE).displayName(Messages.LABEL_DATE.localize()).build());
			options.add(new UserOption.Builder(TextHandler.KEY_DUPLEX)
					.displayName(Messages.LABEL_DUPLEX.localize())
					.addValue(new UserOptionValue.Builder("true").displayName(Messages.BOOLEAN_ON.localize()).build())
					.addValue(new UserOptionValue.Builder("false").displayName(Messages.BOOLEAN_OFF.localize()).build())
					.defaultValue("true")
					.build());
			options.add(new UserOption.Builder(TextHandler.KEY_IDENTIFIER).displayName(Messages.LABEL_IDENTIFIER.localize()).build());
			options.add(new UserOption.Builder(TextHandler.KEY_LANGUAGE).displayName(Messages.LABEL_LANGUAGE.localize()).build());
			UserOption.Builder mode = new UserOption.Builder(TextHandler.KEY_MODE)
					.displayName(Messages.LABEL_TABLE.localize());
			TaskUtils.listAsUserOptionValues(tableCatalog)
			.forEach(mode::addValue);
			options.add(mode.build());
			options.add(new UserOption.Builder(TextHandler.KEY_TITLE).displayName(Messages.LABEL_TITLE.localize()).build());
		}
		return options;
	}

}
