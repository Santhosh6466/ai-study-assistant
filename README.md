# AI Study Assistant

A Spring Boot backend that uses **Spring AI** to help CS students study — get concepts explained simply, summarize notes, generate practice quizzes, and have a tutoring conversation that remembers context. Built as a hands-on project while preparing for placements, to go beyond just watching tutorials and actually implement each core Spring AI concept end-to-end.

Runs fully locally using **Ollama** — no API keys, no cloud costs, no external dependencies.

---

## Features

| Endpoint | What it does | Spring AI concept demonstrated |
|---|---|---|
| `GET /tutor` | Ask any CS/DSA concept, get a short, simple explanation | System messages (persona/behavior control) |
| `GET /summarize` | Paste study notes, get a 2-sentence summary | Prompt templates (dynamic content injection) |
| `GET /generate-quiz` | Give a topic, get back a structured 5-question multiple-choice quiz | Structured output (`.entity()` — typed Java objects, not raw text) |
| `GET /study-chat` | An ongoing tutoring conversation that remembers what was discussed earlier | Chat Memory (`MessageChatMemoryAdvisor`) |

---

## Tech Stack

- **Java 17+**
- **Spring Boot**
- **Spring AI** (`spring-ai-ollama-spring-boot-starter`)
- **Ollama** — running locally, model: `qwen3:8b`

---

## Architecture

Every endpoint flows through the same core pattern:

```
Controller (@RestController)
      |
      v
ChatClient.prompt()...call()      <- fluent API builds the request
      |
      v
Advisor Chain (ChatMemory, when applicable)
      |
      v
ChatModel (OllamaChatModel)       <- portable, provider-agnostic interface
      |
      v
Ollama local server (localhost:11434)
      |
      v
qwen3:8b model
```

`ChatClient` is configured **once** in the controller's constructor (via `ChatClient.Builder`), including the memory advisor. Every request then reuses that same, already-built client — build once, call many times.

---

## Setup

**1. Install and run Ollama, pull the model:**
```bash
ollama pull qwen3:8b
ollama run qwen3:8b
```
Exit the interactive chat with `/bye` once confirmed — Ollama's server keeps running in the background on port `11434`.

**2. `application.properties`:**
```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen3:8b
spring.ai.ollama.chat.options.temperature=0.7
spring.ai.ollama.init.pull-model-strategy=never
```

**3. Run the Spring Boot app** — confirm you see `Started ChatBotApplication` with no errors.

---

## API Examples

**Ask a DSA concept:**
```bash
curl "http://localhost:8080/tutor?question=what is a hashmap"
```

**Summarize notes:**
```bash
curl "http://localhost:8080/summarize?notes=<paste long text here>"
```

**Generate a quiz:**
```bash
curl "http://localhost:8080/generate-quiz?topic=binary search trees"
```
Returns structured JSON:
```json
{
  "topic": "binary search trees",
  "questions": [
    {
      "question": "What is the time complexity of search in a balanced BST?",
      "options": ["O(1)", "O(log n)", "O(n)", "O(n^2)"],
      "correctAnswer": "O(log n)"
    }
  ]
}
```

**Study chat with memory:**
```bash
curl "http://localhost:8080/study-chat?message=explain sliding window&conversationId=user1"
curl "http://localhost:8080/study-chat?message=give me an example&conversationId=user1"
```
The second call correctly builds on the first, since conversation history is passed with every request under the same `conversationId`. Using a different `conversationId` starts a completely isolated conversation.

---

## What I Learned Building This

- The distinction between `ChatClient.Builder` (one-time configuration) and `ChatClient.prompt()` (per-request execution)
- Why `ChatClient` is a fluent facade built on top of the lower-level, provider-agnostic `ChatModel` interface
- How prompt templates cleanly separate static instructions from dynamic user data
- How structured output (`.entity()`) generates a JSON schema from a Java record and maps the model's response directly into typed objects
- That LLMs are stateless — "memory" is the application re-sending conversation history with every request, not the model actually remembering anything

---

## Roadmap / Next Steps

- [ ] **RAG** — add `/ask-my-notes`: upload a PDF of study notes, ask questions grounded in that specific document (embeddings + vector store + retrieval)
- [ ] Add basic tests for each endpoint
- [ ] Add a minimal frontend (or keep as a Postman-testable API)

---

## Why This Project

Built while preparing for placements, alongside DSA practice — the goal was to genuinely understand Spring AI by implementing every core concept myself, rather than just watching tutorials. Each endpoint maps to something I can explain and defend in an interview, not just code that happens to work.
