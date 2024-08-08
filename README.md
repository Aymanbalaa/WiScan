# WiScan

## Product Overview :
WiScan is a powerful tool designed to capture and analyze local WiFi data. 
This tool gathers information such as BSSIDs, SSIDs, security protocols, and corresponding location data. 
It processes and cleans the collected data, providing real-time progress updates to the user. 
The cleaned data is then sent to the accompanying app for advanced visualizations.
The system is highly configurable, allowing users to adjust settings such as WiFi scan frequency and radius.

## Android App Actvities :
1- Home page with hardware system stats and button navigation to rest of the app

2- Discovered network list provides basic networks details , allows sorting filtering and exporting to csv

3- Active List sorts networks by signal strength

4- Map Activity uses triangulation algorithm to set pin of every network on the map using google maps api and allows user to chose cluster radius.

5- Statistics page that provides interactive , dynamically changing pie charts for security protocols and network providers 

6- Setting page where user can change language , database refresh interval , theme ( light/dark) and other metrics.Also offers a shutdown button that will safely shutdown the scanner device.

7- Help and FAQ page with video tutorial


![image](https://github.com/user-attachments/assets/51df8092-c566-4caa-927c-ad6a4be5d806)
![image](https://github.com/user-attachments/assets/3db7dfbd-4550-40cc-9131-532aeb98e12b)
![image](https://github.com/user-attachments/assets/953a8b09-3566-4c6c-87b0-c67518689d95)
![image](https://github.com/user-attachments/assets/6e4b8309-e9ec-4d14-ae79-11b76eed4a11)
![image](https://github.com/user-attachments/assets/4da2815c-8dc4-4702-810f-2b810ca5810a)
![image](https://github.com/user-attachments/assets/891f0a5b-5155-422b-98e7-ffb91e9c7364)


## Hardware Specifications :
1- Raspberry Pi 4 2GB Model B

2- GPS module

3- 5dBi Long Range WiFi Antenna

4- Holder designed in CAD to help transport

5- Power banks and hotspots

## Technologies :
1- Android Studio

2- Google Maps SDK

3- Flask Server

4- SQLite

5- OkHTTP ( for server fetching )

6- MPAndroidChart ( for interactive pie charts and statistics )





