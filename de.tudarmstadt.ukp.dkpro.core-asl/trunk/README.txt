== PROPRIETARY RESOURCES ==

Use the Maven profile "use-proprietary-resources" to enable additional test cases or functions
if you have installed the proprietary Maven artifacts such as those containing the TreeTagger
models and binaries.

== API DOCS ==

To be able to have the API docs on the Google Code project page, we need to commit it to Subversion.
Because there are many modules, the "aggregate" goal of the Maven javadoc plugin is used.

   1. Run Maven with the profile "build-apidocs":
   
      mvn -Pbuild-apidocs clean install
   
   2. Sync the API docs into the "apidocs" folder
   
      ant -f scripts/build.xml sync-apidocs
   
   3. Commit the "apidocs" folder

         svn commit -m "Updated API docs" apidocs