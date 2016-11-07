Hello, in the following lines I will introduce you, how to setup a webdav service based on apache2 and run a load test of your choice. 
The description focuses on linux distributions, tested on Ubuntu 16.04 LTS and enables you to run load tests based on XLT testsuite-webdav.



How to setup your Apache2 based webdav service

-Install Apache2 by issuing the following command lines:
	sudo apt-get update
	sudo apt-get install apache2

	You can check it by opening “http://localhost/” at your browser. You should see apaches default page.

-Create your webdav directory and grand permissions on disc by
	sudo mkdir  /var/www/webdav 				*(or your favorite destination)
	sudo chown -R www-data:www-data /var/www/

-Enabling webdav modules by
	sudo a2enmod dav
	sudo a2enmod dav_fs
	these are stored at /etc/apache2/mods-available and /etc/apache2/mods-enabled

-Configure your apache service
	gain write permissions by 
		sudo chmod 777 /etc/apache2/sites-available/000-default.conf
	open /etc/apache2/sites-available/000-default.conf
	-add as first line: 
		DavLockDB /var/DavLOCK
	-add the following befor the “VirtualHost” tag closes
		Alias /webdav "/var/www/webdav"			

		<Directory /var/www/webdav>
			DAV On
		</Directory>

	I've added my 000-default.conf to my documentation folder
	/samples/testsuite-webdav/doc/

	-restart your server with
		sudo service apache2 restart

-Test it by the following command
	echo “this is a sample file” |  sudo tee -a /var/www/webdav/sample.txt
	this should create your file named sample.txt at /var/www/webdav/
	this should be visible in your browser at http://localhost/webdav/

-Until now you can mount your webdav directory in your file browser 
	(Naulilus via “Connect to Server” to “dav://localhost/webdav/”)
How to setup Digest Authentication

-Type at the command line to install the dependencies
	sudo apt-get install apache2-utils

-Generate the users password file and add each user by repeating the following command
	sudo htdigest -c /etc/apache2/users.password webdav username
	you where immediately asked to type the related password twice

-Grant read permissions to Apache
	sudo chown www-data:www-data /etc/apache2/users.password

-Add the following into etc/apache2/sites-abailable/000-default.conf below “DAV On”
	Options +Indexes
	AuthType Digest
	AuthName “webdav”
	AuthDigestProvider file
	AuthUserFile “/etc/apache2/users.password”
	Require valid-user

	I've added my 000-default.conf to my documentation folder
	/samples/testsuite-webdav/doc/

-Enable the Digest module by
	sudo a2enmod auth_digest
	sudo service apache2 restart


Use this user names and passwords to run your load tests based on testsuite-webdav
