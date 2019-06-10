package org.daisy.braille.utils.impl.tasks;

import java.util.stream.Stream;

import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.streamline.api.option.UserOptionValue;

class TaskUtils {
	
	static Stream<UserOptionValue> listAsUserOptionValues(TableCatalogService catalog) {
		return catalog.list().stream()
			.sorted((v1, v2)->v1.getDisplayName().compareTo(v2.getDisplayName()))
			.map(v -> new UserOptionValue.Builder(v.getIdentifier())
					.displayName(v.getDisplayName())
					.description(v.getDescription())
					.build()
			);
	}

}
