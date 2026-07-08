This folder must contain the MySQL JDBC driver jar before the project will run.

1. Download "MySQL Connector/J" (mysql-connector-j-8.3.0.jar or newer) from:
   https://dev.mysql.com/downloads/connector/j/
   (choose "Platform Independent" > ZIP or TAR archive)

2. Extract the .jar file and place it directly in this "lib" folder,
   so the path is: Bhojan-Aloy/lib/mysql-connector-j-8.3.0.jar

3. In Eclipse: right-click the project > Build Path > Configure Build Path
   > Libraries tab > confirm the jar is listed (the included .classpath
   file already points to lib/mysql-connector-j-8.3.0.jar — if you use a
   different version/filename, update .classpath or re-add the jar via
   "Add JARs..." in Eclipse).
