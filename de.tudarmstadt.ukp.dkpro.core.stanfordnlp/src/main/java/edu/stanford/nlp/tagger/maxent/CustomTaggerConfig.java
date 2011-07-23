package edu.stanford.nlp.tagger.maxent;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;

public class CustomTaggerConfig
	extends TaggerConfig
{
	public CustomTaggerConfig(String... args)
	{
		super(args);
	}

	@Override
	DataInputStream getTaggerDataInputStream(String aModelFileOrUrl)
		throws IOException
	{
		return new DataInputStream(new BufferedInputStream(new URL(aModelFileOrUrl).openStream()));
	}
}
