pipeline {
	agent any
	
    environment {
        APP_IMAGE = '677143160410.dkr.ecr.eu-central-1.amazonaws.com/cloud-demo/app-flask1:latest'
        APP_PORT = 5000
		// this is random every time
		K8S_CLUSTER_NAME = 'i-0f8c17fe3d267b34b@eksworkshop-eksctl.eu-central-1.eksctl.io'
    }
    
	stages {
	   stage('Preparation') {
			steps {
			  // Get code from a GitHub repository
			  git 'https://github.com/fastviper/flask1.git'
			}
	   }
	   
	   stage('Build') {
			steps {
			   // docker registry login
			   sh '$(aws ecr get-login --region eu-central-1 --no-include-email)'
			   // docker build
			   sh 'docker build -t $APP_IMAGE .'
			   // docker push (not needed locally, but eventually...)
			   sh 'docker push $APP_IMAGE'
		   }
	   }
	   stage('CI-start') {
			steps {
				echo 'Deploying to local CI env'
				// APP_IMAGE and APP_PORT will be taken automatically by docker-compose as documented in
				// https://docs.docker.com/compose/compose-file/#variable-substitution
				sh '/usr/local/bin/docker-compose up -d '
			}
	   }
	   stage('CI-Test') {
			steps {
				echo 'Testing local CI env'
				// -f : fail on http code 4xx or 5xx
				// -s : curl is silent
				// -o /dev/null : send any output to hell (just be aware that Lucifer has good taste)
				sh 'curl -fs -o /dev/null http://localhost:$APP_PORT'
			}
	   }
	   /*
	   // this will not work without setting security key for Jenkins
	   stage('Deploy') {
			steps {
				echo 'Deploy to kubernetes cluster - complete CD (note that for this LoadBalancer must already be setup)'
				##sh 'curl -o flask1-deployment.yaml https://raw.githubusercontent.com/fastviper/flask1/master/jenkins-pipeline/flask1-deployment.yaml'
				##sh '/usr/local/bin/kubectl apply -f flask1-deployment.yaml'
			}
	   }
	   */
	   stage('CICD-Test') {
			steps {
				echo 'Testing app availablility via Load Balancer'
				sh 'sleep 15'
				sh 'curl -fs -o /dev/null http://a52a59da6f11e11e980f3060a8146e6b-523974958.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a52a59da6f11e11e980f3060a8146e6b-523974958.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a52a59da6f11e11e980f3060a8146e6b-523974958.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a52a59da6f11e11e980f3060a8146e6b-523974958.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a52a59da6f11e11e980f3060a8146e6b-523974958.eu-central-1.elb.amazonaws.com:8888'
			}
	   }
	}
}