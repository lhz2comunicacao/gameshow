package com.example.forca

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ForcaScreen()
                }
            }
        }
    }
}

private const val MAX_ERRORS = 6

@Composable
fun ForcaScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wordList = remember { mutableStateOf<List<String>>(emptyList()) }
    val currentWord = remember { mutableStateOf("") }
    val guessedLetters = remember { mutableStateOf(setOf<Char>()) }
    val wrongGuesses = remember { mutableStateOf(0) }
    val statusMessage = remember { mutableStateOf("Selecione um arquivo de texto com uma palavra por linha.") }
    val inputLetter = remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val loaded = withContext(Dispatchers.IO) {
                    readLinesFromUri(context.contentResolver, uri)
                }
                val cleaned = loaded.map { it.trim() }.filter { it.isNotEmpty() }
                if (cleaned.isEmpty()) {
                    statusMessage.value = "Arquivo sem palavras válidas."
                } else {
                    wordList.value = cleaned
                    startNewRound(wordList, currentWord, guessedLetters, wrongGuesses, statusMessage)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Jogo da Forca",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(text = statusMessage.value)

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { launcher.launch(arrayOf("text/plain")) }) {
                Text(text = "Selecionar arquivo")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (wordList.value.isNotEmpty()) {
                        startNewRound(wordList, currentWord, guessedLetters, wrongGuesses, statusMessage)
                    } else {
                        statusMessage.value = "Carregue um arquivo primeiro."
                    }
                }
            ) {
                Text(text = "Nova palavra")
            }
        }

        if (currentWord.value.isNotEmpty()) {
            Text(text = "Erros: ${wrongGuesses.value} / $MAX_ERRORS")
            Text(
                text = maskedWord(currentWord.value, guessedLetters.value),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = "Letras usadas: ${guessedLetters.value.sorted().joinToString(", ")}")

            OutlinedTextField(
                value = inputLetter.value,
                onValueChange = { value ->
                    inputLetter.value = value.take(1).uppercase()
                },
                label = { Text(text = "Letra") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier.width(120.dp)
            )

            Button(
                onClick = {
                    submitGuess(
                        inputLetter,
                        currentWord,
                        guessedLetters,
                        wrongGuesses,
                        statusMessage
                    )
                }
            ) {
                Text(text = "Chutar")
            }
        }
    }

    LaunchedEffect(wrongGuesses.value, guessedLetters.value, currentWord.value) {
        checkGameState(currentWord, guessedLetters, wrongGuesses, statusMessage)
    }
}

private fun readLinesFromUri(contentResolver: android.content.ContentResolver, uri: Uri): List<String> {
    return contentResolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
        lines.toList()
    } ?: emptyList()
}

private fun startNewRound(
    wordList: MutableState<List<String>>,
    currentWord: MutableState<String>,
    guessedLetters: MutableState<Set<Char>>,
    wrongGuesses: MutableState<Int>,
    statusMessage: MutableState<String>
) {
    val word = wordList.value.random().uppercase()
    currentWord.value = word
    guessedLetters.value = emptySet()
    wrongGuesses.value = 0
    statusMessage.value = "Nova palavra sorteada!"
}

private fun submitGuess(
    inputLetter: MutableState<String>,
    currentWord: MutableState<String>,
    guessedLetters: MutableState<Set<Char>>,
    wrongGuesses: MutableState<Int>,
    statusMessage: MutableState<String>
) {
    val letter = inputLetter.value.trim().uppercase()
    if (letter.isEmpty()) {
        statusMessage.value = "Digite uma letra."
        return
    }
    val char = letter.first()
    if (!char.isLetter()) {
        statusMessage.value = "Use apenas letras."
        inputLetter.value = ""
        return
    }
    if (guessedLetters.value.contains(char)) {
        statusMessage.value = "Você já tentou essa letra."
        inputLetter.value = ""
        return
    }

    guessedLetters.value = guessedLetters.value + char
    if (!currentWord.value.contains(char)) {
        wrongGuesses.value = wrongGuesses.value + 1
        statusMessage.value = "Letra errada."
    } else {
        statusMessage.value = "Boa!"
    }
    inputLetter.value = ""
}

private fun maskedWord(word: String, guessedLetters: Set<Char>): String {
    return word.map { char ->
        if (!char.isLetter()) {
            char
        } else if (guessedLetters.contains(char)) {
            char
        } else {
            '_'
        }
    }.joinToString(" ")
}

private fun checkGameState(
    currentWord: MutableState<String>,
    guessedLetters: MutableState<Set<Char>>,
    wrongGuesses: MutableState<Int>,
    statusMessage: MutableState<String>
) {
    if (currentWord.value.isEmpty()) {
        return
    }
    if (wrongGuesses.value >= MAX_ERRORS) {
        statusMessage.value = "Fim de jogo! A palavra era ${currentWord.value}."
        return
    }
    val allGuessed = currentWord.value
        .filter { it.isLetter() }
        .all { guessedLetters.value.contains(it) }
    if (allGuessed) {
        statusMessage.value = "Você venceu!"
    }
}
