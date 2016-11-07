package com.company.tests.webdav;

import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.company.tests.webdav.util.RandomInputStream;
import com.github.sardine.DavResource;
import com.xceptance.xlt.api.data.GeneralDataProvider;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.webdav.actions.WebDAVConnect;
import com.xceptance.xlt.webdav.actions.WebDAVCopy;
import com.xceptance.xlt.webdav.actions.WebDAVCreateDirectory;
import com.xceptance.xlt.webdav.actions.WebDAVDelete;
import com.xceptance.xlt.webdav.actions.WebDAVExists;
import com.xceptance.xlt.webdav.actions.WebDAVGet;
import com.xceptance.xlt.webdav.actions.WebDAVList;
import com.xceptance.xlt.webdav.actions.WebDAVMove;
import com.xceptance.xlt.webdav.actions.WebDAVPut;
import com.xceptance.xlt.webdav.util.ListSelector;
import com.xceptance.xlt.webdav.util.WebDAVContext;

/**
 * Basic WebDAV test scenario. It demonstrates how to
 * <ul>
 * <li>configure access to the WebDAV server</li>
 * <li>list the contents of a directory</li>
 * <li>select specific or random resources from a listing</li>
 * <li>upload and download files</li>
 * <li>create directories on the server</li>
 * <li>move resources to another directory</li>
 * <li>delete resources from the server</li>
 * </ul>
 *
 * @author Karsten Sommer (Xceptance Software Technologies GmbH)
 */
