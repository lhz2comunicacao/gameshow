# Jogo da Forca (Android)

Aplicativo simples de jogo da forca em Kotlin/Jetpack Compose. O usuário seleciona um arquivo de texto (`.txt`) com uma palavra por linha, e o app escolhe uma palavra aleatória para iniciar a partida.

## Como gerar o APK (sem assinatura da Play Store)

```bash
./gradlew assembleDebug assembleRelease
```

Também é possível usar o script auxiliar:

```bash
./scripts/build_apks.sh
```

Para acompanhar todo o processo em detalhes (limpar, compilar, listar e validar hash dos APKs):

```bash
./scripts/build_and_verify_apks.sh
```

Os APKs serão gerados em:

```
app/build/outputs/apk/debug/
app/build/outputs/apk/release/app-release.apk
```

O APK de release não é assinado para publicação na Play Store. O APK de debug é assinado automaticamente com a chave de debug do Android. Para instalar manualmente em um dispositivo, habilite a opção de instalação de fontes desconhecidas.

## Build automatizado (YAML)

Há um workflow GitHub Actions em `.github/workflows/build-apk.yml` que compila os APKs de debug e release e publica os artefatos. Ele pode ser executado manualmente em **Actions** (workflow_dispatch) para acompanhar todo o processo até a compilação.

> Observação: este ambiente não executa o GitHub Actions diretamente. Use o workflow no repositório para acompanhar o build no GitHub e baixar os APKs gerados como artefatos.

## Compatibilidade com processadores

O app não usa bibliotecas nativas (NDK). A configuração gera APKs por arquitetura e também um APK universal compatível com ARM, ARM64, x86 e x86_64. Os APKs universais ficam em `app/build/outputs/apk/{debug,release}/app-*-universal.apk`.

Compatibilidade mínima: Android 10 (API 29) ou superior.

## Como jogar

1. Toque em **Selecionar arquivo** e escolha um arquivo `.txt` com uma palavra por linha.
2. Use o campo **Letra** para tentar letras e toque em **Chutar**.
3. Use **Nova palavra** para sortear outra palavra do mesmo arquivo.
