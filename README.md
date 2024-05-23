# Intro to Java Final Project
## Haohan Ji

My project is an online room reservation system. The general idea is that users can sign up for an account and log in to see a weekly schedule of occupied rooms. My program consists of a plethora of functionalities like opening/closing connection with the server, creating accounts on the database, login/logout and checking/adding/deleting room reservations on the database. 

## My program also has features that make it more user friendly. Here are some examples.
### A program log on the login interface displays program status. 

### A top status bar on the main interface gives feedback on users’ activities. 

### An emergency level is labeled on all room reservations, giving reference on which events are more important. 
### Privileged accounts have more power on editing room reservations. 
### Multiple users can use it at the same time and get updates in realtime.

## How to run: 
Simply run the Client.java and MultiThreadServer.java. The only privileged user is admin: Username: admin Password: 123456.  Users firstly need to open an online connection to the server and enter account info. New users can create an account by clicking the “signUp” button. After signing in, the main interface pops out and shows everything. The top bar is the action status bar, which displays response once the user takes any action. At the center, the user enters necessary reservation details and hits the “Add” button. Then the event table updates. To delete an event, enter the eventID at the bottom of the table and hit delete. Notice, only the admin has permission to delete every event. Regular users can only delete their own event. 

## Evaluation: In this project I use many important Java features: 
### Databases/JDBC is a crucial part that I use to store both user info and event info. The server maintains Mydatabase.db to have access to both user information and event information.
### Networking: The client needs to open and close sockets to connect to the server and every data transfer is done through input/outputstream.
### Multithreading enables multiple users to connect to the server at the same time and get real time updates on the event table.
### GUI is essential to my program’ user friendliness. 
