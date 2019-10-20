pipeline {
	agent any
	
    environment {
		APP_IMAGE = "677143160410.dkr.ecr.eu-central-1.amazonaws.com/cloud-demo/app-flask1"
		APP_PORT = 5000
		// Amazon EKS cluster name -- take a look at /home/ec2-user/.kube/config when using eksctl to setup Amazon EKS
		K8S_CLUSTER_NAME = 'i-0f8c17fe3d267b34b@eksworkshop-eksctl.eu-central-1.eksctl.io'
    }
    
	stages {
		stage('Preparation') {
			environment {
				git_commit_short = "ASSERT FALSE"
			}

			steps {
				// Get code from a GitHub repository
				git 'https://github.com/fastviper/flask1.git'
			
			
				// set docker image tag to short SHA1 of commit from which we build
				// this is nice for traceability, but also Amazon EKS fails to refresh images with :latest tag
				script {
					git_commit_short = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
					APP_IMAGE = sh(returnStdout: true, script: "echo $APP_IMAGE:$git_commit_short").trim()
				}
			}
		}
	   
		stage('Build') {
			steps {
				echo 'In build'
				echo APP_IMAGE
				
				// docker build
				sh "docker build -t $APP_IMAGE ."

				// docker registry login
				sh '$(aws ecr get-login --region eu-central-1 --no-include-email)'
				// docker push (not needed locally, but eventually...)
				sh "docker push $APP_IMAGE"
				
				
			}
		}
		stage('CI-start') {
			steps {
				echo 'In CI-start'
				echo APP_IMAGE

				echo 'Deploying to local CI env'
				// APP_IMAGE and APP_PORT will be taken automatically by docker-compose as documented in
				// https://docs.docker.com/compose/compose-file/#variable-substitution
				sh "APP_IMAGE=$APP_IMAGE /usr/local/bin/docker-compose up -d "
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
		// this will not work without setting security key for Jenkins
		stage('Deploy') {
			environment {
				// better source it from file, not commit to git -- but for proof of concept, it can stay here
				// They are in --> /usr/local/bin/jenkins_aws_creds.sh
				AWS_SECRET_ACCESS_KEY="lF4WZZ/E4oALpl3PBaStU2BqJQCbTQqa9uhCDIof"
				AWS_ACCESS_KEY_ID="AKIAZ3KHAUZNKSQDARL5"
			}
		
			steps {
				// first we need the credentials to operate on cluster
				// this is already set up, and it's done once - by mapping AWS IAM role to kubernetes RBAC
				// we only need variables, as in environment above
				// REMEMBER TO GIVE jenkins RIGHTS TO DO DEPLOYMENTS -- give_jenkins_rights_on_cluster.sh
				// let's verify this, ok? :)
				sh "aws sts get-caller-identity"
				sh '/usr/local/bin/kubectl get pods -l app=flask1'
			
				echo 'Deploy to kubernetes cluster - complete CD (note that for this LoadBalancer must already be setup!)'
				sh 'curl -o flask1-deployment.yaml https://raw.githubusercontent.com/fastviper/flask1/master/jenkins-pipeline/flask1-deployment.yaml'
				sh "APP_IMAGE=$APP_IMAGE /usr/local/bin/kubectl apply -f flask1-deployment.yaml"
			}
		}
		stage('CICD-Test') {
			steps {
				// remember to setup load balancer and wait for DNS propagation, 15s won't be enough initially -- flask1-load-balancer-setup.sh
				echo 'Testing app availablility via Load Balancer'
				sh 'sleep 15'
				sh 'curl -fs -o /dev/null http://a54e6417ff2cd11e9837a06cb65aee8e-420668248.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a54e6417ff2cd11e9837a06cb65aee8e-420668248.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a54e6417ff2cd11e9837a06cb65aee8e-420668248.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a54e6417ff2cd11e9837a06cb65aee8e-420668248.eu-central-1.elb.amazonaws.com:8888'
				sh 'curl -fs -o /dev/null http://a54e6417ff2cd11e9837a06cb65aee8e-420668248.eu-central-1.elb.amazonaws.com:8888'
			}
		}

	}
}