package com.AI.chatBot.controller;

import com.AI.chatBot.structures.Quiz;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, ChatMemory chatMemory){
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
    @GetMapping("/tutor")
    public String tutor(@RequestParam String question){
        return chatClient.prompt()
                .system("You are a patient DSA tutor. Keep every answer under 3 sentences. Use simple language.")
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/summarize")
    public String summarize(@RequestParam String notes){
        String template = "Summarize the following study notes in 2 sentences: {notes}";
        return chatClient.prompt()
                .user(u -> u.text(template).param("notes", notes))
                .call()
                .content();
    }

    @GetMapping("/generate-quiz")
    public Quiz generateQuiz(@RequestParam String topic) {
        String template = "Generate exactly 5 multiple-choice questions on the topic: {topic}. " +
                "Each question should have 4 options and clearly indicate the correct answer.";

        return chatClient.prompt()
                .system("You are a CS professor creating a practice quiz for a student preparing for placements.")
                .user(u -> u.text(template).param("topic", topic))
                .call()
                .entity(Quiz.class);
    }
    
    @GetMapping("/study-chat")
    public String studyChat(@RequestParam String message,
                            @RequestParam String conversationId){
        return chatClient.prompt()
                .system("You are a helpful, patient CS study companion.")
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}