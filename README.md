# Customer Support Helpdesk System

Spring Boot backend with JWT authentication and role-based access (ADMIN, STAFF, USER).

## Setup

1. Create the database:
```sql
CREATE DATABASE helpdesk_DB;
```

2. Copy `application.properties.example` to `application.properties` and fill in your real values (database password, Gmail address, Gmail app password).

3. Run the app. It starts on `http://localhost:8080`.

## API Testing Guide (Postman)

Use **Bearer Token** under the Authorization tab for every request except register and login. Tokens expire after 1 hour — log in again to get a fresh one.

### 1. Register users

`POST http://localhost:8080/auth/register`

**Admin:**
```json
{
  "name": "Omkar Admin",
  "email": "admin@helpdesk.com",
  "password": "admin123",
  "role": "ADMIN"
}
```

**Staff:**
```json
{
  "name": "Staff Person",
  "email": "staff@helpdesk.com",
  "password": "staff123",
  "role": "STAFF"
}
```

**User:**
```json
{
  "name": "Normal User",
  "email": "user@helpdesk.com",
  "password": "user123",
  "role": "USER"
}
```

### 2. Login (get a token)

`POST http://localhost:8080/auth/login`
```json
{
  "email": "user@helpdesk.com",
  "password": "user123"
}
```
Copy the returned token. Use it as the Bearer Token for the role that logged in.

### 3. USER creates a ticket

`POST http://localhost:8080/api/tickets`
Auth: USER token
```json
{
  "title": "My laptop screen is broken",
  "description": "The screen has a crack and shows wrong colors",
  "priority": "HIGH"
}
```
Note the `id` returned — used in steps below.

### 4. USER views their own tickets

`GET http://localhost:8080/api/tickets/my`
Auth: USER token

### 5. ADMIN logs in, then assigns the ticket to STAFF

Login as admin (step 2 pattern), then:

`PUT http://localhost:8080/api/admin/tickets/{ticketId}/assign`
Auth: ADMIN token
```json
{
  "staffId": 2
}
```
(`staffId` is the STAFF user's `id` — check with `SELECT id, name, role FROM users;` in MySQL)

### 6. STAFF logs in, views assigned tickets

`GET http://localhost:8080/api/tickets/assigned`
Auth: STAFF token

### 7. STAFF updates status to IN_PROGRESS

`PUT http://localhost:8080/api/tickets/{ticketId}`
Auth: STAFF token
```json
{
  "status": "IN_PROGRESS"
}
```

### 8. Add comments (conversation thread)

`POST http://localhost:8080/api/tickets/{ticketId}/comment`
Auth: STAFF or USER token
```json
{
  "message": "Hi, can you tell me which laptop model you are using?"
}
```

### 9. Upload a file (attachment)

`POST http://localhost:8080/api/tickets/{ticketId}/upload`
Auth: USER or STAFF token
Body type: `form-data`, key `file`, value: choose a file

### 10. STAFF resolves the ticket

`PUT http://localhost:8080/api/tickets/{ticketId}`
Auth: STAFF token
```json
{
  "status": "RESOLVED"
}
```
This automatically emails the ticket creator that their issue is resolved.

### 11. ADMIN / STAFF view all tickets

`GET http://localhost:8080/api/tickets/all`
Auth: ADMIN or STAFF token

## Roles Summary
| Endpoint | Who can access |
|---|---|
| `/auth/register`, `/auth/login` | Anyone |
| `/api/tickets` (create), `/api/tickets/my`, comments, upload | Logged-in user (any role) |
| `/api/tickets/all` | ADMIN, STAFF |
| `/api/admin/**` | ADMIN only |

Step 1: Joining the System
A new person registers their name and password. They log in. Your security code generates a token and gives it to them.

Step 2: Asking for Help
The person creates a "Ticket". They type out their problem and upload a picture of the error. Your code saves this ticket in the database. Your code automatically sends an email to the Admin saying a new problem has arrived.

Step 3: Assigning the Work
The Admin logs in. Your code shows the Admin a list of all open tickets. The Admin clicks a button to assign the new ticket to a specific Staff member.

Step 4: Working on the Problem
The Staff member logs in. They see the ticket assigned to them. They change the ticket status from "OPEN" to "IN_PROGRESS". Your code saves this new status in the database.

Step 5: Talking
The Staff member needs more details. They type a comment inside the ticket. The User replies with another comment. Your code saves all these messages in the database and links them directly to that specific ticket.

Step 6: Finishing the Work
The Staff member fixes the problem. They change the ticket status to "RESOLVED". Your code saves this final status. Your code sends an automatic email to the User letting them know their problem is officially fixed
