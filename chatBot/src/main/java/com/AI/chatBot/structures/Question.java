package com.AI.chatBot.structures;

import java.util.List;

public record Question(String question, List<String> options, String correctAnswer) {
}