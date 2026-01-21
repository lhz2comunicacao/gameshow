package com.example.forca

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var errorsText: TextView
    private lateinit var wordText: TextView
    private lateinit var lettersText: TextView
    private lateinit var inputLetter: EditText
    private lateinit var guessButton: Button
    private lateinit var newWordButton: Button

    private var wordList: List<String> = emptyList()
    private var currentWord: String = ""
    private var guessedLetters: MutableSet<Char> = mutableSetOf()
    private var wrongGuesses: Int = 0

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val loaded = readLinesFromUri(contentResolver, it)
                .map { line -> line.trim() }
                .filter { line -> line.isNotEmpty() }
            if (loaded.isEmpty()) {
                setStatus(getString(R.string.status_invalid_file))
            } else {
                wordList = loaded
                startNewRound()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.text_status)
        errorsText = findViewById(R.id.text_errors)
        wordText = findViewById(R.id.text_word)
        lettersText = findViewById(R.id.text_letters)
        inputLetter = findViewById(R.id.input_letter)
        guessButton = findViewById(R.id.button_guess)
        newWordButton = findViewById(R.id.button_new_word)

        setStatus(getString(R.string.status_select_file))

        findViewById<Button>(R.id.button_select_file).setOnClickListener {
            filePicker.launch(arrayOf("text/plain"))
        }

        newWordButton.setOnClickListener {
            if (wordList.isNotEmpty()) {
                startNewRound()
            } else {
                setStatus(getString(R.string.status_load_file_first))
            }
        }

        guessButton.setOnClickListener {
            submitGuess()
        }

        refreshUi()
    }

    private fun readLinesFromUri(contentResolver: ContentResolver, uri: Uri): List<String> {
        return contentResolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
            lines.toList()
        } ?: emptyList()
    }

    private fun startNewRound() {
        currentWord = wordList.random().uppercase()
        guessedLetters = mutableSetOf()
        wrongGuesses = 0
        setStatus(getString(R.string.status_new_word))
        refreshUi()
    }

    private fun submitGuess() {
        val letter = inputLetter.text.toString().trim().uppercase()
        if (letter.isEmpty()) {
            setStatus(getString(R.string.status_enter_letter))
            return
        }
        val char = letter.first()
        if (!char.isLetter()) {
            setStatus(getString(R.string.status_only_letters))
            inputLetter.setText("")
            return
        }
        if (guessedLetters.contains(char)) {
            setStatus(getString(R.string.status_already_used))
            inputLetter.setText("")
            return
        }

        guessedLetters.add(char)
        if (!currentWord.contains(char)) {
            wrongGuesses += 1
            setStatus(getString(R.string.status_wrong_letter))
        } else {
            setStatus(getString(R.string.status_good_guess))
        }
        inputLetter.setText("")
        refreshUi()
        checkGameState()
    }

    private fun refreshUi() {
        errorsText.text = getString(R.string.errors_format, wrongGuesses, MAX_ERRORS)
        wordText.text = maskedWord(currentWord, guessedLetters)
        lettersText.text = getString(
            R.string.letters_format,
            guessedLetters.sorted().joinToString(", ")
        )
    }

    private fun maskedWord(word: String, guessedLetters: Set<Char>): String {
        if (word.isEmpty()) {
            return ""
        }
        return word.map { char ->
            when {
                !char.isLetter() -> char
                guessedLetters.contains(char) -> char
                else -> '_'
            }
        }.joinToString(" ")
    }

    private fun checkGameState() {
        if (currentWord.isEmpty()) {
            return
        }
        if (wrongGuesses >= MAX_ERRORS) {
            setStatus(getString(R.string.status_game_over, currentWord))
            return
        }
        val allGuessed = currentWord
            .filter { it.isLetter() }
            .all { guessedLetters.contains(it) }
        if (allGuessed) {
            setStatus(getString(R.string.status_win))
        }
    }

    private fun setStatus(message: String) {
        statusText.text = message
    }

    companion object {
        private const val MAX_ERRORS = 6
    }
}
