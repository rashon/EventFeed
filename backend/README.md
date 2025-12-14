# EventFeed Backend API

This is a simple Go backend using Gin framework for managing events.

## Setup

1. Ensure Go 1.25+ is installed.
2. Navigate to the `backend` directory.
3. Run `go mod tidy` to install dependencies.
4. Run `go run .` to start the server on `:8080`.

## Endpoints

### POST /login
Authenticates a user and returns a dummy token.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Response (200):**
```json
{
  "token": "token-admin-abc123..."
}
```

**Response (401):**
```json
{
  "error": "invalid credentials"
}
```

### GET /profile
Returns the current user's profile details (name and email).

**Response (200):**
```json
{
  "name": "Admin User",
  "email": "admin@eventfeed.com"
}
```

### GET /events
Lists events with pagination.

**Query Parameters:**
- `page` (int, default 1): Page number.
- `size` (int, default 10): Items per page.

**Response (200):**
```json
{
  "page": 1,
  "size": 10,
  "total": 200,
  "events": [
    {
      "id": 1,
      "title": "Event #001",
      "description": "This is a generated description for event 1.",
      "start": "2025-12-12T12:00:00Z",
      "end": "2025-12-12T14:00:00Z",
      "location": "Location 1",
      "organizer": {
        "id": 1,
        "username": "admin"
      }
    },
    ...
  ]
}
```

### GET /events/:id
Gets details of a specific event.

**Path Parameters:**
- `id` (int): Event ID.

**Response (200):**
```json
{
  "id": 1,
  "title": "Event #001",
  "description": "This is a generated description for event 1.",
  "start": "2025-12-12T12:00:00Z",
  "end": "2025-12-12T14:00:00Z",
  "location": "Location 1",
  "organizer": {
    "id": 1,
    "username": "admin"
  }
}
```

**Response (404):**
```json
{
  "error": "event not found"
}
```

### GET /download/:file_id
Streams a large file (default 1 GiB of random bytes).

**Query Parameters:**
- `mb` (int, default 1024): Size in MiB.

**Response:** Binary stream with headers for download.

## Data Structures

- **User**: {id, username, password (hidden), name, email}
- **Event**: {id, title, description, start, end, location, organizer}

## Testing

Run tests with `go test ./...`.

## Notes
- Uses in-memory data; no persistence.
- 200 dummy events seeded on startup.
- Login uses hardcoded "admin"/"password".