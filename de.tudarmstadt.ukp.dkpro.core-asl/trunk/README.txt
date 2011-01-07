== PROPRIETARY RESOURCES ==

Use the Maven profile "use-proprietary-resources" to enable additional test cases or functions
if you have installed the proprietary Maven artifacts such as those containing the TreeTagger
models and binaries.

== API DOCS ==

To be able to have the API docs on the Google Code project page, we need to commit it to Subversion.
Because there are many modules, the "aggregate" goal of the Maven javadoc plugin is used.

   1. Make sure your Subversion is configured to set the proper mime-type on HTML files. Your
      ~/.subversion/config file should contain the following lines:

      enable-auto-props = yes
      
      [auto-props]
      *.html       = svn:mime-type=text/html

   2. Run Maven with the profile "build-apidocs":
   
      mvn -Pbuild-apidocs clean install
   
   3. Sync the API docs into the "apidocs" folder
   
      ant -f scripts/build.xml sync-apidocs
   
   4. Commit the "apidocs" folder

         svn commit -m "Updated API docs" apidocs