package main

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
)

type User struct {
	ID       int    `json:"id"`
	Username string `json:"username"`
	Password string `json:"-"`
}

type Event struct {
	ID          int       `json:"id"`
	Title       string    `json:"title"`
	Description string    `json:"description"`
	Start       time.Time `json:"start"`
	End         time.Time `json:"end"`
	Location    string    `json:"location"`
	Organizer   User      `json:"organizer"`
}

var (
	users  map[string]User
	events []Event
)

func seedData() {
	users = map[string]User{
		"admin": {ID: 1, Username: "admin", Password: "password"},
	}

	organizer := users["admin"]
	now := time.Now()
	// create 200 dummy events for pagination testing
	for i := 1; i <= 200; i++ {
		e := Event{
			ID:          i,
			Title:       fmt.Sprintf("Event #%03d", i),
			Description: fmt.Sprintf("This is a generated description for event %d.", i),
			Start:       now.Add(time.Duration(i) * time.Hour),
			End:         now.Add(time.Duration(i+2) * time.Hour),
			Location:    fmt.Sprintf("Location %d", (i%10)+1),
			Organizer:   organizer,
		}
		events = append(events, e)
	}
}

func main() {
	seedData()

	r := gin.Default()

	r.POST("/login", handleLogin)
	r.GET("/events", handleListEvents)
	r.GET("/events/:id", handleGetEvent)
	r.GET("/download/:file_id", handleDownload)

	r.Run(":8080")
}

func handleLogin(c *gin.Context) {
	var req struct {
		Username string `json:"username"`
		Password string `json:"password"`
	}
	if err := c.BindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid request"})
		return
	}
	u, ok := users[req.Username]
	if !ok || req.Password != u.Password {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid credentials"})
		return
	}
	// return a dummy token
	token := "token-" + req.Username + "-" + randHex(8)
	c.JSON(http.StatusOK, gin.H{"token": token})
}

func handleListEvents(c *gin.Context) {
	pageStr := c.DefaultQuery("page", "1")
	sizeStr := c.DefaultQuery("size", "10")

	page, err := strconv.Atoi(pageStr)
	if err != nil || page < 1 {
		page = 1
	}
	size, err := strconv.Atoi(sizeStr)
	if err != nil || size < 1 {
		size = 10
	}

	total := len(events)
	start := (page - 1) * size
	if start > total {
		c.JSON(http.StatusOK, gin.H{"page": page, "size": size, "total": total, "events": []Event{}})
		return
	}
	end := start + size
	if end > total {
		end = total
	}
	c.JSON(http.StatusOK, gin.H{"page": page, "size": size, "total": total, "events": events[start:end]})
}

func handleGetEvent(c *gin.Context) {
	idStr := c.Param("id")
	id, err := strconv.Atoi(idStr)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}
	for _, e := range events {
		if e.ID == id {
			c.JSON(http.StatusOK, e)
			return
		}
	}
	c.JSON(http.StatusNotFound, gin.H{"error": "event not found"})
}

func handleDownload(c *gin.Context) {
	// stream random bytes to simulate a large file
	// optional query param `mb` sets size in MiB. Default 1024 MiB (1 GiB).
	mbStr := c.DefaultQuery("mb", "1024")
	mb, err := strconv.Atoi(mbStr)
	if err != nil || mb < 1 {
		mb = 1024
	}
	totalBytes := int64(mb) * 1024 * 1024

	c.Header("Content-Type", "application/octet-stream")
	c.Header("Content-Disposition", "attachment; filename=download.bin")
	c.Header("Content-Length", strconv.FormatInt(totalBytes, 10))

	// copy from crypto/rand.Reader directly to ResponseWriter
	_, copyErr := io.CopyN(c.Writer, rand.Reader, totalBytes)
	if copyErr != nil && copyErr != io.EOF {
		// connection may be closed by client; just log error to response if still possible
		c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": "failed to stream"})
		return
	}
}

func randHex(n int) string {
	b := make([]byte, n)
	_, _ = rand.Read(b)
	return hex.EncodeToString(b)
}
