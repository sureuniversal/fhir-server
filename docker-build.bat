docker build --tag hapiproject/hapi:latest --tag hapiproject/hapi:4.1 --tag sureuniversal/fhir-server:locale7  -m 4g .
#docker push sureuniversal/fhir-server:locale7


#for running
docker run -it --env FHIR_PG_DATASOURCE_USER_NAME="postgres" --rm --privileged --pid=host  --publish 8443:8443 -t sureuniversal/fhir-server:locale6
#--env FHIR_PG_DATASOURCE_USER_NAME="postgres" --env FHIR_PG_DATASOURCE_PASSWORD="QvZP25ycU5bSPCVX" --env FHIR_PG_DATASOURCE_URL="jdbc:postgresql://fhir-db.c4vki83xjehx.eu-west-1.rds.amazonaws.com/fhir"