public class TWebDAV extends AbstractTestCase
{
    /**
     * Executes the WebDAV demo scenario.
     * 
     * @throws Throwable
     *             if something went wrong on the network or in case of unexpected results
     */
    @Test
    public void test() throws Throwable
    {
    	// our unique id to create a clean directory for testing
    	final String uniqueName = Session.getCurrent().getUserID();
    	
    	//  our root folder
    	final String ROOT = "/Temp/";
    	
    	// *********************************************************************
    	// Build up the connection and make sure the root folder responds,
    	// even if this would yield a 404, the connection would be established
    	// but of course, the 404 assertion fail this test already
    	new WebDAVConnect(
    			"https://staging-performance.xceptance.com", 
    			"/on/webdav/Sites" + ROOT,
    			"user", "password").run();
    	
    	// *********************************************************************
    	// At the beginning we create an action.
        // This will check our webdav directory and this should be named "CheckResourcePath Home" in your test results.
        // Alternatively we can use a constructor without timer name,
        // this will give our action the name of its class CheckResourcePath in the results.
        final WebDAVList root = new WebDAVList(".", 1).timerName("List Root");
        root.run();

        // Now we want to get a file out of the results. So let us select one out of the results.
        // Get the results from the previous performed action
        final List<DavResource> results = root.getResources();

        // *********************************************************************
        // We select all files by using the ListSelector utility.
        // This will return a new list.
        final List<DavResource> dirs = ListSelector.getAllDirectories(results);
        
        // let us check if we have our test dir, if yes, get rid off it
        for (final DavResource dir : dirs)
        {
        	if (dir.getName().equals(uniqueName))
        	{
        		// remove the user dir 
                // Finally we delete our directory, to prepare following iterations of our test case
                new WebDAVDelete(dir).timerName("Delete Root").run();

                // And check it is gone
                new WebDAVExists(dir, false).run();
        	}
        }

        // *********************************************************************
        // create the dir again
        final String USERROOT = uniqueName;
        new WebDAVCreateDirectory(USERROOT).timerName("CreateDirectory UserRoot").run();
        
        // *********************************************************************
        new WebDAVExists(USERROOT, true).timerName("Exists UserRoot").run();

        // *********************************************************************
        // Upload/Put
        final byte[] randomFileContent = new byte[1024 * 10];
        XltRandom.nextBytes(randomFileContent);
        
        final String randomFilePath1 = USERROOT + "/" + "random1.bin";
        new WebDAVPut(randomFilePath1, randomFileContent).timerName("Put Bin 10kB").run();
        
        // *********************************************************************
        // Copy
        final String randomFilePath2 = USERROOT + "/" + "random2.bin";
        new WebDAVCopy(randomFilePath1, randomFilePath2).timerName("Copy 10kB").run();

        // *********************************************************************
        // Download/Get
        final WebDAVGet get = new WebDAVGet(randomFilePath2, true).timerName("Get 10kB");
        get.run();
        
        Assert.assertArrayEquals(randomFileContent, get.getFileContent());
        
        // *********************************************************************
        // Delete
        new WebDAVDelete(randomFilePath2).timerName("Delete 10k").run();
        new WebDAVExists(randomFilePath2, false).timerName("Exists").run();

        // *********************************************************************
        // Move
        final String randomFilePath3 = USERROOT + "/" + "random3.bin";
        new WebDAVMove(randomFilePath1, randomFilePath3).timerName("Move").run();
        new WebDAVExists(randomFilePath1, false).timerName("Exists").run();
        new WebDAVExists(randomFilePath3, true).timerName("Exists").run();
        
        // *********************************************************************
        // Upload/Put a large file
        final String largeFilePath1 = USERROOT + "/" + "large.bin";
        new WebDAVPut(largeFilePath1, new RandomInputStream(1024 * 1024 * 10, false)).timerName("Put Bin 10MB").run();
        new WebDAVExists(largeFilePath1, true).timerName("Exists").run();
        
        final String largeFilePath2 = USERROOT + "/" + "large.txt";
        new WebDAVPut(largeFilePath2, new RandomInputStream(1024 * 1024 * 10, true)).timerName("Put Text 10MB").run();
        new WebDAVExists(largeFilePath2, true).timerName("Exists").run();
        
        // *********************************************************************
        // Get the large files
        new WebDAVGet(largeFilePath1, false).timerName("Get Bin 10MB").run();
        new WebDAVGet(largeFilePath2, false).timerName("Get Text 10MB").run();

        // ok, create a fancy directory
        // *********************************************************************
        // directories
        final String baseDirName = USERROOT + "/" + "Dir";
        final int DIRCOUNT = 5;
        
        final int TEXTSIZE = 1024 * 10;
        String text = GeneralDataProvider.getInstance().getText(50, 100, false);
        while (text.length() < TEXTSIZE)
        {
        	text = text + GeneralDataProvider.getInstance().getText(5, 10, false);
        }
        final byte[] CODECONTENT = StringUtils.getBytesUtf8(org.apache.commons.lang3.StringUtils.substring(text, 0, TEXTSIZE));
        
        
        final int BINARYSIZE = 1024 * 5;
        final byte[] BINARYCONTENT = new byte[BINARYSIZE];
        XltRandom.nextBytes(BINARYCONTENT);
        
        for (int i = 0; i < DIRCOUNT; i++)
        {
        	final String dirPath = baseDirName + i;
        	new WebDAVCreateDirectory(dirPath).timerName("CreateDir L2").run();
            new WebDAVExists(dirPath, true).timerName("Exists").run();

            // files per dir
            final int FILECOUNT = 10; // we get two per loop, txt and bin
            for (int f = 0; f < FILECOUNT; f++)
            {
            	final String filePath = baseDirName + i + "/" + "file" + f + ".txt";
            	new WebDAVPut(filePath, CODECONTENT).timerName("Put Text " + (int)(CODECONTENT.length / 1024) + "kB").run();
                new WebDAVExists(filePath, true).timerName("Exists").run();

            	final String filePathBin = baseDirName + i + "/" + "file" + f + ".bin";
            	new WebDAVPut(filePathBin, CODECONTENT).timerName("Put Bin " + (int)(BINARYCONTENT.length / 1024) + "kB").run();
                new WebDAVExists(filePathBin, true).timerName("Exists").run();
            }
        }
    }

    /**
     * Performs any clean-up task.
     */
    @After
    public void cleanUp()
    {
        // release the WebDAV client
        WebDAVContext.clean();
    }
}
