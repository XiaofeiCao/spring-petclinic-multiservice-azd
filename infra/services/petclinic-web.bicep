param location string
param serviceName string = 'petclinic-web'
param environmentId string
param registryEndpoint string
param backendUrl string
param appImage string

var placeholder = appImage == 'mcr.microsoft.com/azuredocs/containerapps-helloworld:latest'

resource app 'Microsoft.App/containerApps@2024-03-01' = {
  name: serviceName
  location: location
  tags: { 'azd-service-name': serviceName }
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    managedEnvironmentId: environmentId
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: placeholder
        ? null
        : {
            external: true
            allowInsecure: false
            targetPort: 8080
            transport: 'http'
          }
      registries: [{ server: registryEndpoint, identity: 'system' }]
    }
    template: {
      containers: [
        {
          name: 'app'
          image: appImage
          resources: { cpu: json('0.5'), memory: '1Gi' }
          env: [
            { name: 'PETCLINIC_BACKEND_URL', value: backendUrl }
          ]
          probes: placeholder
            ? []
            : [
                {
                  type: 'Startup'
                  tcpSocket: { port: 8080 }
                  periodSeconds: 5
                  failureThreshold: 60
                }
                {
                  type: 'Readiness'
                  tcpSocket: { port: 8080 }
                  periodSeconds: 10
                  failureThreshold: 3
                }
              ]
        }
      ]
      scale: { minReplicas: 1, maxReplicas: 1 }
    }
  }
}

output principalId string = app.identity.principalId
output SERVICE_PETCLINIC_WEB_ENDPOINT_URL string = placeholder
  ? ''
  : 'https://${app.properties.configuration.ingress.fqdn}'
