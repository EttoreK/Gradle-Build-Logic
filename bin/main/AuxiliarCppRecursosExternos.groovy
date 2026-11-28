import groovy.io.FileType
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.gradle.api.GradleException

abstract class AuxiliarCppRecursosExternos {
    // Encapsula comandos locais (Windows/Linux) ou remotos via SSH para a VM
    static List<String> comandoRemoto(boolean eWindows, boolean eLinux, String osAlvo,
        List<String> entradaCmds, File projDir, String dono, String sede, String dirRemoto,
        String extDir = null
    ) {
        if (eWindows && osAlvo == 'linux') {
            if (!dono || !sede || !dirRemoto) {
                throw new GradleException('Credenciais não configuradas pra Linux')
            }
            return [
                'ssh', "${dono}@${sede}",
                "mkdir -p '${dirRemoto}' && cd '${dirRemoto}' && ${entradaCmds.join(' ')}"
            ]
        } else if (eLinux && osAlvo == 'windows') {
            if (!dono || !sede || !dirRemoto) {
                throw new GradleException('Credenciais não configuradas pra Windows')
            }
            return [
                'ssh', "${dono}@${sede}",
                "if not exist \"${dirRemoto}\" mkdir \"${dirRemoto}\" & cd /d \"${dirRemoto}\" && ${entradaCmds.join(' ')}"
            ]
        } else {
            String dirLocal = projDir.absolutePath
            if (eWindows) {
                if (extDir != null) {
                    return [
                        'cmd', '/c',
                        "set \"PATH=${extDir.replace('/', '\\')};%PATH%\" && " +
                        "cd /d \"${dirLocal}\" && ${entradaCmds.join(' ')}"
                    ]
                }
                return ['cmd', '/c', "cd /d \"${dirLocal}\" && ${entradaCmds.join(' ')}"]
            }
            return ['bash', '-c', "cd '${dirLocal}' && ${entradaCmds.join(' ')}"]
        }
    }
    // Copia recursivamente os recursos de asseios para a pasta de destino
    static void copiaRes(File asseiosDir, File destinoDir, String asseiosNome) {
        if (!asseiosDir.exists()) {
            println ">> AVISO: Pasta ${asseiosNome} não encontrada em: ${asseiosDir}"
            return
        }

        File recursDir = new File(destinoDir, 'recursos')
        if (!recursDir.exists()) { recursDir.mkdirs() }

        println ">> INFO: Copiando recursos de '${asseiosDir.name}' para '${recursDir.name}'..."
        asseiosDir.eachFileRecurse { File arq ->
            File destArq = new File(
                recursDir, asseiosDir.toPath().relativize(arq.toPath()).toString()
            )
            if (arq.isDirectory()) {
                destArq.mkdirs()
            } else {
                destArq.parentFile.mkdirs()
                Files.copy(arq.toPath(), destArq.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
    // Gera ícone PNG 64x64 a partir dos SVGs de asseios (nome livre, fallback 'favicon')
    static void geraFavicon(File asseiosDir, File genDir, boolean eWindows, String winDisc,
        String nomeIcone = 'favicon'
    ) {
        File fundoSVG = new File(asseiosDir, 'icones/appBackground.svg')
        File frenteSVG = new File(asseiosDir, 'icones/appForeground.svg')
        if (!fundoSVG.exists() || !frenteSVG.exists() || !nomeIcone) { return }

        if (!genDir.exists()) { genDir.mkdirs() }
        String arqIcone = nomeIcone.endsWith('.png') ? nomeIcone : "${nomeIcone}.png"
        File pngInjetavel = new File(genDir, arqIcone)

        String magickCmd = eWindows ? "${winDisc}:/msys64/ucrt64/bin/magick.exe" : 'magick'
        List<String> mesclaCmd = [
            magickCmd, '-background', 'none',
            fundoSVG.absolutePath, frenteSVG.absolutePath,
            '-gravity', 'center', '-composite', '-resize', '64x64',
            pngInjetavel.absolutePath
        ]

        ProcessBuilder pb = new ProcessBuilder(mesclaCmd)
        if (eWindows) {
            pb.environment().put(
                'PATH', 
                "${winDisc}:/msys64/ucrt64/bin" + File.pathSeparator + pb.environment().get('PATH')
            )
        }

        Process processo = pb.start()
        processo.waitForProcessOutput(System.out, System.err)
        if (processo.exitValue() != 0) {
            throw new GradleException("Falha ao gerar ${arqIcone} via ImageMagick")
        }
    }
    // Converte arquivos de asseios (+ ícone gerado opcional) em um header C++
    static void embuteRes(File asseiosDir, File genDir, String asseiosNome,
        String nomeIcone = 'favicon', List<String> extensoesIgnoradas = []
    ) {
        File saidaHPP = new File(genDir, "${asseiosNome}.hpp")
        if (!asseiosDir.exists()) { return }
        saidaHPP.parentFile.mkdirs()

        Set<String> ignorar = extensoesIgnoradas.collect {
            it.toLowerCase().replaceFirst(/^\./, '')
        } as Set
        List<String> arqvs = []
        StringBuilder hpp = new StringBuilder()
        int idx = 0

        hpp.append('#pragma once\n')
        hpp.append('#include <cstdint>\n')
        hpp.append('#include <string>\n')
        hpp.append('#include <vector>\n')
        hpp.append('#include <map>\n\n')
        hpp.append("namespace ${asseiosNome} {\n")

        Closure<Void> adicionaBytes = { String chaveMapa, byte[] bytes ->
            String nomeVar = "res_${idx++}_${chaveMapa.replaceAll('[^a-zA-Z0-9]', '_')}"
            if (bytes.length == 0) {
                arqvs.add("\t\t{\"${chaveMapa}\", std::vector<uint8_t>{}}")
            } else {
                hpp.append("    const unsigned char ${nomeVar}[] = {")
                hpp.append(bytes.collect { String.format('0x%02x', it) }.join(', '))
                hpp.append('};\n')
                arqvs.add("\t\t{\"${chaveMapa}\", std::vector<uint8_t>(${nomeVar}, ${nomeVar} + sizeof(${nomeVar}))}")
            }
        }

        asseiosDir.eachFileRecurse(FileType.FILES) { File arq ->
            String ext = arq.name.contains('.')
                ? arq.name.substring(arq.name.lastIndexOf('.') + 1).toLowerCase()
                : ''
            if (ext in ignorar) { return }

            String dir = asseiosDir.toPath().relativize(arq.toPath())
                .toString().replace(File.separator, '/')
            adicionaBytes(dir, arq.readBytes())
        }

        if (nomeIcone) {
            String arqIcone = nomeIcone.endsWith('.png') ? nomeIcone : "${nomeIcone}.png"
            File pngInjetavel = new File(genDir, arqIcone)
            if (pngInjetavel.exists()) {
                adicionaBytes(arqIcone, pngInjetavel.readBytes())
            }
        }

        hpp.append('\n\tinline std::vector<uint8_t> Obter(const std::string& caminho) {\n')
        hpp.append('\t\tstatic std::map<std::string, std::vector<uint8_t>> arquivos = {\n')
        hpp.append(arqvs.join(',\n'))
        hpp.append('\n\t\t};\n')
        hpp.append('\t\tif (arquivos.count(caminho)) return arquivos[caminho];\n')
        hpp.append('\t\treturn {};\n')
        hpp.append('\t}\n')
        hpp.append('}\n')

        String conteudo = hpp.toString()
        if (!saidaHPP.exists() || saidaHPP.text != conteudo) {
            saidaHPP.text = conteudo
        }
    }
    // Gera header com macros #define a partir de um mapa flexível [CHAVE: valor]
    static void geraPropriedadesHpp(File genDir, Map<String, Object> propriedades,
        String nomeArquivo = 'propriedades.hpp'
    ) {
        File propHpp = new File(genDir, nomeArquivo)
        propHpp.parentFile.mkdirs()

        StringBuilder sb = new StringBuilder('#pragma once\n')
        propriedades.each { String chave, Object valor ->
            if (valor instanceof Number || valor instanceof Boolean) {
                sb.append("#define ${chave} ${valor}\n")
            } else {
                String escapado = String.valueOf(valor)
                    .replace('\\', '\\\\')
                    .replace('"', '\\"')
                sb.append("#define ${chave} \"${escapado}\"\n")
            }
        }

        String conteudo = sb.toString()
        if (!propHpp.exists() || propHpp.text != conteudo) {
            propHpp.text = conteudo
        }
    }
    // Coleta flags -I de todas as subpastas de src/main/bib
    static List<String> coletaIncludesBib(File projDir, boolean eRemoto) {
        List<String> flamas = []
        File bibDir = new File(projDir, 'src/main/bib')
        if (bibDir.exists()) {
            bibDir.eachDirRecurse { File dir ->
                if (eRemoto) {
                    String relativo = projDir.toPath().relativize(dir.toPath()).toString()
                    flamas << "-I${relativo.replace(File.separator, '/')}"
                } else {
                    flamas << "-I${dir.absolutePath.replace(File.separator, '/')}"
                }
            }
        }
        return flamas
    }
    // Coleta os arquivos .cpp de src/main/cpp e src/main/bib/cpp
    static List<String> coletaFontesCpp(File projDir) {
        List<String> fontes = []
        ['src/main/cpp', 'src/main/bib/cpp'].each { String subDir ->
            File pasta = new File(projDir, subDir)
            if (pasta.exists()) {
                pasta.eachFileRecurse(FileType.FILES) { File arq ->
                    if (arq.name.endsWith('.cpp')) {
                        fontes << projDir.toPath().relativize(arq.toPath())
                            .toString().replace(File.separator, '/')
                    }
                }
            }
        }
        return fontes
    }
    // Flamas mínimas de compilação; plugins e usuário adicionam o resto via flamasExtras
    static List<String> pegaFlamasComp(File projDir, File genDir, String alvo, String arqtt,
        String config, boolean eRemoto, boolean eWindowsHost, String stdCpp = 'c++26',
        String otimizacao = null, List<String> flamasExtras = []
    ) {
        String osAlvo = alvo.toLowerCase()
        String opt = otimizacao ?: (config == 'Release' ? '-Os' : '-O0')
        List<String> flamas = [
            "-std=${stdCpp}",
            opt,
            '-Wdeprecated'
        ]

        if (config == 'Debug') { 
            flamas << '-g' 
            if (osAlvo == 'windows' && stdCpp == 'c++26') {
                flamas << '-D__cpp_lib_constexpr_exceptions=0'
            }
        }
        if (osAlvo in ['windows', 'linux'] && arqtt in ['X86', 'X86-64']) {
            flamas << (arqtt == 'X86' ? '-m32' : '-m64')
        }

        if (eRemoto) {
            flamas << '-Isrc/main/headers'
            if (eWindowsHost && osAlvo == 'windows') {
                flamas << "-I${genDir.absolutePath.replace(File.separator, '/')}"
            } else {
                flamas.addAll(['-Ibuild/tmp/generated', '-Ibuild/tmp'])
            }
        } else {
            flamas.addAll([
                "-I${projDir.absolutePath.replace(File.separator, '/')}/src/main/headers",
                "-I${genDir.absolutePath.replace(File.separator, '/')}"
            ])
        }

        flamas.addAll(coletaIncludesBib(projDir, eRemoto))
        if (flamasExtras) { flamas.addAll(flamasExtras) }
        return flamas
    }
    // Flamas mínimas de linkagem com separação explícita entre bibliotecas estáticas e dinâmicas
    static List<String> pegaFlamasLink(String alvo, String arqtt, String config, 
        List<String> flamasEstaticas = [], List<String> flamasDinamicas = [],
        List<String> flamasExtras = []
    ) {
        String osAlvo = alvo.toLowerCase()
        List<String> flamas = []

        if (config == 'Release' && osAlvo in ['windows', 'linux']) {
            flamas << '-s'
        }

        if (osAlvo == 'windows') {
            if (flamasEstaticas) {
                flamas.addAll(['-Wl,-Bstatic', '-Wl,--start-group'])
                flamas.addAll(flamasEstaticas)
                flamas << '-Wl,--end-group'
            }
            if (flamasDinamicas) {
                flamas.addAll(['-Wl,-Bdynamic', '-Wl,--start-group'])
                flamas.addAll(flamasDinamicas)
                flamas << '-Wl,--end-group'
            }
            // Runtime mínimo C++ estático no MinGW para não depender de libstdc++-6.dll
            flamas.addAll([
                '-Wl,-Bstatic', '-lstdc++', '-lwinpthread',
                '-Wl,-Bdynamic', '-static-libgcc', '-static-libstdc++'
            ])
        } else if (osAlvo == 'linux') {
            if (arqtt == 'X86') {
                flamas.addAll(['-m32', '-Wl,-m,elf_i386', '-L/usr/lib/i386-linux-gnu'])
            } else {
                flamas.addAll([
                    '-m64', '-Wl,-m,elf_x86_64', '-L/usr/local/lib', '-L/usr/lib/x86_64-linux-gnu'
                ])
            }
            if (flamasEstaticas) {
                flamas.addAll(['-Wl,-Bstatic', '-Wl,--start-group'])
                flamas.addAll(flamasEstaticas)
                flamas << '-Wl,--end-group'
            }
            flamas << '-Wl,-Bdynamic'
            if (flamasDinamicas) { flamas.addAll(flamasDinamicas) }
            flamas.addAll(['-pthread', '-static-libgcc', '-static-libstdc++'])
        } else {
            // Clang/Apple ld64 e NDK não usam -Wl,-Bstatic / --start-group
            if (flamasEstaticas) { flamas.addAll(flamasEstaticas) }
            if (flamasDinamicas) { flamas.addAll(flamasDinamicas) }
        }

        if (flamasExtras) { flamas.addAll(flamasExtras) }
        return flamas
    }
    // Motor genérico de instalação de pacotes (MSYS2 no Windows, apt no Linux)
    static void instalaDeps(Closure<List<String>> executaRemoto, String winDisc,
        List<String> winDeps = [], List<String> linDeps = [], boolean habilitarI386 = false
    ) {
        if (winDeps) {
            println '>> INFO: Instalando dependências do Windows...'
            try {
                Process procWin = executaRemoto('windows', [
                    "${winDisc}:/msys64/usr/bin/bash.exe -l -c",
                    "\"pacman -Syu --needed --noconfirm ${winDeps.join(' ')}\""
                ]).execute()
                procWin.waitForProcessOutput(System.out, System.err)
                if (procWin.exitValue() != 0) {
                    println ">> ERRO: Falha ao instalar no Windows (Código: ${procWin.exitValue()})"
                }
            } catch (Exception e) {
                println ">> AVISO: VM Windows indisponível. ${e.message}"
            }
        }

        if (linDeps) {
            println '>> INFO: Instalando dependências do Linux...'
            List<String> cmdsLin = []
            if (habilitarI386) { cmdsLin.addAll(['sudo dpkg --add-architecture i386', '&&']) }
            cmdsLin.addAll(['sudo apt update', '&&', 'sudo apt install', '-y', linDeps.join(' ')])
            try {
                Process procLin = executaRemoto('linux', cmdsLin).execute()
                procLin.waitForProcessOutput(System.out, System.err)
                if (procLin.exitValue() != 0) {
                    println ">> ERRO: Falha ao instalar no Linux (Código: ${procLin.exitValue()})"
                }
            } catch (Exception e) {
                println ">> AVISO: VM Linux indisponível. ${e.message}"
            }
        }
    }
}