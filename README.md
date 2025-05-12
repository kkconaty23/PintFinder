
# PintFinder

**PintFinder** is a Java/JavaFX desktop application that helps users discover, rate, and review Guinness (and other pints) at their favorite bars. Pints are plotted on an interactive map using Leaflet, and all user-generated data is stored in Firebase.

---

## Table of Contents

1. [Features](#features)  
2. [Architecture & Tech Stack](#architecture--tech-stack)  
3. [Prerequisites](#prerequisites)  
4. [Setup & Installation](#setup--installation)
5. [Intended Users](#intended-users)
6. [How It Works](#how-it-works)
7. [Contributing](#contributors)    


---

## Features

- **Interactive Map**:  
  - Displays bar locations with Leaflet via embedded HTML/JavaScript in a JavaFX `WebView`.  
  - Click markers to view and rate pints.  
- **User Ratings & Reviews**:  
  - Rate Guinness (or any pint) on a 5‑star scale.  
  - Add optional text comments.  
- **Firebase Backend**:  
  - Store and retrieve bar details, user ratings, and comments.  
  - Real‑time updates and offline support.
- **Search & Filter**:  
  - Search bars by name or location.  
  - Filter by average rating.
  - **Personlized Profile Page**
  - Set your profile picture.
  - Look up your past reviews and favorite bars.
- **Desktop‑First UX**:  
  - Responsive JavaFX UI with modern controls.  
  

---

## Architecture & Tech Stack

| Component                   | Technology                                |
|-----------------------------|-------------------------------------------|
| **Frontend**                | Java 11+, JavaFX 17+                      |
| **Map**                     | Leaflet.js (embedded in JavaFX WebView)   |
| **Backend / Database**      | Firebase Firestore & Firebase Auth        |
| **Build Tool**              | Maven                                     |
| **Language**                | Java                                      |
| **IDE**                     | Intelij Ultimate Edition                  |

---

## Prerequisites

- Java Development Kit (JDK) 11 or above  
- Maven 3.6+  
- Firebase account with a Firestore database set up  

---
## Intended Users

- People above the age of 21: Those above the legal drinking age in America
- Guinness fans: Drinkers who are people who order Guinness when going out to a bar
- Social Media Users: People who use social media both post online and view other people's content

---

## Setup & Installation

1. **Clone the repository**  
   ```bash
   git clone https://github.com/kkconaty23/PintFinder.git
   cd PintFinder
2. **Run the build**


## How it works

## Splash Screen
![splash page gif](https://github.com/user-attachments/assets/b350d19b-741a-4192-8adb-f559a9e81b2e)

Our animation welcoming you to the Pint Finder Application 

## Login Window
![login](https://github.com/user-attachments/assets/03d7c903-e610-4b46-a2f2-206e0b434019)

Our login page that requires an email and password that is pulled from our firebase database in order to sign you in to your personal account

## Create Account Page
![sign up page](https://github.com/user-attachments/assets/9d4a5552-a144-4951-877c-22356eebb210)

Our create account page that takes in a users first name, last name, email, and password (hashed) which is uploaded to firebase in order to create a user account. We also have alerts set in place that if you do not check the "Are you 21" box or your password does not match the confirm password field then your account will not be created until those are fixed.

## Home Page
![homepage](https://github.com/user-attachments/assets/9703fef8-97b9-48cd-99b6-389e433bef16)

The home page has a interactive map that allows you to find and rate bars using a drag and click and or search function. There is a side panel that shows all the past reviews of the bars and who made them, plus gives the user the ability to leave their own reviews. There is a pint glass on the right side that is the average score for the current bar clicked on and fills or drains depending on the score out of 10. We also have 3 signs at the top of the page. "Bar of the day" chooses a random bar that is our Bar of the day and will last for 24 hours. "Top 10 Bars" will pull the 10 highest rated bars from our database and list them in descending order. Finally "Profile" will take the current user to their own profile page.

## Profile Page
![profile page pint finder](https://github.com/user-attachments/assets/971c9e0e-55f1-4098-b467-2725105c2ff1)

The Pint Finder Profile page pulls the users informatino from our database to display their first name, get there past reviews and provided some information  the user such as the last bar they reviewed, the number of said reveiws and how long they have been a member.



## Contributors


- [Kevin Conaty](https://github.com/kkconaty23) 
- [Justin Derenthal](https://github.com/JderenthalCS) 
- [Josh Samson](https://github.com/jsams909) 
- [Ryan Cuccurullo](https://github.com/ryguy0601)



