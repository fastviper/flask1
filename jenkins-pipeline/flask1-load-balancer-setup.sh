kubectl delete service flask1-deployment
kubectl expose deployment flask1-deployment --type=LoadBalancer --port=8888 --target-port=5000
kubectl get svc|grep ^flask1  ## ExternalIP
