# Jogo da Forca (Android)

Aplicativo simples de jogo da forca em Kotlin/Jetpack Compose. O usuário seleciona um arquivo de texto (`.txt`) com uma palavra por linha, e o app escolhe uma palavra aleatória para iniciar a partida.

## Como gerar o APK (sem assinatura da Play Store)

```bash
./gradlew assembleRelease
```

O APK será gerado em:

```
app/build/outputs/apk/release/app-release.apk
```

Esse APK não é assinado para publicação na Play Store. Para instalar manualmente em um dispositivo, habilite a opção de instalação de fontes desconhecidas.

## Compatibilidade com processadores

O app não usa bibliotecas nativas (NDK), então o APK é universal e compatível com diferentes arquiteturas (ARM, ARM64, x86, x86_64) suportadas pelo Android.

## Como jogar

1. Toque em **Selecionar arquivo** e escolha um arquivo `.txt` com uma palavra por linha.
2. Use o campo **Letra** para tentar letras e toque em **Chutar**.
3. Use **Nova palavra** para sortear outra palavra do mesmo arquivo.
