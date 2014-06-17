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
         

== GENERATING COVERAGE REPORT ==

This is a two-step process. First Cobertura needs to be run via Maven. Then a special antrun target
needs to be invoked to aggregate the data for all modules into a single report.

    mvn clean cobertura:cobertura
    mvn -Pcobertura-aggregate antrun:run
    
The aggregated coverage report is placed in target/site/cobertura.

We can currently not include the generated report on the Google Code project page because each page
of the report contains a time stamp. Since the reports need to be stored in the subversion
repository, this would cause a lot of data to be changed for each update and cause the repository
to grow too fast. For JavaDoc we could simply turn off the timestamp. For Cobertura, we will
probably have to try some XSLT magic.
