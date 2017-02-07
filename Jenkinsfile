@Library('libpipelines@master') _

hose {
    EMAIL = 'qa'
    SLACKTEAM = 'stratioqa'
    DEVTIMEOUT = 40
    MODULE = 'unix-maven-plugin'
    REPOSITORY = 'unix-maven-plugin'
    FOSS = true
    MOJO = true

    DEV = { config ->        
        doCompile(config)
        doUT(config)
        doPackage(config)
                        
        parallel(DOC: {
            doDoc(config)
        }, QC: {
            doStaticAnalysis(config)
        }, DEPLOY: {
            doDeploy(config)
        }, failFast: config.FAILFAST)
        
    }
}
