package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    private var usedWords: MutableSet<String> = mutableSetOf()

    // 読み取り専用
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private lateinit var currentWord: String
    var userGuess by mutableStateOf("")
        private set

    init {
        resetGame()
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }

    fun updateUserGuess(guessWord: String) {
        userGuess = guessWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updateScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updateScore)
        } else {
            _uiState.update { currentState -> currentState.copy(isGuessedWordWrong = true) }
        }
        updateUserGuess("")
    }

    private fun updateGameState(updateScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updateScore,
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updateScore,
                    currentWordCount = currentState.currentWordCount.inc()
                )
            }
        }
    }

    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        return if (usedWords.contains(currentWord)) {
            pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tmpWord = word.toCharArray()
        tmpWord.shuffle()
        while (String(tmpWord) == word) {
            tmpWord.shuffle()
        }
        return String(tmpWord)
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }
}