aws iam create-user --user-name jenkins
aws iam create-access-key --user-name jenkins | tee /tmp/create_output.json
cat << EoF > /tmp/jenkins_aws_creds.sh
export AWS_SECRET_ACCESS_KEY=`grep SecretAccessKey /tmp/create_output.json  | awk -F: '{print $2}' | sed 's/,//' | sed 's/^ //g'`
export AWS_ACCESS_KEY_ID=`grep AccessKeyId /tmp/create_output.json | awk -F: '{print $2}' | sed 's/,//' | sed 's/^ //g'`
EoF
sudo mv /tmp/jenkins_aws_creds.sh /usr/local/bin/jenkins_aws_creds.sh
