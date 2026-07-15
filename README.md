# Build-Logic

Este repositório possui arquivos `.gradle` designados para serem usados juntos de [gradle-modelo](https://github.com/EttoreK/Gradle-Modelo.git) e [gradle-control](https://github.com/EttoreK/Gradle-Control.git). O principal objetivo desse repositório é contribuir com comandos úteis e práticos para o desenvolvimento de aplicativos.

## Nota

As instruções para o adição desse repositório no projeto estão em [gradle-modelo](https://github.com/EttoreK/Gradle-Modelo.git).

## Comandos

Use `graldew` para Windows e `./gradlew` para Unix

### Android (Java)

1. Limpa os builds anteriores do Android:

```shell
./gradlew cleanAndroid
```

2. Sobereescreve o .classpath incluindo módulos e dependências:

```shell
./gradlew posBuild
```

3. Monta o projeto Android (Debug e Release):

```shell
./gradlew buildAndroid
```

4. Executa o simulador Android e instala o programa:

```shell
./gradlew runSimulatorAndroid
```

5. Executa o simulador Android, instala e executa o programa:

```shell
./gradlew runAndroid
```

6. Roda os testes unitários do Android:

```shell
./gradlew testAndroid
```

### Desktop (C++)

01. Sincroniza os arquivos fonte para a VM Linux via SSH:

```shell
./gradlew syncToLinux
```

02. Sincroniza os arquivos fonte para a VM Windows via SSH:

```shell
./gradlew syncToWindows
```

03. Limpa os diretórios de build C++ localmente e nas VMs (via SSH):

```shell
./gradlew cleanC
```

04. Converte os arquivos do diretório espolio/assets em um Header C++:

```shell
./gradlew criaEspolio
```

05. Instala dependências do Pacman via MSYS2 Bash:

```shell
./gradlew instalaDeps
```

06. Compila nativamente, ou via SSH em para o alvo desejado:

```shell
./gradlew build['Win' | 'Lin']['64' | '32']['' | 'Release']
```

07. Executa o binário do alvo desejado:

```shell
./gradlew run['Win' | 'Lin']['64' | '32']['' | 'Release']
```
08. Testes unitários do alvo desejado:

```shell
./gradlew test['Win' | 'Lin']['64' | '32']['' | 'Release']
```

09. Administra a conversão e compilação do ícone do Windows:

```shell
./gradlew winIco
```

10. Gera o ícone PNG do aplicativo para Linux:

```shell
./gradlew linIco
```

### Web (Java)

01. Cria um `.jar`gordo para o SpringBoot:

```shell
./gradlew bootJar
```

02. Cria um `.jar`distribuido para o SpringBoot:

```shell
./gradlew orderJar
```

03. Copia as dependências para a pasta `lib/`:

```shell
./gradlew orderLibs
```

04. Limpa o(s) `.jar` para o aplicativo SpringBoot:

```shell
./gradlew cleanWeb
```

05. Usa técnicas de minificação para encolher o `.jar` do SpringBoot:

```shell
./gradlew minWebJar
```

06. Constroi o `.jar` para o aplicativo SpringBoot, usando bootJar como padrão ou orderJar ao usar `-Pthin=true`:

```shell
./gradlew buildWeb [-Pthin=true]
```

07. Executa se o `.jar` na pasta libs:

```shell
./gradlew runWeb
```

08. Realiza os testes para o SpringBoot:

```shell
./gradlew testWeb
```

09. Gera o icone.png para a SpringBoot:

```shell
./gradlew webIco
```

10. Sobrescreve o .classpath do VS Code incluindo dependências do SpringBoot:

```shell
./gradlew posBuildWeb
```

11. Envia para o servidor e reinicia o serviço

```shell
./gradlew submitWeb
```

### Darwin (Swift)

01. Garante que o diretório existe, sincroniza os arquivos e ajusta as permissões na VM Mac:

```shell
./gradlew syncToMac
```

02. Limpa os arquivos de build do Xcode para iOS:

```shell
./gradlew cleanMac
```

03. Compila o projeto Xcode para o alvo:

```shell
./gradlew build['Macos' | 'Ios']['' | 'Release']
```

04. Executa o app compilado para o alvo:

```shell
./gradlew run['Macos' | 'Ios']['' | 'Release']
```

05. Executa os testes específicos para o alvo:

```shell
./gradlew test['Macos' | 'Ios']['Intg' | 'Unit' | 'Ui']
```

06. Executa todos testes para o alvo:

```shell
./gradlew test['Macos' | 'Ios']
```

07. Cria um arquivo .xcarchive para ao alvo (Release):

```shell
./gradlew archive['Macos' | 'Ios']
```

08. Exporta o apliativo.xcarchive para AppStore

```shell
./gradlew export['Macos' | 'Ios']
```

09. Envia o pacote alvo exportado para o App Store Connect

```shell
./gradlew submit['Macos' | 'Ios']
```

10. Gera os arquivos do projeto .xcodeproj

```shell
./gradlew xproj
```

11. Gera o Assets.xcassets para o Xcode

```shell
./gradlew xIcone
```