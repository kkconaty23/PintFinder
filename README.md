# PintFinder

**PintFinder** is a Java/JavaFX desktop application that helps users discover, rate, and review Guinness (and other pints) at their favorite bars. Pints are plotted on an interactive map using Leaflet, and all user-generated data is stored in Firebase.

---

## Table of Contents

1. [Features](#features)  
2. [Architecture & Tech Stack](#architecture--tech-stack)  
3. [Prerequisites](#prerequisites)  
4. [Setup & Installation](#setup--installation)
5. [Intended & Users](#intended--users)
6. [Contributing](#contributors)    


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

## Contributors


- [Kevin Conaty](https://github.com/kkconaty23) 
- [Justin Derenthal](https://github.com/JderenthalCS) 
- [Josh Samson](https://github.com/jsams909) 
- [Ryan Cuccurullo](https://github.com/ryguy0601)



