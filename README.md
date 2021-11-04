# State Of Survival Battle simulator
------

Hi survivors !

I've always wanted to understand what happens in a State Of Survival battle. Some are just curious, but it's like the grail for others to be able to find the magic formula which is hidden behind  😜.

For six months now I have been trying to unravel Kings' little secrets. But the subject is very complex. It is with the new fighting system that I have made decisive discoveries that have allowed me to understand and reproduce now a complete battle.

You can read more on : https://en.ultimate-guide.ovh/fights/battle-mechanics

The battle mechanics of the game is realy complex. You have no idea how many formulas and parameters used😵. Kings must have had as much trouble making and calibrating a realistic engine as I did finding their formulas. At first I used an Excel file but that was quickly insufficient. So I developed an application to simulate and understand the result of a battle with an accuracy of > 99%.

⚠️The simulator only works for states with the new battle engine (currently below 409). This guide is writen for those who have avery  good understanding of the game and who are looking for a better understanding of the battle mechanics in order to optimise them. It will not answer simple questions (what ratio should I use). If this is not your case, you don't have to do anything here ⚠️

More information on simulator here : https://en.ultimate-guide.ovh/fights/battle-simulator

![image](https://user-images.githubusercontent.com/80217596/110250726-20a11800-7f7d-11eb-9b53-6a6e2d6382ed.png)

# Online Simulator
------
The link to access to the simulator : https://battle.ultimate-guide.ovh/battle/ 

Use your discord account to login. This allows you to have your environment with your configurations.

On a mobile phone, you can add it to the desktop to have it as an application.

# Build & run
------

This projet a maven Project, you can also import the project in Eclipse or your favorit java editor.
The battle engine use a MariaDB(kind of mysql) database for load & save your configurations, but you can run the app wihtout it. Configure your database in persistence.xml file, the tables are automaitcly created. However, you will have to create teh database yourself: connect with a mysql client to the db and run "create database battle". 

For maven standalone build & run : 

__Step 1: Install Java JDK 8+ and Maven__

[Java JDK 8+](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)

[Maven](http://maven.apache.org/download.html)

__Step 2: Run__

		mvn tomcat7:run

__Step 3: Use your web browser__

[http://localhost:8080/sos.battle/](http://localhost:8080/sos.battle/)

__Step 4: Optional : If you whant install on a web server => use the war file__

		mvn clean install