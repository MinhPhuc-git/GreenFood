# 🍃 GreenFood - Smart E-Commerce Platform

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Roboflow](https://img.shields.io/badge/Roboflow-AI-6A0DAD?style=for-the-badge)

GreenFood is a modern, feature-rich e-commerce web application built with **Spring Boot**, designed specifically for organic food and agricultural products. It integrates cutting-edge **AI capabilities** (via Roboflow) for ingredient detection.

---

## 🌟 Key Features

- **🛒 E-commerce Core**: Complete shopping cart, checkout, and order management.
- **🤖 AI Ingredient Detection**: Upload food images to automatically detect ingredients using a customized Roboflow model.
- **🎁 Loyalty Program**: Accumulate points, membership tiers, and apply vouchers.
- **🛡️ Secure Authentication**: JWT-based authentication & authorization.
- **📊 Admin Dashboard**: Full control over products, users, orders, and analytics.

## 🛠️ Technology Stack

- **Backend**: Java 17+, Spring Boot 3.2.x, Spring Data JPA, Spring Security
- **Database**: MySQL 8.0+
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla), Thymeleaf templates
- **Build Tool**: Maven
- **External Services**: Roboflow API (Computer Vision), SMTP (Email Notifications)

## 📁 Project Architecture (Package by Feature)

The project follows a clean **"Package by Feature"** architecture to ensure modularity and scalability:

```text
com.example.GreenFood
├── admin/       # Dashboard & System Management
├── config/      # Security, Database, External APIs configuration
├── loyalty/     # Points, Vouchers, Memberships
├── model/       # JPA Entities (Entities mapping to database)
├── order/       # Cart, Checkout, Order History
├── product/     # Products, Recipes, Reviews, Roboflow AI
└── user/        # Authentication, Accounts, Profiles, Email
```

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- MySQL Database
- Maven 3.9+

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/MinhPhuc-git/GreenFood.git
   cd GreenFood
   ```

2. **Configure Database & APIs:**
   Rename `src/main/resources/application.properties.example` to `application.properties` and update the placeholders:
   - MySQL credentials (`spring.datasource.username`, `spring.datasource.password`).
   - Roboflow API Key (`roboflow.api-key`).

3. **Database Initialization:**
   The tables will be automatically generated upon the first run (`spring.jpa.hibernate.ddl-auto=update`).

4. **Run the Application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   Or use the included maven wrapper if you prefer building it first:
   ```bash
   ./mvnw clean package
   java -jar target/GreenFood-0.0.1-SNAPSHOT.jar
   ```

5. **Access the platform:**
   - URL: `http://localhost:8081`

---
*Developed with ❤️ by the GreenFood Team.*
