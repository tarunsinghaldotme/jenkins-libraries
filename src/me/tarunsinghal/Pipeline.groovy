package me.tarunsinghal;

def helmVersion() {
    println "checking client/server version"
    sh "helm version --short"
}


def helmChartVersionUpdate(String version, String chart_dir) {
    println "${version}"
    sh "sed -i -e 's/0.1.0/${version}/g' ${chart_dir}/Chart.yaml"
}

def helmLint(String chart_dir) {
    sh "helm lint ${chart_dir}"
}


def helmDeploy(Map args) {
    //configure helm client and confirm tiller process is installed
    helmVersion()
    


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


def ImagePublishECR(String REGION, String repo_name, String DockerfileName, String DockerfilePath, String version ) {
	println "Building Docker Image"
	DockerImage = docker.build("${repo_name}", "-f ${DockerfileName} ${DockerfilePath}")

	println "Getting login to AWS ECR"
	sh "$(aws ecr get-login --no-include-email --region ${REGION})"

	println "Pushing Image to ECR"
	DockerImage.push("${version}")
        DockerImage.push("latest")
}


