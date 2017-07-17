# testsuite-webdav

## Description

This is a load test suite built for XLT. This project focuses on making load testing WebDAV servers possible and gives detailed results for analytic purposes. The test suite is based on XLT by Xceptance Software Technologies GmbH and the Java library Sardine.

## Prerequisites
* XLT 4.9.2 (or higher)
* Java IDE (e.g. Eclipse or IntelliJ)
* Java / OpenJDK
* Sardine 5.7 or higher

## Installation
* Download latest XLT from Xceptance homepage 
( https://www.xceptance.com/en/xlt/download.html ).
* Start a new Project by importing testsuite-webdav ( xlt-4.7.1/samples/testsuite-webdav ).
* Include all JAR’s to your project ( xlt-4.7.1/lib/ ).
* Include the latest Sardine JAR to your project 
( https://github.com/lookfirst/sardine or http://search.maven.org ).

## Setup your project
Change your projects output paths to:

* Output:		xlt-4.7.1/samples/testsuite-webdav/classes
* Test output:		xlt-4.7.1/samples/testsuite-webdav/target/test-classes

If you miss this step, your master controller will not be able to execute your test cases.

## Usage Guide
JavaDoc is added at xlt-4.7.1/samples/testsuite-webdav/doc/index.html.
TWebdav is an example to show how testsuite-webdav works. It is located at 
/home/student2/xlt-4.7.1/samples/testsuite-webdav/src/webdav/loadtest/tests and describes a test scenario. It is recommendable to use this test to get into the testsuite.

Testsuite-webdav provides you an action set of basic operations executed by Sardine client, which implements webdav communication as a headless client. These actions cover all basic file operations on a server. The actions are named as the following pattern:

\[webdav request name\]\[affected data\]

Affected data is a generic term for:
File		-	is a single file
Directory	-	is a single directory
Resource	-	is a single file or directory

To make it clear, there are 8 different actions:

* CheckResourcePath - to check the existence of affected data at a path to your server
* ListResources - to get a set of resources for following actions or assertions
* CreateDirectory - to create a new directory at a specific path
* GetFile - to get a file
* PutFile - to upload a file onto your server
* MoveResource - to move a file or a whole directory
* CopyResource - to copy a file or a whole directory
* DeleteResource - to delete a file or a whole directory

To compose own test cases, the actions can be combined to test scenarios. The first step is to create a initial action and setup the connection information. Therefore an action gives the methods to set a host name, a webdav directory and if authentication is used, to set credentials. These methods should be called at the first action instance or the static context holder (WebdacContext) directly after instantiating it and importantly before running the action. 

To use an action it needs paths, or resource objects from previous performed list actions. A list action returns always a set of resources, may empty or with a single resource. To do a selection simply use the ListSelection utility, which provides several methods to select results of different kinds. These methods can also return empty sets or in case of getting a random result, it returns NULL out of an empty set.

The ListResources and GetFile actions are something special, because they keep results in memory as long as they do not have been released manually. In case of the GetFile action, you can set a storage flag to false, if you do not need the results for following actions and use it just as a download load test. In this case it can be also instantiated as an anonymous action like the following example: 
new GetFile(“http://localhost/webdav/Readme.txt”, false).run();
With this example you can fire and forget, otherwise use the release methods of these actions as soon as the results are not used anymore.

At instantiating an action, keep in mind to set a ‘/’ at the end of the path string to avoid unsuspected reactions. Whitespace in a path will be substituted by ASCII corresponding hexadecimal values by taking use of the PathBuilder utility in all predefined actions.

To run an action use its run method. After completing the action the results and all used paths can be accessed by their provided methods for following actions. If the test is performed by several users or machines at the same time, it has to focus on directing actions to separated locations to avoid race conditions. Therefore it is helpful to create user specific directories by adding their userID, provided by Session.getCurrent().getUserID() into the directories names and founding a test design on separating. 

CreateDirectory create = new CreateDirectory(“http//localhost/webdav/” + Session.getCurrent().getUserID + “_Home/”).run()

For all following actions you can use this path by create.getUsedRelativePath() inside the constructor.

At the creation of an action it is stored inside the context holder as “activeAction” and if there was an action before, this is referenced as “previousAction” of the “activeAction”. All initialising information like host name, webdav directory or credential can be also set by the context, which is user specific. An important thing is to call the “clean” method of the context until the test is finished. In my example test case it is done by the @After annotation which calls WebdavContext.clean(). This releases your “activeAction” to avoid endless chaining along several iterations and resulting memory problems. At least you have to compile your testcase and actions to provide it to the mastercontroller, which executes the load test. If your IDE responds, the mastercontroller could not find the test case, that is the most likely reason, so check “Setup your project” again if you do these steps.
