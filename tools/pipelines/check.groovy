def run(helm_home, namespace_prefix, kubernetes_domain, buildId) {
  stage("Dependencies") {
    sh('go run tools/pre-test-local-repos.go')
    sh("go get github.com/kubernetes/apimachinery/pkg/util/yaml")
  }

  stage("Run tests") {
    try {
      withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
        withEnv([
          'HELM_HOME=' + helm_home,
          'HELM_CMD=' + pwd() + '/helm',
          'KUBECTL_CMD=' + pwd() + '/kubectl',
        ]) {
          sh("set -o pipefail; exec 3>&1; go test -v -timeout 90m -args --images --charts --kubernetes-domain ${kubernetes_domain} --image-repo nexus-scc.ng.mirantis.net:5000/${buildId} --exclude tweepub,tweetics,kibana,logstash --prefix ${namespace_prefix}- 2>&1 3>&- | tee /dev/fd/3 | ./go-junit-report > report.xml 3>&-")
        }
      }
    } finally {
      junit('report.xml')
    }
  }
}
return this;
