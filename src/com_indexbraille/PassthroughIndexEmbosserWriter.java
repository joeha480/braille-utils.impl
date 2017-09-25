/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com_indexbraille;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.impl.embosser.ConfigurableEmbosser;
import org.daisy.braille.impl.embosser.ExtendedEmbosserWriter;
import org.daisy.braille.impl.embosser.InternalContract;
import org.daisy.braille.impl.embosser.InternalContract.BrailleRange;
import org.daisy.braille.impl.embosser.InternalContractNotSupportedException;
import org.daisy.braille.utils.api.embosser.Contract;
import org.daisy.braille.utils.api.embosser.ContractNotSupportedException;
import org.daisy.braille.utils.api.embosser.EmbosserWriter;
import org.daisy.braille.utils.api.embosser.EmbosserWriterProperties;
import org.daisy.braille.utils.api.embosser.StandardLineBreaks;
import org.daisy.braille.utils.api.table.BrailleConverter;

/**
 * @author Joel Håkansson
 */
public class PassthroughIndexEmbosserWriter implements ExtendedEmbosserWriter {
	private static final Logger logger = Logger.getLogger(PassthroughIndexEmbosserWriter.class.getCanonicalName());
	private final OutputStream os;
	private final BrailleConverter bc;
	private final IndexHeader header;
	private final byte[] footer;
	private final EmbosserWriterProperties props;
	private EmbosserWriter writer;

	PassthroughIndexEmbosserWriter(OutputStream os, BrailleConverter bc, IndexHeader header, EmbosserWriterProperties props) {
		this.os = os;
		this.bc = bc;
		this.header = header;
		this.footer = new byte[0];
		this.props = props;
		this.writer = null;
	}

	@Override
	public void open(boolean duplex) throws IOException {
		throw new IOException("Only use this with an internal contract.");
	}

	@Override
	public void open(boolean duplex, Contract contract) throws IOException, ContractNotSupportedException {
		throw new ContractNotSupportedException("Only use this with an internal contract.");
	}

	@Override
	public void open(boolean duplex, InternalContract contract) throws IOException, InternalContractNotSupportedException {
		// Because it isn't possible to ...
		if (contract.getBrailleRange()==BrailleRange.SIX_DOT) { // && contract.onlySimpleRowgaps()
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Using the configurable embosser.");
			}
			writer = new ConfigurableEmbosser.Builder(os, bc)
				.breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
				.padNewline(ConfigurableEmbosser.Padding.NONE)
				.footer(footer)
				.embosserProperties(props)
				.header(header.getIndexHeader(false))
				.build();
			writer.open(duplex);
		} else { // if (contract.getBrailleRange()==BrailleRange.EIGHT_DOT && contract.onlySimpleRowgaps())
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Using transparent embosser.");
			}
			writer = new IndexTransparentEmbosserWriter(os, bc, header.getIndexHeader(true), footer, props);
			writer.open(duplex);
		}
	}

	@Override
	public void write(String braille) throws IOException {
		writer.write(braille);
	}

	@Override
	public void newLine() throws IOException {
		writer.newLine();
	}

	@Override
	public void setRowGap(int value) {
		writer.setRowGap(value);
	}

	@Override
	public int getRowGap() {
		return writer.getRowGap();
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void newPage() throws IOException {
		writer.newPage();
	}

	@Override
	public void newSectionAndPage(boolean duplex) throws IOException {
		writer.newSectionAndPage(duplex);
	}

	@Override
	public void newVolumeSectionAndPage(boolean duplex) throws IOException {
		writer.newVolumeSectionAndPage(duplex);
	}

	@Override
	public boolean isOpen() {
		return writer.isOpen();
	}

	@Override
	public boolean isClosed() {
		return writer.isClosed();
	}

	@Override
	public int getMaxHeight() {
		return props.getMaxHeight();
	}

	@Override
	public int getMaxWidth() {
		return props.getMaxWidth();
	}

	@Override
	public boolean supports8dot() {
		return props.supports8dot();
	}

	@Override
	public boolean supportsAligning() {
		return props.supportsAligning();
	}

	@Override
	public boolean supportsDuplex() {
		return props.supportsDuplex();
	}

	@Override
	public boolean supportsVolumes() {
		return props.supportsVolumes();
	}

	@Override
	public boolean supportsZFolding() {
		return props.supportsZFolding();
	}

	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return props.supportsPrintMode(mode);
	}

}
