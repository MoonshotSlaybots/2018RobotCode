# 2018RobotCode
Java code for 2018 year (Power Up)

To have required libraries, must install eclipse plugin "Robot Java Development" by WPI

to install
--------------
https://wpilib.screenstepslive.com/s/currentCS/m/java/l/599681-installing-eclipse-c-java

1. open eclipse and click "help" in the top tool bar
2. click "install new software" 
3. click "add" near the top of the window
4. in the "name" field enter "FRC plugins"
5. in the "location" field enter "http://first.wpi.edu/FRC/roborio/release/eclipse/"
6. click ok
7. check the WPILib Robot Development plugin box 
8. (optional) select which language to install from the drop down under WPILib Robot Development
9. click next at the bottom of the window, the plugin will begin to download and install
10. click ok on the window about unsigned content when it appears 
11. restart eclipse for the plugin to take effect

WPI Library Documentation
-------------------------
Documentation of the libraries used in frc (for java programming) can be found here: http://first.wpi.edu/FRC/roborio/release/docs/java/

Build Path Errors
------------------

If you are still expericing build path errors try these steps courtesy of team #3504 (https://github.com/GirlsOfSteelRobotics/Docs/wiki/Programming-Environment-Set-Up)

The projects imported in the step above might all show build path errors. If you see this issue, it can be fixed by creating a temporary project based on a WPIlib template. The process of creating a new project will define a set of build variables needed by all robot projects. Once created, the temporary project can be removed.

13. Create a temporary FRC Java project
    1. From the File menu, choose New...
    2. In the submenu, choose Project (NOT Java Project!)
    3. Open WPILib Robot Java Development
    4. Select Example Robot Java Project
    5. Click Next >
    6. The Select Example Project to Create wizard opens
    7. Choose GearsBot, click Next >
    8. Enter the Project Name "temp"
    9. Click Finish
    10. All (or at least most) of the errors should disappear after a minute
14. Now delete the "temp" project:
    1. Right-click on "temp"
    2. Select Delete from the pop-up menu
    3. IMPORTANT: enable the "Delete project contents on disk" option to clean up the unneeded files
    4. Click OK
