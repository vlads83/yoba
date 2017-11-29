#!/usr/bin/env groovy

import jenkins.model.Jenkins
import hudson.model.*
import groovy.json.*

node('master'){
	
try {
 //clean up work space
    step([$class: 'WsCleanup'])

    env.GIT_REPO_URL = 'https://github.com/vlads83/Yoba.git'
    echo "Detected Git Repo URL: ${env.GIT_REPO_URL} , branch : ${env.BRANCH_NAME} , committer : ${env.GIT_AUTHOR_EMAIL}"
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'UserExclusion', excludedUsers: '''narezatel''']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '8cc10957-0d45-44f5-88e6-c3c2633213b9', url: 'https://github.com/vlads83/Yoba.git']]])
    git_msg = sh (script: "git log -1 | grep 'DRY_RUN'", returnStatus: true)
    echo "GIT message : ${git_msg}"
    envPropertiesPath = "ci_tools/pipeline_properties"
    echo "Cat pipiline_properties , 1st time" 	
    sh("cat ci_tools/pipeline_properties")



    //insert build paramerts into envs_properties
	if ("${env.BRANCH_NAME}" =~ /.*HOTFIX.*/) {
        sh("""sed -ri "s/(GITSCM_POLLING=)[^=]*\$/\\1\\"disable\\"/" ${envPropertiesPath}""")
      } //end if
	
	if ("${git_msg}" == '0') {
	echo "Modifying pipiline"
        sh("""sed -ri "s/(GITSCM_POLLING=)[^=]*\$/\\1\\"disable\\"/" ${envPropertiesPath}""")
      } //end if
      
      echo "Cat pipiline_properties , 2nd time"
      sh("cat ci_tools/pipeline_properties")

      load 'ci_tools/pipeline_properties'
      sh("printenv")


       //configure job properties
          if ("${env.GITSCM_POLLING}" == 'enable') {
                echo "GIT SCM POLLING : ${env.GITSCM_POLLING}"
            properties([
              parameters([
                  //string(name: 'SERVICE_NAME', defaultValue: "${env.SERVICE_NAME}", description: 'Service name'),
                  //string(name: 'ENVIRONMENT_TYPE', defaultValue: "${env.ENVIRONMENT_TYPE}", description: 'Environment name'),
                  //choice(choices: ['integration', 'staging', 'production'].join("\n"), description: 'Environment name', name: 'ENVIRONMENT_TYPE'),
                  //string(name: 'PLATFORM_NAME', defaultValue: "${env.PLATFORM_NAME}", description: 'Platform name'),
                ]),
                [$class: 'jenkins.model.BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '50']],
                disableConcurrentBuilds(),
                ])
          } //end if

          else if ("${env.GITSCM_POLLING}" == 'disable') {
                 echo "GIT SCM POLLING : ${env.GITSCM_POLLING}"
            properties([
              parameters([
                  //string(name: 'SERVICE_NAME', defaultValue: "${env.SERVICE_NAME}", description: 'Service name'),
                  //string(name: 'ENVIRONMENT_TYPE', defaultValue: "${env.ENVIRONMENT_TYPE}", description: 'Environment name'),
                  //choice(choices: ['integration', 'staging', 'production'].join("\n"), description: 'Environment name', name: 'ENVIRONMENT_TYPE'),
                  //string(name: 'PLATFORM_NAME', defaultValue: "${env.PLATFORM_NAME}", description: 'Platform name'),
                ]),
                [$class: 'jenkins.model.BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '50']],
                disableConcurrentBuilds(),
               ])
          }//end else if

}//end of try

catch(err){
    throw err
    error "Colud not find any Git repository for the job ${JOB_NAME}"
} //end of catch

} //end of node

node('master'){

try {

stage ('Build'){
	 sh ("chmod u+x ci_tools/build.sh")
	 sh ("ci_tools/build.sh")
	} //end of stage
	
////////////////////////////
 } //end of try

	catch(err){
    	throw err
    	error "Error in job ${JOB_NAME}"
	} //end of catch

} //end of node 