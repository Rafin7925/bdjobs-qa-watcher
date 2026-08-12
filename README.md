bdjobs-qa-watcher

A Selenium + Java automation that searches BDJobs for QA/Software Testing roles, filters out irrelevant results, and (soon) logs new postings to MySQL with a WhatsApp alert — so I stop manually refreshing job boards during my internship search.

Built as a hands-on project while learning Selenium automation and API integration, coming from a manual QA testing background.

Why I built this

I know manual testing well but wanted real, working experience with test automation and API calls — not just theory. Instead of a generic tutorial project, I built something I'd actually use: a bot that watches BDJobs for QA/testing roles so I don't miss new postings while job hunting.

What it does right now
Opens BDJobs and searches for QA/testing-related keywords
Scrapes every job listing from the results page (title, company, link)
Filters results client-side for genuinely relevant roles (BDJobs' own search isn't strict about keyword matching, so filtering is done in code)
Prints matched jobs with their direct application link
Tech stack
Java — core language
Selenium WebDriver — browser automation
WebDriverManager — automatic ChromeDriver management
Maven — dependency management
MySQL (in progress) — storing seen jobs to detect new postings
CallMeBot WhatsApp API (planned) — instant notification when a new job is found
Roadmap
 Selenium scraper with search + client-side filtering
 MySQL storage to track previously seen jobs
 WhatsApp notification via CallMeBot API when a new job is detected
 Scheduled/looped execution
 Postman collection documenting the notification API calls
How to run
Clone the repo
Open in IntelliJ IDEA as a Maven project
Let Maven resolve dependencies (selenium-java, webdrivermanager)
Run JobScraper.java
A Chrome window will open, search BDJobs, and print matched QA jobs to the console
A note on AI assistance

I used Claude AI as a learning aid throughout this project — to debug Selenium locator issues, understand Angular-based site quirks, and structure the code — while writing, running, and understanding every part myself. This project reflects my actual skill level as I continue learning automation and API testing, not a finished/polished production tool.

About me

QA Engineer with a manual testing background, currently building automation and API testing skills.

LinkedIn · Portfolio · GitHub
