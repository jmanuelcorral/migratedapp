resource "azurerm_virtual_network" "oracle" {
  name                = "${var.project_name}-${var.environment}-oracle-vnet"
  address_space       = ["10.0.0.0/16"]
  location            = var.location
  resource_group_name = var.resource_group_name

  tags = {
    Environment = var.environment
    Component   = "database"
  }
}

resource "azurerm_subnet" "oracle" {
  name                 = "oracle-subnet"
  resource_group_name  = var.resource_group_name
  virtual_network_name = azurerm_virtual_network.oracle.name
  address_prefixes     = ["10.0.1.0/24"]
}

resource "azurerm_network_interface" "oracle" {
  name                = "${var.project_name}-${var.environment}-oracle-nic"
  location            = var.location
  resource_group_name = var.resource_group_name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.oracle.id
    private_ip_address_allocation = "Dynamic"
  }

  tags = {
    Environment = var.environment
    Component   = "database"
  }
}

resource "azurerm_network_security_group" "oracle" {
  name                = "${var.project_name}-${var.environment}-oracle-nsg"
  location            = var.location
  resource_group_name = var.resource_group_name

  security_rule {
    name                       = "allow-oracle"
    priority                   = 100
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "1521"
    source_address_prefix      = "10.0.0.0/16"
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = "allow-ssh"
    priority                   = 200
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }

  tags = {
    Environment = var.environment
    Component   = "database"
  }
}

resource "azurerm_network_interface_security_group_association" "oracle" {
  network_interface_id      = azurerm_network_interface.oracle.id
  network_security_group_id = azurerm_network_security_group.oracle.id
}

resource "azurerm_linux_virtual_machine" "oracle" {
  name                = "${var.project_name}-${var.environment}-oracle-vm"
  resource_group_name = var.resource_group_name
  location            = var.location
  size                = "Standard_D4s_v3"
  admin_username      = "oracleadmin"

  admin_ssh_key {
    username   = "oracleadmin"
    public_key = file("~/.ssh/id_rsa.pub")
  }

  network_interface_ids = [
    azurerm_network_interface.oracle.id,
  ]

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Premium_LRS"
    disk_size_gb         = 128
  }

  source_image_reference {
    publisher = "Oracle"
    offer     = "Oracle-Linux"
    sku       = "ol85-lvm-gen2"
    version   = "latest"
  }

  custom_data = base64encode(templatefile("${path.module}/cloud-init.yaml", {
    oracle_password = var.oracle_admin_password
  }))

  tags = {
    Environment = var.environment
    Component   = "database"
  }
}

resource "azurerm_managed_disk" "oracle_data" {
  name                 = "${var.project_name}-${var.environment}-oracle-data"
  location             = var.location
  resource_group_name  = var.resource_group_name
  storage_account_type = "Premium_LRS"
  create_option        = "Empty"
  disk_size_gb         = 256

  tags = {
    Environment = var.environment
    Component   = "database"
  }
}

resource "azurerm_virtual_machine_data_disk_attachment" "oracle" {
  managed_disk_id    = azurerm_managed_disk.oracle_data.id
  virtual_machine_id = azurerm_linux_virtual_machine.oracle.id
  lun                = 0
  caching            = "ReadWrite"
}
