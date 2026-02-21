# College Helpdesk (Professional Edition)

Production-style complaint platform for colleges with role workflows, SLA, reports, notifications, and attachments.

## Completed Features
- Student registration + secure login
- Role-based access (`STUDENT`, `STAFF`, `MANAGEMENT`)
- Complaint submission with category + priority
- Staff assignment by management
- Status pipeline (`SUBMITTED`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`)
- Notes timeline on each complaint
- Attachments upload/download per complaint
- SLA due time + automatic escalation scheduler
- Email notifications (new complaint, assignment, status change, escalation)
- Management analytics (status KPIs, department load, monthly trend)
- Enhanced UI with hover transitions, panel depth, and interactive 3D tilt animation

## Default Users
- `admin / admin123` (management)
- `dean / dean123` (management)
- `staff1 / staff123` (staff)
- `staff2 / staff123` (staff)
- `student1 / student123` (student)

## Local Run
```powershell
cd "C:\Users\Tanvi\OneDrive\Desktop\studenthd"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tanvi9606"
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```
Open: `http://localhost:8081/login`

## Optional Mail Setup
```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="your_email"
$env:MAIL_PASSWORD="your_app_password"
$env:MAIL_FROM="helpdesk@college.edu"
```

## GitHub Push
```powershell
cd "C:\Users\Tanvi\OneDrive\Desktop\studenthd"
git init
git add .
git commit -m "Professional college helpdesk"
git branch -M main
git remote add origin https://github.com/<your-username>/<repo-name>.git
git push -u origin main
```

## Deploy Options

### Option 1: Docker (recommended)
```powershell
docker compose up --build -d
```
App: `http://localhost:8081/login`

### Option 2: Cloud VM / VPS
1. Install Java 17 + MySQL.
2. Set env vars (`DB_USERNAME`, `DB_PASSWORD`, optional mail vars).
3. Build jar: `mvn -DskipTests package`
4. Run: `java -jar target/student-helpdesk-1.0.0.jar --server.port=8080`
5. Put Nginx in front (optional) + SSL.

### Option 3: Render/Railway/Heroku-like
- Use included `Procfile`.
- Set environment variables in platform settings.
- Ensure MySQL service is reachable.

## Key Files
- Backend workflow: `src/main/java/com/studenthelpdesk/service/HelpdeskService.java`
- Scheduler: `src/main/java/com/studenthelpdesk/service/EscalationScheduler.java`
- Notifications: `src/main/java/com/studenthelpdesk/service/NotificationService.java`
- File storage: `src/main/java/com/studenthelpdesk/service/FileStorageService.java`
- Dashboard UI: `src/main/resources/templates/dashboard.html`
- Animations: `src/main/resources/static/js/app.js`
- Styles: `src/main/resources/static/css/style.css`
- Docker: `Dockerfile`, `docker-compose.yml`
