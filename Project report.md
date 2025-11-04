🧾 **Assignment Notes: AKLeg Senate Scraper ![](Aspose.Words.1e7af31a-f765-4ff8-93ae-308396833a6e.001.png)**

**Name:** Sahil Mittal 

**Year:** B.Tech – 3rd Year 

**Date:** November 4, 2025 

**Assignment Title:** Web Scraping Alaska Senate Members using Selenium in Java 

**Summary:** 

For this assignment, I created a Java program that automatically scrapes details of Alaska State Senate members from the official website[ https://akleg.gov/senate.php](https://akleg.gov/senate.php?utm_source=chatgpt.com). The program uses **Selenium WebDriver**, managed by **WebDriverManager**, to open a browser (in headless mode) and visit each senator’s profile. From there, it extracts key details like the senator’s name, title, district, party, contact info, and email. 

All the collected information is neatly saved into a senators.json file using the **Jackson library**. I also added some short delays and checks to make sure the pages load properly and that I don’t overload the site while scraping. Overall, the project helped me understand how Selenium can be used for data automation and real-world information gathering. 

**Main Features of My Code:** 

- Launches a **headless Chrome browser** automatically using ChromeOptions. 
- Collects all senator profile links from the Senate page. 
- Visits each profile and extracts: 
- Name 
- Title (Senator/Representative) 
- District (Position) 
- Party 
- Address 
- Phone 
- Email 
- Profile URL 
- Covers both **“Session Contact”** and **“Interim Contact”** sections for complete contact info. 
- Saves all the data in a properly formatted **JSON file (senators.json)**. 
- Adds small **delays between requests** to be respectful to the website. 

**Time Taken:** Roughly **2 hours** in total — including coding, debugging, testing, and 

formatting the output file. 
