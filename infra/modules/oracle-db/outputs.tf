output "private_ip_address" {
  description = "Private IP address of the Oracle VM"
  value       = azurerm_network_interface.oracle.private_ip_address
}

output "vm_id" {
  description = "ID of the Oracle VM"
  value       = azurerm_linux_virtual_machine.oracle.id
}

output "vnet_id" {
  description = "ID of the Oracle VNet"
  value       = azurerm_virtual_network.oracle.id
}
