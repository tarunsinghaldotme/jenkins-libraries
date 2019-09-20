package me.tarunsinghal;

def helmVersion() {
    println "checking client/server version"
    sh 'helm version --short'
}


def helmChartVersionUpdate(String version, String chart_dir) {
    sh 'sed -i "s#0.1.0#${version}#g" ${chart_dir}/Chart.yaml'
}

def helmLint(String chart_dir) {
    sh 'helm lint ${chart_dir}'
}


def helmDeploy(Map args) {
    //configure helm client and confirm tiller process is installed
    helmversion()
    
    helmChartVersionUpdate()


    def String namespace

    // If namespace isn't parsed into the function set the namespace to the name
    if (args.namespace == null) {
        namespace = "default"
    } else {
        namespace = args.namespace
    }

    if (args.dry_run) {
        println "Running dry-run deployment"

        sh "helm upgrade --dry-run --install ${args.name} ${args.chart_dir} --set image.tag=${args.version_tag} --namespace=${namespace}"
    } else {
        println "Running deployment"

        // reimplement --wait once it works reliable
        sh "helm upgrade --install ${args.name} ${args.chart_dir} --set image.tag=${args.version_tag} --namespace=${namespace}"

        // sleeping until --wait works reliably
        sleep(20)

        echo "Application ${args.name} successfully deployed. Use helm status ${args.name} to check"
    }
}   
