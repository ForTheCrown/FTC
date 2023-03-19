# FTC
The ForTheCrown SMP's plugin code.  
Server IP: `mc.forthecrown.net`

## Building
To build the plugin you should first check [`build-info.json`](build-info.json) 
to ensure that debug mode is enabled/disabled, according to your requirement.  
  
Then execute the `gradlew build` command to build the plugin.  
  
The resulting jar files will be in `build/libs` and will be named something 
similar to `ForTheCrown-1.19.3-1655-DEBUG.jar`  
  
You can also use the `build-info.json` to  define a test server path and a
launch file to immediately spin up a test server after building, this only
happens if you've defined the values in the JSON file and then used the 
`buildToTestServer` task.

## Contributing
If you wanna contribute to the FTC repository, fork this project, make your 
changes and submit a pull request, we'll look over the pull request and then 
decide if we'll include it or not.  
  
Even if we do not include your changes, we'll still be thankful you chose to 
contribute to our codebase.  
  
As a general rule of thumb, we use [Google's Java style](https://google.github.io/styleguide/javaguide.html)
with a column character limit of 80.