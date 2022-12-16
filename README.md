# FTC
The ForTheCrown SMP's plugin repository.  
Server IP: `mc.forthecrown.net`

## Building
To build the plugin you should first check [`buildver.properties`](buildver.properties) 
to ensure that debug mode is enabled/disabled, according to your requirement.  
  
Then execute the `gradlew build` command to build the plugin.  
  
The resulting jar files will be in `build/libs` and will be named something 
similar to `ForTheCrown-Debug-1.19.3-1655.jar`  
  
You can also use [`test_server.properties`](test_server.properties) to 
define a test server pathand a launch file to immediately spin up a test 
server after building, this only happens if you've defined the values in 
the properties file and then used the `build_to_test_server` task.

## Contributing
If you wanna contribute to the FTC repository, fork this project, make your 
changes and submit a pull request, we'll look over the pull request and then 
decide if we'll include it or not.  
  
Even if we do not include your changes, we'll still be thankful you chose to 
contribute to our codebase

## Dependencies
FTC mostly on the PaperMC and other open-source libraries that can be 
accessed easily through maven repositories, some plugins, however, tend to
cause issues when used like this, for those plugins, the [`libs`](libs) 
directory holds the jar files of the dependencies that cannot be acquired
through maven repositories.
