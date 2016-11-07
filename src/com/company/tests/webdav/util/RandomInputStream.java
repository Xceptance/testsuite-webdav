package com.company.tests.webdav.util;

/*
 * Copyright WizTools.org
 * Licensed under the Apache License, Version 2.0:
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Modified by Rene Schwietzke, Xceptance GmbH
 */

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.util.XltRandom;

/**
 * Originally written by Elliotte Rusty Harold for the book Java I/O 2nd
 * edition.
 * 
 * @author subwiz
 * @author rschwietzke
 */
public class RandomInputStream extends InputStream
{
	// shall we do text or binary? we will get 32 to 125 ascii for text
	private final boolean textContent;
					
	// stream was closed							
	private boolean closed = false;
	
	// maximum size to generate
	private final int targetSize;
	
	// generated so far
	private int size = 0;
	
	public RandomInputStream(final int targetSize, final boolean textContent)
	{
		super();
		this.textContent = textContent;
		this.targetSize = targetSize;
	}

	@Override
	public int read() throws IOException
	{
		checkOpen();
		
		// size reached
		if (size >= targetSize)
		{
			return -1;
		}
			
		size++;
		
		return getData();
	}
	
	private int getData()
	{
		if (textContent)
		{
			return XltRandom.nextInt(32, 127);
		}
		else
		{
			return XltRandom.nextInt(0, 256);
		}
	}

	@Override
	public int read(byte[] data, int offset, int length) throws IOException
	{
		checkOpen();

		// size reached
		if (size >= targetSize)
		{
			return -1;
		}
				
		byte[] temp = new byte[Math.min(length, targetSize - size)];
	
		for (int i = 0; i < length; i++)
		{
			temp[i] = (byte) getData();
		}
		
		System.arraycopy(temp, 0, data, offset, temp.length);
		
		size += temp.length;
		
		return length;
	}

	@Override
	public int read(byte[] data) throws IOException
	{
		return read(data, 0, data.length);

	}

	@Override
	public long skip(long bytesToSkip) throws IOException
	{
		checkOpen();

		// It's all random so skipping has no effect.
		return bytesToSkip;
	}

	@Override
	public void close()
	{
		this.closed = true;
	}

	private void checkOpen() throws IOException
	{
		if (closed)
		{
			throw new IOException("Input stream closed");
		}
	}

	@Override
	public int available()
	{
		// Limited only by available memory and the size of an array.
		return targetSize - size;
	}
}