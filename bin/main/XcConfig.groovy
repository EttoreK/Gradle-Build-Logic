import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Internal

abstract class XcConfig {
    static final String PROXY_TYPE = '1' // tipo de relação 1 - Native Target / 2 - Arquivo
    static final String INCLUDE_IN_INDEX = '0' // 1sim/0não é escaneado pelo motor de indexação
    static final String BUILD_ACTION_MASK = '2147483647' // número total de bits
    static final String RUN_ONLY_FOR_DEPLOYMENT_POSTPROCESSING = '0' // 1sim/0não apenas em Archive
    static final String DEFAULT_CONFIGURATION_IS_VISIBLE = '0' // flag de UI legada
    static final String MAIN_DIR = 'src/main/swift'
    static final String TESTE_DIR = 'src/test/swift'
    static final String RES_DIR = 'src/main/resources'

    abstract DirectoryProperty getProjDir()
    abstract DirectoryProperty getRootDir()
    abstract DirectoryProperty getSrcDir()
    abstract Property<Boolean> geteMac()
    abstract Property<String> getBuildXDir()
    abstract Property<String> getIdTime()
    abstract Property<String> getMacDir()
    abstract Property<String> getMacDono()
    abstract Property<String> getMacSede()
    abstract Property<String> getNomeApp()
    abstract Property<String> getTesteUiNome()
    abstract Property<String> getTesteUnitNome()
    abstract Property<String> getVmAppDir()
    abstract Property<String> getVmDir()
    abstract Property<String> getXcIdPacoteApp()
    abstract Property<String> getXcodeProj() 

    @Internal List<String> comandoRemoto(List<String> cmds) {
        // Extrai os valores das próprias propriedades nativas da Extension
        boolean eMac = geteMac().get()
        String dono = getMacDono().get()
        String sede = getMacSede().get()
        String dirApp = getVmAppDir().get()

        final Map<String, String> vmCmds = [
            bash: '/bin/bash',
            chmod: '/bin/chmod',
            chown: '/usr/sbin/chown',
            killall: '/usr/bin/killall',
            mkdir: '/bin/mkdir',
            open: '/usr/bin/open',
            rm: '/bin/rm',
            sudo: '/usr/bin/sudo',
            swift: '/usr/bin/swift',
            xcrun: '/usr/bin/xcrun',
        ]
        
        if (eMac) { 
            return ['bash', '-c', cmds.join(' ')] 
        } else {
            if (!dono || !sede) {
                throw new Exception('Credenciais não configuradas pra MacOs')
            }
            List<String> cmdRemotos = cmds.collect { vmCmds.getOrDefault(it, it) }
            return ['ssh', "${dono}@${sede}", "\"cd '${dirApp}' && ${cmdRemotos.join(' ')}\""]
        }
    }
}