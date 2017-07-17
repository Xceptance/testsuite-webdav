package com.company.tests.webdav;

import java.io.IOException;
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
import com.xceptance.xlt.webdav.actions.WebDavConnect;
import com.xceptance.xlt.webdav.actions.WebDavCopy;
import com.xceptance.xlt.webdav.actions.WebDavCreateDirectory;
import com.xceptance.xlt.webdav.actions.WebDavDelete;
import com.xceptance.xlt.webdav.actions.WebDavExists;
import com.xceptance.xlt.webdav.actions.WebDavGet;
import com.xceptance.xlt.webdav.actions.WebDavList;
import com.xceptance.xlt.webdav.actions.WebDavMove;
import com.xceptance.xlt.webdav.actions.WebDavPut;
import com.xceptance.xlt.webdav.util.DavResourceListUtils;
import com.xceptance.xlt.webdav.util.WebDavContext;

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
        final String baseUrl = "https://localhost:8443";
        final String rootDir = "/webdav";
        final String user = "webdav";
        final String password = "webdav";

        // our unique id to create a clean directory for testing
        final String uniqueName = Session.getCurrent().getUserID();

        // *********************************************************************
        // Build up the connection and make sure the root folder responds,
        // even if this would yield a 404, the connection would be established
        // but of course, the 404 assertion fail this test already
        new WebDavConnect(baseUrl, rootDir, user, password).run();

        // *********************************************************************
        // At the beginning we create an action.
        // This will check our webdav directory and this should be named "CheckResourcePath Home" in your test results.
        // Alternatively we can use a constructor without timer name,
        // this will give our action the name of its class CheckResourcePath in the results.
        final WebDavList root = new WebDavList(".", 1).timerName("List Root");
        root.run();

        // Now we want to get a file out of the results. So let us select one out of the results.
        // Get the results from the previous performed action
        final List<DavResource> results = root.getResources();

        // *********************************************************************
        // We select all files by using the ListSelector utility.
        // This will return a new list.
        final List<DavResource> dirs = DavResourceListUtils.getAllDirectories(results);

        // let us check if we have our test dir, if yes, get rid off it
        for (final DavResource dir : dirs)
        {
            if (dir.getName().equals(uniqueName))
            {
                // remove the user dir
                // Finally we delete our directory, to prepare following iterations of our test case
                new WebDavDelete(dir).timerName("Delete Root").run();

                // And check it is gone
                new WebDavExists(dir, false).run();
            }
        }

        // *********************************************************************
        // create the dir again
        final String USERROOT = uniqueName;
        new WebDavCreateDirectory(USERROOT).timerName("CreateDirectory UserRoot").run();

        // *********************************************************************
        new WebDavExists(USERROOT, true).timerName("Exists UserRoot").run();

        // *********************************************************************
        // Upload/Put
        final byte[] randomFileContent = new byte[1024 * 10];
        XltRandom.nextBytes(randomFileContent);

        final String randomFilePath1 = USERROOT + "/" + "random1.bin";
        new WebDavPut(randomFilePath1, randomFileContent).timerName("Put Bin 10kB").run();

        // *********************************************************************
        // Copy
        final String randomFilePath2 = USERROOT + "/" + "random2.bin";
        new WebDavCopy(randomFilePath1, randomFilePath2).timerName("Copy 10kB").run();

        // *********************************************************************
        // Download/Get
        final WebDavGet get = new WebDavGet(randomFilePath2, true).timerName("Get 10kB");
        get.run();

        Assert.assertArrayEquals(randomFileContent, get.getFileContent());

        // *********************************************************************
        // Delete
        new WebDavDelete(randomFilePath2).timerName("Delete 10k").run();
        new WebDavExists(randomFilePath2, false).timerName("Exists").run();

        // *********************************************************************
        // Move
        final String randomFilePath3 = USERROOT + "/" + "random3.bin";
        new WebDavMove(randomFilePath1, randomFilePath3).timerName("Move").run();
        new WebDavExists(randomFilePath1, false).timerName("Exists").run();
        new WebDavExists(randomFilePath3, true).timerName("Exists").run();

        // *********************************************************************
        // Upload/Put a large file
        final String largeFilePath1 = USERROOT + "/" + "large.bin";
        new WebDavPut(largeFilePath1, new RandomInputStream(1024 * 1024 * 10, false)).timerName("Put Bin 10MB").run();
        new WebDavExists(largeFilePath1, true).timerName("Exists").run();

        final String largeFilePath2 = USERROOT + "/" + "large.txt";
        new WebDavPut(largeFilePath2, new RandomInputStream(1024 * 1024 * 10, true)).timerName("Put Text 10MB").run();
        new WebDavExists(largeFilePath2, true).timerName("Exists").run();

        // *********************************************************************
        // Get the large files
        new WebDavGet(largeFilePath1, false).timerName("Get Bin 10MB").run();
        new WebDavGet(largeFilePath2, false).timerName("Get Text 10MB").run();

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
            new WebDavCreateDirectory(dirPath).timerName("CreateDir L2").run();
            new WebDavExists(dirPath, true).timerName("Exists").run();

            // files per dir
            final int FILECOUNT = 10; // we get two per loop, txt and bin
            for (int f = 0; f < FILECOUNT; f++)
            {
                final String filePath = baseDirName + i + "/" + "file" + f + ".txt";
                new WebDavPut(filePath, CODECONTENT).timerName("Put Text " + (int) (CODECONTENT.length / 1024) + "kB").run();
                new WebDavExists(filePath, true).timerName("Exists").run();

                final String filePathBin = baseDirName + i + "/" + "file" + f + ".bin";
                new WebDavPut(filePathBin, CODECONTENT).timerName("Put Bin " + (int) (BINARYCONTENT.length / 1024) + "kB").run();
                new WebDavExists(filePathBin, true).timerName("Exists").run();
            }
        }
    }

    /**
     * Performs any clean-up task.
     * 
     * @throws IOException
     */
    @After
    public void cleanUp() throws IOException
    {
        // release the WebDAV client
        WebDavContext.cleanUp();
    }
}
