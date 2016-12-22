#identity-entitlement-xacml


##Admin MicroServices
 
 Get all policy details
 ```
 curl -v --user PRIMARY/admin:admin -X GET http://localhost:9090/entitlement/admin/policy/getall 
 ```
 Get a policy with policyId
 ```
 curl -v --user PRIMARY/admin:admin -X GET http://localhost:9090/entitlement/admin/policy/{policyid} 
 ```
 Upload policy (.xacml file)
 ```
 curl -v --user PRIMARY/admin:admin -X POST --data-binary @{policy}.xacml http://localhost:9090/entitlement/admin/policy/create/{policy}
 ```
 
##Evaluation MicroServices

 Evaluate policy 
 ```
 curl -v --user PRIMARY/admin:admin -H "Content-Type: application/json" -H "Accept: application/xml" -d "{"subject" : "admin","resource" : "locker","action" : "open"}" -X POST http://localhost:9090/entitlement/evaluation/by-attrib
 ```
 
##Implemented features

1. Policy deployment listener
2. Virtually storing policies
3. Automatically sync file policy and virtual policy store and policy collection
4. Balana engine with policy, attribute and resource finder
(attribute and resource finder uses mock stores)
5. Evaluate policy
6. Micro services for Admin and evaluation

##DOTO

1. implement caching
2. refactor with java 8 features