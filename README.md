# E-Learning Desktop (Java Swing)

Java desktop e-learning platform with student, instructor, and admin dashboards.

## Features (Current)
- Role-based login (Admin / Instructor / Student)
- Course management and approvals
- Lesson video playback + progress tracking
- Reviews and ratings for courses
- Comments for lessons

## Upcoming Data Model (DB-ready)
Planned data layer to support:
- Lesson interactions: likes + comments
- Course reviews: rating + review comments/likes
- Certificates auto-issued at 100% completion
- Activity analytics for statistics

### New/renamed tables
- `lesson_comments` (renamed from `comments`)
- `lesson_likes`
- `lesson_views`
- `course_reviews` (renamed from `reviews`)
- `course_review_comments`
- `course_review_likes`
- `certificates`
- `user_activity`
- `lessons.like_count`, `lessons.comment_count` (cached counters)

## Project Structure
```
src/main/java/com/elearning/
  ui/                UI screens and dialogs
  service/           Business logic
  dao/               DB access
  model/             Entities
  util/              Helpers (DB, files, charts, etc.)
database/
  schema.sql         DB schema
  seed.sql           Seed data
```

## Setup
### 1) Database
This project uses MySQL/MariaDB. Default local config uses:
- Host: `127.0.0.1`
- Port: `3306`
- Database: `elearning_db`

Create DB + seed (PowerShell):
```powershell
.\scripts\db_check.ps1
.\scripts\db_seed.ps1
```

### 2) Local config
`src/main/resources/config.properties` is ignored in git to avoid conflicts.
Create your own local file with DB connection info.

### 3) Run the app
```powershell
C:\Tools\apache-maven-3.9.6\bin\mvn.cmd -q javafx:run
```

## MariaDB DDL Recovery Log (Important)
If you see errors like `Error writing file '.\ddl_recovery.log'`, fix the MariaDB service once:

1) Edit `C:\ProgramData\MariaDB\my.ini` and add under `[mysqld]`:
```
log-ddl-recovery=D:/MariaDB/data/ddl_recovery.log
```

2) Restart service:
```
sc stop MariaDB
sc start MariaDB
```

After that, `.\scripts\db_check.ps1` and `.\scripts\db_seed.ps1` should run without issues.

## Sample Accounts (Seed)
- `admin` / `admin123`
- `instructor1` / `instructor123`
- `instructor2` / `instructor123`
- `student1` / `student123`
- `student2` / `student123`

## Notes
- Course reviews are stored in `course_reviews` and are tied to a course.
- Lesson interactions (likes/comments) are stored in lesson tables.
