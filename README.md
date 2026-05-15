# Build-Logic

Este repositório possui arquivos `.gradle` designados para serem usados juntos de [gradle-modelo](https://github.com/EttoreK/Gradle-Modelo.git) e [gradle-control](https://github.com/EttoreK/Gradle-Control.git). O principal objetivo desse repositório é contribuir com comandos úteis e práticos para o desenvolvimento de aplicativos.

## Nota

As instruções para o adição desse repositório no projeto estão em [gradle-modelo](https://github.com/EttoreK/Gradle-Modelo.git).

# Comandos

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

7. Sincroniza os arquivos fonte para a VM Linux via SSH:

```shell
./gradlew syncToLinux
```

8. Sincroniza os arquivos fonte para a VM Windows via SSH:

```shell
./gradlew syncToWindows
```

9. Limpa os diretórios de build C++ localmente e nas VMs (via SSH):

```shell
./gradlew cleanC
```

10. Converte os arquivos do diretório espolio/assets em um Header C++:

```shell
./gradlew criaEspolio
```

11. Instala dependências do Pacman via MSYS2 Bash:

```shell
./gradlew instalaDeps
```

12. Compila nativamente, ou via SSH em para o alvo desejado:

```shell
./gradlew build['Win' | 'Lnx']['64' | '32']['' | 'Release']
```

13. Executa o binário do alvo desejado:

```shell
./gradlew run['Win' | 'Lnx']['64' | '32']['' | 'Release']
```
14. Testes unitários do alvo desejado:

```shell
./gradlew test['Win' | 'Lnx']['64' | '32']['' | 'Release']
```

15. Administra a conversão e compilação do ícone do Windows:

```shell
./gradlew winIco
```

16. Gera o ícone PNG do aplicativo para Linux:

```shell
./gradlew lnxIco
```

17. Cria um `.jar`gordo para o SpringBoot:

```shell
./gradlew bootJar
```

17. Cria um `.jar`distribuido para o SpringBoot:

```shell
./gradlew orderJar
```

18. Copia as dependências para a pasta `lib/`:

```shell
./gradlew orderLibs
```

19. Limpa o(s) `.jar` para o aplicativo SpringBoot:

```shell
./gradlew cleanWeb
```

20. Usa técnicas de minificação para encolher o `.jar` do SpringBoot:

```shell
./gradlew minWebJar
```

21. Constroi o `.jar` para o aplicativo SpringBoot, usando bootJar como padrão ou orderJar ao usar `-Pthin=true`:

```shell
./gradlew buildWeb [-Pthin=true]
```

22. Executa se o `.jar` na pasta libs:

```shell
./gradlew runWeb
```

23. Realiza os testes para o SpringBoot:

```shell
./gradlew testWeb
```

24. Gera o icone.png para a SpringBoot:

```shell
./gradlew webIco
```

25. Sobrescreve o .classpath do VS Code incluindo dependências do SpringBoot:

```shell
./gradlew posBuildWeb
```

26. Garante que o diretório existe, sincroniza os arquivos e ajusta as permissões na VM Mac:

```shell
./gradlew syncToMac
```

27. Limpa os arquivos de build do Xcode para iOS:

```shell
./gradlew cleanMac
```

28. Compila o projeto Xcode para o alvo:

```shell
./gradlew build['Macos' | 'Ios']['' | 'Release']
```

29. Executa o app compilado para o alvo:

```shell
./gradlew run['Macos' | 'Ios']['' | 'Release']
```

30. Executa os testes para o alvo:

```shell
./gradlew test['Macos' | 'Ios']['Unit' | 'Ui']
```

31. Cria um arquivo .xcarchive para ao alvo (Release):

```shell
./gradlew archive['Macos' | 'Ios']
```

32. Exporta o apliativo.xcarchive para AppStore

```shell
./gradlew export['Macos' | 'Ios']
```

33. Envia o pacote alvo exportado para o App Store Connect

```shell
./gradlew submit['Macos' | 'Ios']
```

34. Gera os arquivos do projeto .xcodeproj

```shell
./gradlew xproj
```

35. Gera o Assets.xcassets para o Xcode

```shell
./gradlew appleIco
```