targetScope = 'subscription'

@minLength(1)
@maxLength(64)
param environmentName string
param location string
@description('Opt in before the first provision when subscription policy requires a VNet-integrated Container Apps environment.')
param useVirtualNetwork bool = false

@description('Database size: small = 1 vCore / 2 GiB; larger = 2 vCores / 4 GiB and higher cost. Both use 32 GiB storage.')
@allowed(['small', 'larger'])
param databaseSize string

@minLength(1)
param databaseAdministratorObjectId string
@minLength(1)
param databaseAdministratorName string

var restServiceName = 'petclinic-rest'
var webServiceName = 'petclinic-web'

resource group 'Microsoft.Resources/resourceGroups@2024-03-01' = {
  name: 'rg-${environmentName}'
  location: location
  tags: { 'azd-env-name': environmentName }
}

module hosting 'hosting.bicep' = {
  name: 'hosting'
  scope: group
  params: {
    location: location
    useVirtualNetwork: useVirtualNetwork
  }
}

module bootstrapRest 'bootstrap.bicep' = {
  name: 'bootstrap-rest'
  scope: group
  params: {
    location: location
    serviceName: restServiceName
    environmentId: hosting.outputs.environmentId
  }
}

module bootstrapWeb 'bootstrap.bicep' = {
  name: 'bootstrap-web'
  scope: group
  params: {
    location: location
    serviceName: webServiceName
    environmentId: hosting.outputs.environmentId
  }
}

module registryAccessRest 'registry-access.bicep' = {
  name: 'registry-access-rest'
  scope: group
  params: {
    registryName: hosting.outputs.registryName
    principalId: bootstrapRest.outputs.principalId
  }
}

module registryAccessWeb 'registry-access.bicep' = {
  name: 'registry-access-web'
  scope: group
  params: {
    registryName: hosting.outputs.registryName
    principalId: bootstrapWeb.outputs.principalId
  }
}

module database 'database.bicep' = {
  name: 'database'
  scope: group
  params: {
    location: location
    size: databaseSize
    administratorObjectId: databaseAdministratorObjectId
    administratorName: databaseAdministratorName
    outboundIps: bootstrapRest.outputs.outboundIps
  }
}

module petclinicRest 'services/petclinic-rest.bicep' = {
  name: 'service-petclinic-rest'
  scope: group
  dependsOn: [registryAccessRest]
  params: {
    location: location
    serviceName: restServiceName
    environmentId: hosting.outputs.environmentId
    registryEndpoint: hosting.outputs.registryEndpoint
    databaseHost: database.outputs.host
    appImage: bootstrapRest.outputs.image
  }
}

module petclinicWeb 'services/petclinic-web.bicep' = {
  name: 'service-petclinic-web'
  scope: group
  dependsOn: [registryAccessWeb]
  params: {
    location: location
    serviceName: webServiceName
    environmentId: hosting.outputs.environmentId
    registryEndpoint: hosting.outputs.registryEndpoint
    backendUrl: petclinicRest.outputs.SERVICE_PETCLINIC_REST_ENDPOINT_URL
    appImage: bootstrapWeb.outputs.image
  }
}

output AZURE_RESOURCE_GROUP string = group.name
output AZURE_CONTAINER_REGISTRY_ENDPOINT string = hosting.outputs.registryEndpoint
output CONTAINER_APP_ENVIRONMENT_ID string = hosting.outputs.environmentId
output AZURE_VIRTUAL_NETWORK_ID string = hosting.outputs.virtualNetworkId
output APPLICATION_PRINCIPAL_ID string = petclinicRest.outputs.principalId
output DATABASE_HOST string = database.outputs.host
output DATABASE_SERVER_ID string = database.outputs.serverId
output DATABASE_OUTBOUND_IPS string = string(bootstrapRest.outputs.outboundIps)
output DATABASE_SIZE string = databaseSize
output SERVICE_PETCLINIC_REST_ENDPOINT_URL string = petclinicRest.outputs.SERVICE_PETCLINIC_REST_ENDPOINT_URL
output SERVICE_PETCLINIC_WEB_ENDPOINT_URL string = petclinicWeb.outputs.SERVICE_PETCLINIC_WEB_ENDPOINT_URL
