import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic



def setup() {
    
    def DESTINATION_DIR = '~/Notification'
    def GIT_REPO_URL = 'git@bitbucket.org:innowise-group/jenkins_libraries.git'
    def WORKSPACE = '/var/jenkins_home/jenkins_libraries'

    // Add necessary keys 
    sh """
        mkdir -p ~/.ssh
        mkdir -p ~/Notification
        touch ~/.ssh/known_hosts
        ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts
        git clone http://somerepo
        echo $PWD
    """
    
}
def call(String requestor, String requestType) {
    
    // Retrieve sockets params
    def nodeIP = sh(script: 'kubectl get pods -n $KUBE_NAMESPACE -l app=$CHART_NAME -o jsonpath="{.items[*].status.hostIP}" || echo "None"', returnStdout: true).trim()
    def nodePort = sh(script: 'kubectl get svc --selector=release=$CHART_NAME -n $KUBE_NAMESPACE -o jsonpath="{.items[*].spec.ports[*].nodePort}" || echo "None"', returnStdout: true).trim()
   
    
    setup()
    // Pull configuration from the JSON file
    // def cardConfig = readJSON file: 'Finally/Notification/resources/org/chat-builder/google-chat-build-notification.json'
    def jsonFile = readFile("jenkins_libraries/Notification/resources/org/chat-builder/google-chat-build-notification.json")
    // Define status color based on BUILD_STATUS
    def STATUS_COLOR
    def urlObama 
    def status = currentBuild.currentResult
    def duration = currentBuild.durationString - "and counting"
    def node = env.NODE_NAME
    def masterIP = InetAddress.localHost.hostAddress
    switch(currentBuild.currentResult) {
        case 'ABORTED':
            STATUS_COLOR = '#ffa500'
            urlObama = 'https://ichef.bbci.co.uk/news/976/cpsprodpb/757B/production/_93357003_obamacry_gunviolence976.jpg'
            break
        case 'SUCCESS':
            STATUS_COLOR = '#008000'
            urlObama = 'https://www.whitehouse.gov/wp-content/uploads/2021/01/44_barack_obama.jpg'
            break
        case 'FAILURE':
            STATUS_COLOR = '#ff0000'
            urlObama = 'https://bloximages.chicago2.vip.townnews.com/missoulian.com/content/tncms/assets/v3/editorial/2/cf/2cf17c54-1e39-51db-b07f-0bac6eec5d3e/587ba6414f921.preview.jpg?crop=800%2C420%2C0%2C64&resize=800%2C420&order=crop%2Cresize'
            break
        case 'UNSTABLE':
            STATUS_COLOR = '#ffff00'
            urlObama = 'https://cdn.mos.cms.futurecdn.net/D4CYnPiHmEBMrhA5BydvNf-415-80.jpg'
            break
        default:
            STATUS_COLOR = '#808080'
            urlObama = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQefjic9a_iKRaH4V0sQmzU-vp3_yIkkDMt0vs8rr09lA&s'
    }

    def jsonSlurper = new JsonSlurperClassic()
    def jsonObject = jsonSlurper.parseText(jsonFile)
    def headerString = "<font color=${STATUS_COLOR}> ${status} </font>"
    def textParagraph_Node =  String.valueOf(node)
    def textParagraph_Host_Name = String.valueOf(masterIP)
    def textParagraph_nodeIP =  String.valueOf(nodeIP)
    def textParagraph_nodePort = String.valueOf(nodePort)
    jsonObject.cardsV2[0].card.header.imageUrl = urlObama
    jsonObject.cardsV2[0].card.sections[0].header = headerString
    jsonObject.cardsV2[0].card.header.title = " Time spent  ${duration}"

    jsonObject.cardsV2[0].card.sections[0].widgets.add([
        "textParagraph": [
            "text": "Host name: " + textParagraph_Node
        ]
    ])
    jsonObject.cardsV2[0].card.sections[0].widgets.add([
        "textParagraph": [
            "text": "Host IP: " + textParagraph_Host_Name
        ]
    ])
    jsonObject.cardsV2[0].card.sections[0].widgets.add([
        "textParagraph": [
            "text": "Cluster node ip : " +  textParagraph_nodeIP
        ]
    ])
     jsonObject.cardsV2[0].card.sections[0].widgets.add([
        "textParagraph": [
            "text": "Node Port : " +  textParagraph_nodePort
        ]
    ])
    def updatedJson = JsonOutput.toJson(jsonObject)
    // Update JSON with status color
    //cardConfig.cardsV2[0].card.sections[0].header.color = STATUS_COLOR

    // Send a message to Google Chat through hook link
    googlechatnotification url: "https://chat.googleapis.com/v1/spaces/you_id", messageFormat: "card", message: updatedJson

}


 

