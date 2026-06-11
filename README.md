# Spring AI Chat API

A conversational chat API built with **Spring Boot 4.1**, **Spring AI 2.0**, and **Google Gemini**. Features JWT authentication, persistent chat sessions with full conversation memory, configurable AI agents, and a clean RESTful API.

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring AI 2.0.0-RC2 (Google GenAI / Gemini)
- Spring Security + JWT (jjwt 0.12.7)
- Spring Data JPA + PostgreSQL
- Lombok

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  Controller Layer (REST API)                        │
│  ├── AuthController    /api/v1/auth/**              │
│  ├── AgentController   /api/v1/agents/**            │
│  └── ChatController    /api/v1/chat/**              │
├─────────────────────────────────────────────────────┤
│  Service Layer                                      │
│  ├── AuthService       (register, login, JWT)       │
│  └── ChatService       (sessions, messages, AI)     │
├─────────────────────────────────────────────────────┤
│  Spring AI Integration                              │
│  └── ChatClient + MessageChatMemoryAdvisor          │
│      (conversation history rebuilt from DB)          │
├─────────────────────────────────────────────────────┤
│  Data Layer                                         │
│  ├── Users, Roles                                   │
│  ├── AiAgents (configurable system prompt + temp)   │
│  ├── ChatSessions                                   │
│  └── ChatMessages                                   │
└─────────────────────────────────────────────────────┘
```

## Prerequisites

- Java 21+
- PostgreSQL 15+
- A Google Gemini API key ([Get one here](https://aistudio.google.com/apikey))

## Setup

### 1. Clone and navigate

```bash
git clone <repo-url>
cd spring-ai-chat-api
```

### 2. Create PostgreSQL database

```sql
CREATE DATABASE "spring-ai-chat-db";
```

### 3. Configure environment variables

Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

Edit `.env`:

```env
GEMINI_API_KEY=your-gemini-api-key-here
DB_URL=jdbc:postgresql://localhost:5432/spring-ai-chat-db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=replace-with-very-long-secret-key-at-least-32-characters
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The app starts on **port 8081**. On first startup, a default AI agent ("Gemini Assistant") is automatically seeded.

## API Reference

Base URL: `http://localhost:8081`

All responses follow this structure:

```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "timestamp": "2026-06-11T10:30:00"
}
```

### CORS

CORS is configured to allow requests from `http://localhost:3000` (frontend dev server).

---

### Authentication

#### Register

```
POST /api/v1/auth/register
```

**Request:**

```json
{
  "email": "john@example.com",
  "password": "Test1234!",
  "confirmPassword": "Test1234!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201):**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "uuid",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

#### Login

```
POST /api/v1/auth/login
```

**Request:**

```json
{
  "email": "john@example.com",
  "password": "Test1234!"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

---

### AI Agents (Requires Authentication)

All agent endpoints require `Authorization: Bearer <accessToken>`.

#### Create Agent

```
POST /api/v1/agents
```

**Request:**

```json
{
  "name": "Code Reviewer",
  "description": "An AI agent specialized in reviewing code.",
  "systemPrompt": "You are an expert code reviewer. Analyze code for bugs, performance, and security.",
  "temperature": 0.3,
  "enabled": true
}
```

#### List All Agents

```
GET /api/v1/agents
```

#### Get Agent by ID

```
GET /api/v1/agents/{agentId}
```

#### Update Agent

```
PUT /api/v1/agents/{agentId}
```

#### Delete Agent

```
DELETE /api/v1/agents/{agentId}
```

---

### Chat (Requires Authentication)

All chat endpoints require `Authorization: Bearer <accessToken>`.

#### Start a New Session

Sends the first message and creates a new chat session in one call. Optionally pass `agentId` to select a specific agent.

```
POST /api/v1/chat/sessions
```

**Request (with specific agent):**

```json
{
  "message": "Hello! What can you help me with?",
  "agentId": "uuid-of-agent"
}
```

**Request (default agent):**

```json
{
  "message": "Hello! What can you help me with?"
}
```

**Response (201):**

```json
{
  "success": true,
  "message": "Session started successfully",
  "data": {
    "sessionId": "uuid",
    "userMessage": {
      "id": "uuid",
      "content": "Hello! What can you help me with?",
      "senderType": "USER",
      "sentAt": "2026-06-11T10:30:00"
    },
    "aiMessage": {
      "id": "uuid",
      "content": "Hi! I'm your AI assistant...",
      "senderType": "AI_AGENT",
      "sentAt": "2026-06-11T10:30:01"
    }
  }
}
```

#### Send a Message (Continue Session)

```
POST /api/v1/chat/sessions/{sessionId}/messages
```

**Request:**

```json
{
  "message": "Tell me about Spring Boot"
}
```

**Response (200):** Same structure as above.

#### List All Sessions

```
GET /api/v1/chat/sessions
```

**Response (200):**

```json
{
  "success": true,
  "message": "Sessions retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "title": "Hello! What can you help me with?",
      "agentName": "Gemini Assistant",
      "createdAt": "2026-06-11T10:30:00",
      "updatedAt": "2026-06-11T10:35:00"
    }
  ]
}
```

#### Get Messages for a Session

```
GET /api/v1/chat/sessions/{sessionId}/messages
```

#### Delete a Session

```
DELETE /api/v1/chat/sessions/{sessionId}
```

---

## Testing A-Z with cURL

### Step 1: Register a user

```bash
curl -s -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "confirmPassword": "Test1234!",
    "firstName": "Test",
    "lastName": "User"
  }' | jq .
```

### Step 2: Login and capture the token

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!"
  }' | jq -r '.data.accessToken')

echo "Token: $TOKEN"
```

### Step 3: List available agents

```bash
curl -s -X GET http://localhost:8081/api/v1/agents \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Step 4: Create a custom agent

```bash
AGENT_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/agents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Code Helper",
    "description": "Helps with coding questions",
    "systemPrompt": "You are a helpful coding assistant. Give concise answers with code examples.",
    "temperature": 0.5,
    "enabled": true
  }')

echo "$AGENT_RESPONSE" | jq .

AGENT_ID=$(echo "$AGENT_RESPONSE" | jq -r '.data.id')
echo "Agent ID: $AGENT_ID"
```

### Step 5: Start a chat session with the custom agent

```bash
SESSION_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/chat/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"message\": \"What is a HashMap in Java?\",
    \"agentId\": \"$AGENT_ID\"
  }")

echo "$SESSION_RESPONSE" | jq .

SESSION_ID=$(echo "$SESSION_RESPONSE" | jq -r '.data.sessionId')
echo "Session ID: $SESSION_ID"
```

### Step 6: Continue the conversation

```bash
curl -s -X POST "http://localhost:8081/api/v1/chat/sessions/$SESSION_ID/messages" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "message": "How is it different from TreeMap?"
  }' | jq .
```

### Step 7: Send another follow-up (tests memory)

```bash
curl -s -X POST "http://localhost:8081/api/v1/chat/sessions/$SESSION_ID/messages" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "message": "Can you give me a code example comparing both?"
  }' | jq .
```

### Step 8: List all sessions

```bash
curl -s -X GET http://localhost:8081/api/v1/chat/sessions \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Step 9: Get full message history

```bash
curl -s -X GET "http://localhost:8081/api/v1/chat/sessions/$SESSION_ID/messages" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Step 10: Delete the session

```bash
curl -s -X DELETE "http://localhost:8081/api/v1/chat/sessions/$SESSION_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Step 11: Verify deletion

```bash
curl -s -X GET http://localhost:8081/api/v1/chat/sessions \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## Postman / Bruno

Import the collection and environment from `docs/api/`:

- `spring-ai-chat-endpoints.json` — full API collection
- `spring-ai-chat-environments.json` — environment variables

Set your `accessToken` after login, and `sessionId`/`agentId` as needed.

---

## Error Responses

| Status | Scenario |
|--------|----------|
| 400 | Validation errors (blank message, invalid email, etc.) |
| 401 | Missing or invalid/expired JWT token |
| 403 | Accessing another user's session |
| 404 | Session not found, User not found, Agent not found |
| 409 | Email already registered |

---

## Project Structure

```
src/main/java/com/tharidulakmal/springaichatapi/
├── config/
│   ├── DataInitializer.java        # Seeds default AI agent on startup
│   ├── JpaAuditingConfig.java      # Enables @CreatedDate/@LastModifiedDate
│   └── SecurityBeansConfig.java    # PasswordEncoder bean
├── controller/
│   ├── auth/AuthController.java    # Register & Login
│   └── chat/
│       ├── AgentController.java    # CRUD for AI agents
│       └── ChatController.java     # Chat session & message endpoints
├── dto/
│   ├── request/                    # Incoming request records
│   └── response/                   # Outgoing response records
├── entity/
│   ├── BaseEntity.java             # UUID id + audit timestamps
│   ├── auth/User.java, Role.java
│   └── chat/AiAgent.java, ChatSession.java, ChatMessage.java, SenderType.java
├── exception/                      # Global exception handling
├── repository/                     # Spring Data JPA repositories
├── security/                       # JWT filter, UserDetails, SecurityConfig, CORS
├── service/                        # Business logic
└── util/ApiResponse.java           # Standard response wrapper
```

## Key Design Decisions

- **Conversation memory from DB**: On each request, past messages are loaded from PostgreSQL into Spring AI's `MessageWindowChatMemory`. Durable memory without extra infrastructure — survives restarts.
- **Session-per-conversation**: Each session maps to one conversation thread with its own history.
- **Configurable AI agents**: Create multiple agents with different system prompts and temperature. Select which agent to use when starting a chat.
- **Stateless auth**: JWT-based with access + refresh tokens. No server-side session storage.
- **CORS ready**: Configured for `http://localhost:3000` frontend.

## License

MIT
