# THIS IS BACKEND
## GOES TO [FRONTEND](https://github.com/quockhanh4551190031/Boarding-Home-Project-Intern-React-Frontend)

### Run command in Terminal
```
./mvnw spring-boot:run
```

### Environment Configuration

src/main/resources/application.properties
```
spring.application.name=<Your Application Name>
server.port=<Your Port>
# ===== Datasource =====
spring.datasource.url=<Your mysql datasource url>boarding_home_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=<Your DB username>
spring.datasource.password=<Your DB password>
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
# ===== JPA / Hibernate =====
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
# ===== JWT =====
jwt.secret=<Your JWT Secret Key 256-bit/32 byte HS256>
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
# ==== GEMINI API ====
gemini.api.key=<Your Gemini API Key>
gemini.api.model=gemini-3-flash-preview
# ==== Upload image files ====
file.upload-dir=uploads/rooms
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
# ==== Review ====
review.rate-limit.max-per-hour=2
review.min-account-age-minutes=0
# == Correctly retrieves client IP addresses behind proxies (Render, Nginx, etc.) and generates HTTPS image URLs. ==
server.forward-headers-strategy=native
# == Login, Register Limit (Set 0 to disable rules) ==
auth.login.max-failures-per-account-ip=5
auth.login.max-failures-per-account=20
auth.login.max-failures-per-ip=30
auth.login.lock-minutes=15
auth.register.max-per-ip-per-hour=20
# ==== Chatbot limit ====
chatbot.limit.per-minute=6
chatbot.limit.guest-per-day=30
chatbot.limit.user-per-day=80
chatbot.limit.global-per-day=100
# == Room Viewing Schedule (Set to 0 to Disable Quantity Limits) ==
app.timezone=Asia/Ho_Chi_Minh
viewing.min-lead-minutes=60
viewing.max-days-ahead=30
viewing.max-pending-per-tenant=5
viewing.max-per-day-per-tenant=10
```
