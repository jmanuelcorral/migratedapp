output "resource_group_name" {
  description = "Name of the resource group"
  value       = azurerm_resource_group.main.name
}

output "aks_cluster_name" {
  description = "Name of the AKS cluster"
  value       = module.aks.cluster_name
}

output "aks_kube_config" {
  description = "Kubernetes config for AKS cluster"
  value       = module.aks.kube_config
  sensitive   = true
}

output "acr_login_server" {
  description = "ACR login server URL"
  value       = module.aks.acr_login_server
}

output "oracle_db_private_ip" {
  description = "Private IP address of Oracle database VM"
  value       = module.oracle_db.private_ip_address
}
