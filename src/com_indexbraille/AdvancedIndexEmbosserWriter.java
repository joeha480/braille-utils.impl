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

import org.daisy.braille.impl.embosser.DotMapper;
import org.daisy.braille.impl.embosser.DotMapperConfiguration;
import org.daisy.braille.impl.embosser.ExtendedEmbosserWriter;
import org.daisy.braille.impl.embosser.InternalContract;
import org.daisy.braille.impl.embosser.InternalContractNotSupportedException;
import org.daisy.braille.impl.embosser.PageBreaks;
import org.daisy.braille.impl.embosser.StandardPageBreaks;
import org.daisy.braille.utils.api.embosser.Contract;
import org.daisy.braille.utils.api.embosser.ContractNotSupportedException;
import org.daisy.braille.utils.api.embosser.EmbosserWriterProperties;
import org.daisy.braille.utils.api.embosser.StandardLineBreaks;
import org.daisy.braille.utils.api.table.BrailleConverter;

/**
 * @author Joel Håkansson
 */
public class AdvancedIndexEmbosserWriter implements ExtendedEmbosserWriter {
	private static final byte[] linebreak = new StandardLineBreaks(StandardLineBreaks.Type.DOS).getString().getBytes();
	private final OutputStream os;
	private final byte[] header;
	private final byte[] footer;
	private final byte[] activateGraphcis;
	private final byte[] deactivateGraphcis;
	private final EmbosserWriterProperties props;
	private final DotMapper mapper;
	private int rowgap;
	private boolean isOpen;
	private boolean isClosed;
	private boolean currentDuplex;
	private int currentPage;
	private int charsOnRow;
	private double rowsOnPage;
	protected PageBreaks pagebreaks = new StandardPageBreaks();
	

	AdvancedIndexEmbosserWriter(OutputStream os, BrailleConverter bc, byte[] header, EmbosserWriterProperties props) {
		this.props = props;
		this.mapper = new DotMapper(props.getMaxWidth(), DotMapperConfiguration.builder()
				.baseCharacter('@')
				.cellHeight(4)
				.cellWidth(1)
				.map(new int[]{1,2,4,8})
				.build());
		this.isOpen = false;
		this.isClosed = false;
		this.header = header!=null?header:new byte[0];
		this.footer = new byte[0];
		this.activateGraphcis = new byte[]{0x1b, 0x07};
		this.deactivateGraphcis = new byte[]{0x1b, 0x06};
		this.os = os;
	}

	@Override
	public void open(boolean duplex) throws IOException {
		init(duplex);
		os.write(header);
	}

	@Override
	public void open(boolean duplex, Contract contract) throws IOException, ContractNotSupportedException {
		throw new ContractNotSupportedException("Only use this with a contract.");
	}
	
	@Override
	public void open(boolean duplex, InternalContract contract) throws IOException, InternalContractNotSupportedException {
		init(duplex);
		os.write(header);
	}
	
	private void init(boolean duplex) {
		charsOnRow = 0;
		rowsOnPage = 0;
		rowgap = 0;
		currentPage = 1;
		isOpen=true;
		currentDuplex = duplex;
	}
	
	@Override
	public void write(String braille) throws IOException {
		charsOnRow += braille.length();
		if (charsOnRow>props.getMaxWidth()) {
			throw new IOException("The maximum number of characters on a row was exceeded (page is too narrow).");
		}
		mapper.write(braille);
	}

	@Override
	public void newLine() throws IOException {
		mapper.newLine(rowgap);
		rowsOnPage+=1+rowgap/4d;
		charsOnRow = 0;
	}

	@Override
	public void setRowGap(int value) {
		if (value<0) {
			throw new IllegalArgumentException("Non negative integer expected.");
		} else {
			rowgap = value;
		}
	}

	@Override
	public int getRowGap() {
		return rowgap;
	}

	private void addAll(byte[] b) throws IOException {
		os.write(b);
	}

	@Override
	public void close() throws IOException {
		os.write(footer);
		os.close();
		isClosed=true;
		isOpen=false;
	}

	@Override
	public void newPage() throws IOException {
		if (props.supportsDuplex() && !currentDuplex && (currentPage % 2)==1) {
			formFeed();
		}
		formFeed();
	}

	@Override
	public void newSectionAndPage(boolean duplex) throws IOException {
		if (props.supportsDuplex() && (currentPage % 2)==1) {
			formFeed();
		}
		newPage();
		currentDuplex = duplex;
	}
	
	private void formFeed() throws IOException {
		if (charsOnRow>0) {
			mapper.newLine(0);//we'll use 0 here, because we don't need to enforce the space below the last line on the page
			rowsOnPage+=1+rowgap/4d;
		}
		boolean hasData = mapper.hasMoreLines();
		if (hasData) {
			addAll(activateGraphcis);
		}
		while (mapper.hasMoreLines()) {
			addAll(mapper.readLine(true).getBytes());
			addAll(linebreak);
		}
		if (hasData) {
			addAll(deactivateGraphcis);
		}
		if (rowsOnPage>props.getMaxHeight()) {
			throw new IOException("The maximum number of rows on a page was exceeded (page is too short): " + rowsOnPage);
		}
		addAll(pagebreaks.getString().getBytes());
		currentPage++;
		rowsOnPage = 0;
		charsOnRow = 0;
	}

	@Override
	public void newVolumeSectionAndPage(boolean duplex) throws IOException {
		newSectionAndPage(duplex);
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public boolean isClosed() {
		return isClosed;
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
