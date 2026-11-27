import groovy.io.FileType
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.gradle.api.GradleException

abstract class AuxiliarCppRecursosExternos {

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
                "if not exist \"${dirRemoto}\" mkdir \"${dirRemoto}\" &",
                "cd /d \"${dirRemoto}\" && ${entradaCmds.join(' ')}"
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

    static void copiaRes(File espolioDir, File destinoDir, String espNome) {
        if (!espolioDir.exists()) {
            println ">> AVISO: Pasta ${espNome} não encontrada em: ${espolioDir}"
            return
        }

        File recursDir = new File(destinoDir, 'recursos')
        if (!recursDir.exists()) { recursDir.mkdirs() }

        println ">> INFO: Copiando recursos de '${espolioDir.name}' para '${recursDir.name}'..."
        espolioDir.eachFileRecurse { File arq ->
            File destArq = new File(
                recursDir, espolioDir.toPath().relativize(arq.toPath()).toString()
            )
            if (arq.isDirectory()) {
                destArq.mkdirs()
            } else {
                destArq.parentFile.mkdirs()
                Files.copy(arq.toPath(), destArq.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    static void geraFavicon(
        File espolioDir,
        File genDir,
        boolean eWindows,
        String winDisc,
        String nomeIcone = 'favicon'
    ) {
        File fundoSVG = new File(espolioDir, 'icones/appBackground.svg')
        File frenteSVG = new File(espolioDir, 'icones/appForeground.svg')
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
                'PATH', "${winDisc}:/msys64/ucrt64/bin" + File.pathSeparator + pb.environment().get('PATH')
            )
        }

        Process processo = pb.start()
        processo.waitForProcessOutput(System.out, System.err)
        if (processo.exitValue() != 0) {
            throw new GradleException("Falha ao gerar ${arqIcone} via ImageMagick")
        }
    }

    static void embuteRes(
        File espolioDir,
        File genDir,
        String espNome,
        String nomeIcone = 'favicon',
        List<String> extensoesIgnoradas = []
    ) {
        File saidaHPP = new File(genDir, "${espNome}.hpp")
        if (!espolioDir.exists()) { return }
        saidaHPP.parentFile.mkdirs()

        Set<String> ignorar = extensoesIgnoradas.collect { it.toLowerCase().replaceFirst(/^\./, '') } as Set
        List<String> arqvs = []
        StringBuilder hpp = new StringBuilder()
        int idx = 0

        hpp.append('#pragma once\n')
        hpp.append('#include <cstdint>\n')
        hpp.append('#include <string>\n')
        hpp.append('#include <vector>\n')
        hpp.append('#include <map>\n\n')
        hpp.append("namespace ${espNome} {\n")

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

        espolioDir.eachFileRecurse(FileType.FILES) { File arq ->
            String ext = arq.name.contains('.') ? arq.name.substring(arq.name.lastIndexOf('.') + 1).toLowerCase() : ''
            if (ext in ignorar) { return }

            String dir = espolioDir.toPath().relativize(arq.toPath())
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

    static void geraPropriedadesHpp(
        File genDir,
        Map<String, Object> propriedades,
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

    static List<String> coletaIncludesBib(File projDir, boolean eRemoto) {
        List<String> flags = []
        File bibDir = new File(projDir, 'src/main/bib')
        if (bibDir.exists()) {
            bibDir.eachDirRecurse { File dir ->
                if (eRemoto) {
                    String relativo = projDir.toPath().relativize(dir.toPath()).toString()
                    flags << "-I${relativo.replace(File.separator, '/')}"
                } else {
                    flags << "-I${dir.absolutePath.replace(File.separator, '/')}"
                }
            }
        }
        return flags
    }

    static List<String> coletaFontesCpp(File projDir) {
        List<String> fontes = []
        ['src/main/cpp', 'src/main/bib/cpp'].each { String subDir ->
            File pasta = new File(projDir, subDir)
            if (pasta.exists()) {
                pasta.eachFileRecurse(FileType.FILES) { File arq ->
                    if (arq.name.endsWith('.cpp')) {
                        fontes << projDir.toPath().relativize(
                            arq.toPath()
                        ).toString().replace(File.separator, '/')
                    }
                }
            }
        }
        return fontes
    }
}