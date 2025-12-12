package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func setupRouter() *gin.Engine {
	gin.SetMode(gin.TestMode)
	seedData() // Initialize data for tests
	r := gin.Default()
	r.POST("/login", handleLogin)
	r.GET("/events", handleListEvents)
	r.GET("/events/:id", handleGetEvent)
	r.GET("/download/:file_id", handleDownload)
	return r
}

func TestHandleLogin(t *testing.T) {
	r := setupRouter()

	// Valid login
	body := `{"username":"admin","password":"password"}`
	req, _ := http.NewRequest("POST", "/login", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var resp map[string]string
	json.Unmarshal(w.Body.Bytes(), &resp)
	assert.Contains(t, resp["token"], "token-admin-")

	// Invalid login
	body = `{"username":"admin","password":"wrong"}`
	req, _ = http.NewRequest("POST", "/login", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	w = httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusUnauthorized, w.Code)
	var errResp map[string]string
	json.Unmarshal(w.Body.Bytes(), &errResp)
	assert.Equal(t, "invalid credentials", errResp["error"])
}

func TestHandleListEvents(t *testing.T) {
	r := setupRouter()

	req, _ := http.NewRequest("GET", "/events?page=1&size=5", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var resp map[string]interface{}
	json.Unmarshal(w.Body.Bytes(), &resp)
	assert.Equal(t, float64(1), resp["page"])
	assert.Equal(t, float64(5), resp["size"])
	assert.Equal(t, float64(200), resp["total"])
	events := resp["events"].([]interface{})
	assert.Len(t, events, 5)
}

func TestHandleGetEvent(t *testing.T) {
	r := setupRouter()

	// Valid event
	req, _ := http.NewRequest("GET", "/events/1", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var event Event
	json.Unmarshal(w.Body.Bytes(), &event)
	assert.Equal(t, 1, event.ID)
	assert.Equal(t, "Event #001", event.Title)

	// Invalid event
	req, _ = http.NewRequest("GET", "/events/999", nil)
	w = httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
	var errResp map[string]string
	json.Unmarshal(w.Body.Bytes(), &errResp)
	assert.Equal(t, "event not found", errResp["error"])
}

func TestHandleDownload(t *testing.T) {
	r := setupRouter()

	req, _ := http.NewRequest("GET", "/download/1?mb=1", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	assert.Equal(t, "application/octet-stream", w.Header().Get("Content-Type"))
	assert.Equal(t, "attachment; filename=download.bin", w.Header().Get("Content-Disposition"))
	assert.Equal(t, "1048576", w.Header().Get("Content-Length"))

	// Check body length (1 MiB)
	body := w.Body.Bytes()
	assert.Len(t, body, 1024*1024)
}
