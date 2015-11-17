# Space4Cloud Bench
Space4Cloud Bench is an eclipse plugin that allows to execute batch jobs with space4cloud.

To use the tool:
* import complete Palladio models with all the needed extensions and configuration files to run space4cloud,
* make a configuration for the functionality you want to run using space4cloud and call it "batch.prop",
* run the plugin and select the projects to execute
* sit and wait...!

The tool will actually consider all the files in the project folder starting with the "batch.prop" name.
Thus, if you want to run different tests on the same project, you could just name all your files for example:
batch.prop0, batch.prop1, and so on.