### GENERAL INFORMATION

This software has been developed by Alexandre Burel for internal use inside LSMBO only.
To start the application, double-click on the file raw-cleaner.bat


### SETTINGS

Open settings file in config/settings.json
Important: for all paths, make sure to use either "/" or "\\" as separators

You should only change the following values:
* rawDataDirectory : the local raw data directory, such as "D:/Data".
* archiveDirectory : the remote archive directory, such as "V:\\Q-Exactive-Plus 2019". This software expects to find years and months directories from this point.
* machineIdentifiers : the letter used to recognize raw data, such as "Q" for the Q-Exactive Plus. You can set multiple identifiers, separated with a space character (ie. "Q X").
* defaultReportDirectory : the directory suggested when exporting data

The next values can be modified by the administrator only
* rawDataExtension : the list of raw files extensions. Like machineIdentifiers, you can set multiple extensions separated with a space character.
* minimalNumberOfMonthsBeforeDeletion : we should only delete data older than 3 months, do not change this value without warning the administrators.
* minimalUnarchivedFilesBeforeWarning : when parsing the files, if more than this number of files are not archived when they should have been you will get a warning.

* msaccessPath : the path to the ms-access.exe executable, it should be inside a ProteoWizard directory.
* defaultTempDirectory : the directory used to store temporary files. These files should be deleted automatically when closing the software


### RAW DATA IDENTIFICATION

The settings machineIdentifiers and rawDataExtension are used to recognize raw data in the list of files. If a file or directory starts with one of the machine identifiers
and ends with one of the raw data extensions, it will be considered as a raw file and will be checked for corruption.
Make sure to provide information that will only identify true raw data, otherwise you may have a lot of corrupted files, just because ms-access will not be able to read them.